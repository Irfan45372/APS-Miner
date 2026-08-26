package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.MiningRig
import com.example.data.model.MiningRigsCatalog
import com.example.data.model.MiningSessionRecord
import com.example.data.model.RpcConfig
import com.example.data.model.SolanaNetwork
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionStatus
import com.example.data.model.UserAccount
import com.example.data.remote.ApsPriceOracleService
import com.example.data.remote.HeliusWalletPollingService
import com.example.data.remote.SolanaRpcService
import com.example.data.remote.WalletManager
import com.example.data.remote.WalletSessionState
import com.example.ui.audio.CyberpunkAudioEngine
import com.example.ui.audio.SoundEffect
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class TerminalLog(
    val timestamp: String,
    val message: String,
    val type: LogType = LogType.INFO
)

enum class LogType {
    INFO, SUCCESS, WARNING, ERROR, MINING
}

enum class MiningProcessStatus {
    ACTIVE,
    IDLE,
    OVERCLOCKED,
    PAUSED
}

data class OfflineEarnings(
    val durationSeconds: Long,
    val earnedAps: Double,
    val userEmail: String,
    val calculatedAt: Long = System.currentTimeMillis()
) {
    val formattedDuration: String
        get() {
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            return if (hours > 0) {
                "${hours}h ${minutes}m ${seconds}s"
            } else if (minutes > 0) {
                "${minutes}m ${seconds}s"
            } else {
                "${seconds}s"
            }
        }
}

data class MiningUiState(
    val isMining: Boolean = true,
    val processStatus: MiningProcessStatus = MiningProcessStatus.ACTIVE,
    val activeSessionDurationSeconds: Long = 0L,
    val idleDurationSeconds: Long = 0L,
    val currentSessionMinedAps: Double = 0.0,
    val currentSessionStartTime: Long = 0L,
    val minedAps: Double = 0.0,
    val totalClaimedAps: Double = 0.0,
    val currentRigLevel: Int = 1,
    val currentRig: MiningRig = MiningRigsCatalog.getRigForLevel(1),
    val currentHashrateMhs: Double = 1.25,
    val isOverclocked: Boolean = false,
    val overclockSecondsRemaining: Int = 0,
    val overclockCooldownSeconds: Int = 0,
    val rigTemperatureC: Double = 42.0,
    val powerWatts: Int = 45,
    val currentUserEmail: String = "kakakkuganteng@gmail.com",
    val currentUserAccount: UserAccount? = null,
    val walletAddress: String = "",
    val solBalance: Double = 0.0,
    val onChainApsBalance: Double = 0.0,
    val hasApsTokenAccount: Boolean = false,
    val isRefreshingWallet: Boolean = false,
    val isPollingHelius: Boolean = false,
    val lastHeliusPollTime: Long = 0L,
    val rpcConfig: RpcConfig = RpcConfig(),
    val isTestingRpc: Boolean = false,
    val rpcLatencyMs: Long? = null,
    val rpcCurrentSlot: Long? = null,
    val isClaiming: Boolean = false,
    val lastClaimRecord: TransactionRecord? = null,
    val justUpgradedRig: MiningRig? = null,
    val offlineEarnings: OfflineEarnings? = null,
    val isSoundMuted: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val terminalLogs: List<TerminalLog> = emptyList()
) {
    val formattedSessionTime: String
        get() {
            val hours = activeSessionDurationSeconds / 3600
            val minutes = (activeSessionDurationSeconds % 3600) / 60
            val seconds = activeSessionDurationSeconds % 60
            return if (hours > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }

    val formattedIdleTime: String
        get() {
            val hours = idleDurationSeconds / 3600
            val minutes = (idleDurationSeconds % 3600) / 60
            val seconds = idleDurationSeconds % 60
            return if (hours > 0) {
                String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }
        }
}

class MiningViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val userAccountDao = database.userAccountDao()
    private val miningSessionDao = database.miningSessionDao()
    private val transactionDao = database.transactionDao()
    private val rpcService = SolanaRpcService()
    val priceOracleService = ApsPriceOracleService(viewModelScope)
    val audioEngine = CyberpunkAudioEngine(viewModelScope)

    val heliusPollingService = HeliusWalletPollingService(rpcService, viewModelScope)
    val walletManager = WalletManager(application, prefs, viewModelScope)

    private val _uiState = MutableStateFlow(MiningUiState(isSoundMuted = prefs.isSoundMuted))
    val uiState: StateFlow<MiningUiState> = _uiState.asStateFlow()
    val walletSessionState: StateFlow<WalletSessionState> = walletManager.sessionState

    // Session and transaction flows reactive to current user's Gmail
    private val _activeUserEmail = MutableStateFlow(prefs.currentUserEmail)

    val userSessions: StateFlow<List<MiningSessionRecord>> = miningSessionDao.getSessionsForUser(prefs.currentUserEmail)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionHistory: StateFlow<List<TransactionRecord>> = transactionDao.getTransactionsForUser(prefs.currentUserEmail)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUserAccounts: StateFlow<List<UserAccount>> = userAccountDao.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var miningTickerJob: Job? = null
    private var sessionTimerJob: Job? = null
    private var idleTimerJob: Job? = null
    private var overclockTimerJob: Job? = null
    private var currentActiveSessionId: Long = -1L
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        audioEngine.setMuted(prefs.isSoundMuted)
        initializeUserAccount(prefs.currentUserEmail)
        observeHeliusPolling()
        observeWalletSession()
        testRpcConnection()
        fetchRealtimeOraclePrice()
        checkOfflineIdleEarnings()
        startMiningSession()

        addTerminalLog("APS Node Engine initialized for user: ${prefs.currentUserEmail}", LogType.INFO)
        addTerminalLog("Local database mapped to account [${prefs.currentUserEmail}].", LogType.SUCCESS)
    }

    /**
     * Initializes or loads the UserAccount entity stored in Room for the given Gmail account
     */
    private fun initializeUserAccount(email: String) {
        val userEmail = if (email.isBlank()) "kakakkuganteng@gmail.com" else email.trim()
        prefs.currentUserEmail = userEmail
        _activeUserEmail.value = userEmail

        viewModelScope.launch {
            var account = userAccountDao.getUserAccount(userEmail)
            if (account == null) {
                // First-time initialization for this Gmail account
                account = UserAccount(
                    email = userEmail,
                    displayName = userEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                    rigLevel = prefs.rigLevel.coerceIn(1, 5),
                    minedAps = prefs.minedAps,
                    totalClaimedAps = prefs.totalClaimedAps,
                    walletAddress = prefs.walletAddress,
                    lastActiveTimestamp = System.currentTimeMillis()
                )
                userAccountDao.insertOrUpdate(account)
            }

            // Sync prefs & state with the loaded account database
            prefs.minedAps = account.minedAps
            prefs.totalClaimedAps = account.totalClaimedAps
            prefs.rigLevel = account.rigLevel
            if (account.walletAddress.isNotBlank()) {
                prefs.walletAddress = account.walletAddress
            }

            val savedLevel = account.rigLevel.coerceIn(1, 5)
            val rig = MiningRigsCatalog.getRigForLevel(savedLevel)
            val rpcConfig = RpcConfig(
                customApiKey = prefs.customRpcKey,
                useDeveloperRpc = prefs.useDeveloperRpc,
                network = if (prefs.isDevnet) SolanaNetwork.DEVNET else SolanaNetwork.MAINNET
            )

            _uiState.update {
                it.copy(
                    currentUserEmail = userEmail,
                    currentUserAccount = account,
                    minedAps = account.minedAps,
                    totalClaimedAps = account.totalClaimedAps,
                    currentRigLevel = savedLevel,
                    currentRig = rig,
                    currentHashrateMhs = rig.hashrateMhs,
                    walletAddress = account.walletAddress.ifBlank { prefs.walletAddress },
                    rpcConfig = rpcConfig,
                    powerWatts = rig.powerWatts,
                    rigTemperatureC = rig.baseTempC.toDouble()
                )
            }

            val currentWallet = _uiState.value.walletAddress
            if (currentWallet.isNotBlank()) {
                heliusPollingService.startPolling(currentWallet, rpcConfig.activeRpcUrl)
                refreshBalances()
            }
        }
    }

    /**
     * Switch current Gmail user account and load its unique database partition
     */
    fun switchUserAccount(newEmail: String) {
        val trimmed = newEmail.trim()
        if (trimmed.isBlank() || !trimmed.contains("@")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid Gmail / Google account address.") }
            return
        }

        viewModelScope.launch {
            // 1. Close and persist current session for existing user
            stopMiningSession(reason = "USER_SWITCH")
            persistCurrentUserToDatabase()

            // 2. Switch active email
            addTerminalLog("Switching database context to: $trimmed", LogType.INFO)
            initializeUserAccount(trimmed)

            // 3. Start fresh mining session for switched account
            delay(200)
            startMiningSession()
            _uiState.update { it.copy(successMessage = "Switched to account: $trimmed") }
        }
    }

    /**
     * Check offline idle earnings accumulated between lastMiningTimestamp and current time
     */
    private fun checkOfflineIdleEarnings() {
        val lastTimestamp = prefs.lastMiningTimestamp
        val now = System.currentTimeMillis()
        val elapsedSeconds = (now - lastTimestamp) / 1000

        // If app was closed/idle for at least 15 seconds while mining was previously on
        if (elapsedSeconds >= 15 && prefs.isMining) {
            val rig = MiningRigsCatalog.getRigForLevel(prefs.rigLevel)
            // 80% idle efficiency for background/offline mining
            val idleYield = elapsedSeconds * rig.yieldPerSecond * 0.80

            if (idleYield > 0.0001) {
                val earnings = OfflineEarnings(
                    durationSeconds = elapsedSeconds,
                    earnedAps = idleYield,
                    userEmail = prefs.currentUserEmail
                )
                _uiState.update { it.copy(offlineEarnings = earnings) }
                addTerminalLog(
                    "Calculated offline idle yield: +${String.format(Locale.US, "%,.2f", idleYield)} APS across ${earnings.formattedDuration}.",
                    LogType.SUCCESS
                )
            }
        }
    }

    /**
     * Collect offline earnings into user account
     */
    fun collectOfflineEarnings() {
        val earnings = _uiState.value.offlineEarnings ?: return
        viewModelScope.launch {
            val newMined = _uiState.value.minedAps + earnings.earnedAps
            prefs.minedAps = newMined
            prefs.lastMiningTimestamp = System.currentTimeMillis()

            // Record offline mining session in database for user's Gmail
            val offlineSession = MiningSessionRecord(
                userEmail = earnings.userEmail,
                sessionStartTime = System.currentTimeMillis() - (earnings.durationSeconds * 1000),
                sessionEndTime = System.currentTimeMillis(),
                durationSeconds = earnings.durationSeconds,
                minedAps = earnings.earnedAps,
                averageHashrateMhs = _uiState.value.currentRig.hashrateMhs * 0.8,
                rigLevel = _uiState.value.currentRigLevel,
                rigName = _uiState.value.currentRig.name,
                endReason = "OFFLINE_COLLECTED"
            )
            miningSessionDao.insertSession(offlineSession)

            _uiState.update {
                it.copy(
                    minedAps = newMined,
                    offlineEarnings = null,
                    successMessage = "Collected +${String.format(Locale.US, "%,.2f", earnings.earnedAps)} APS from offline idle mining!"
                )
            }
            persistCurrentUserToDatabase()
            addTerminalLog("Collected offline idle yield: +${String.format(Locale.US, "%,.2f", earnings.earnedAps)} APS", LogType.SUCCESS)
        }
    }

    fun dismissOfflineEarnings() {
        _uiState.update { it.copy(offlineEarnings = null) }
    }

    /**
     * Start active mining session and transition from idle to active
     */
    fun startMiningSession() {
        idleTimerJob?.cancel()
        sessionTimerJob?.cancel()
        miningTickerJob?.cancel()

        val startTime = System.currentTimeMillis()
        prefs.isMining = true
        prefs.currentSessionStartTime = startTime

        _uiState.update {
            it.copy(
                isMining = true,
                processStatus = if (it.isOverclocked) MiningProcessStatus.OVERCLOCKED else MiningProcessStatus.ACTIVE,
                activeSessionDurationSeconds = 0L,
                currentSessionMinedAps = 0.0,
                currentSessionStartTime = startTime,
                idleDurationSeconds = 0L,
                powerWatts = it.currentRig.powerWatts
            )
        }

        // Insert initial session entry into database stored for this Gmail user
        viewModelScope.launch {
            val sessionRecord = MiningSessionRecord(
                userEmail = _uiState.value.currentUserEmail,
                sessionStartTime = startTime,
                sessionEndTime = null,
                durationSeconds = 0L,
                minedAps = 0.0,
                averageHashrateMhs = _uiState.value.currentRig.hashrateMhs,
                rigLevel = _uiState.value.currentRigLevel,
                rigName = _uiState.value.currentRig.name,
                endReason = "STILL_ACTIVE"
            )
            currentActiveSessionId = miningSessionDao.insertSession(sessionRecord)
            prefs.currentSessionId = currentActiveSessionId
        }

        // Start session duration timer (every 1s)
        sessionTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update {
                    it.copy(activeSessionDurationSeconds = it.activeSessionDurationSeconds + 1)
                }
            }
        }

        // Start mining yield ticker (100ms precision)
        startMiningTicker()
        audioEngine.startAmbientMiningLoop(isOverclocked = _uiState.value.isOverclocked)
        audioEngine.playUiSound(SoundEffect.TOGGLE_ON)
        addTerminalLog("Mining engine STARTED for [${_uiState.value.currentUserEmail}]. Session initialized.", LogType.SUCCESS)
    }

    /**
     * Stop active mining session and transition to IDLE state with idle timer logic
     */
    fun stopMiningSession(reason: String = "MANUAL_STOP") {
        sessionTimerJob?.cancel()
        miningTickerJob?.cancel()
        idleTimerJob?.cancel()
        overclockTimerJob?.cancel()
        audioEngine.stopAmbientMiningLoop()
        audioEngine.playUiSound(SoundEffect.TOGGLE_OFF)

        val endTime = System.currentTimeMillis()
        val duration = _uiState.value.activeSessionDurationSeconds
        val sessionYield = _uiState.value.currentSessionMinedAps
        val userEmail = _uiState.value.currentUserEmail

        prefs.isMining = false
        prefs.lastMiningTimestamp = endTime

        _uiState.update {
            it.copy(
                isMining = false,
                isOverclocked = false,
                overclockSecondsRemaining = 0,
                processStatus = MiningProcessStatus.IDLE,
                idleDurationSeconds = 0L,
                powerWatts = 12, // Standby idle wattage
                currentHashrateMhs = 0.0
            )
        }

        // Update session in user's Gmail database
        viewModelScope.launch {
            if (currentActiveSessionId > 0) {
                val existing = miningSessionDao.getSessionById(currentActiveSessionId)
                if (existing != null) {
                    miningSessionDao.updateSession(
                        existing.copy(
                            sessionEndTime = endTime,
                            durationSeconds = duration,
                            minedAps = sessionYield,
                            endReason = reason
                        )
                    )
                }
            }
            persistCurrentUserToDatabase()
        }

        // Start idle state timer
        idleTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { current ->
                    val nextIdleTime = current.idleDurationSeconds + 1
                    // Gradual cooling down to ambient ~28°C during idle
                    val cooledTemp = (current.rigTemperatureC * 0.98 + 28.0 * 0.02)
                    current.copy(
                        idleDurationSeconds = nextIdleTime,
                        rigTemperatureC = cooledTemp
                    )
                }
            }
        }

        addTerminalLog(
            "Mining engine PAUSED ($reason). Session finished (${duration}s, +${String.format(Locale.US, "%,.4f", sessionYield)} APS). Entering IDLE standby mode.",
            LogType.WARNING
        )
    }

    /**
     * Toggles mining between Active and Idle states
     */
    fun toggleMining() {
        if (_uiState.value.isMining) {
            stopMiningSession("MANUAL_STOP")
        } else {
            startMiningSession()
        }
    }

    /**
     * Mining calculation loop running at 100ms precision
     */
    private fun startMiningTicker() {
        miningTickerJob?.cancel()
        miningTickerJob = viewModelScope.launch {
            var tickCount = 0
            while (isActive) {
                delay(100)
                if (_uiState.value.isMining) {
                    val state = _uiState.value
                    val multiplier = if (state.isOverclocked) 2.0 else 1.0
                    val yieldPerTick = (state.currentRig.yieldPerSecond * multiplier) / 10.0
                    val newMined = state.minedAps + yieldPerTick
                    val newSessionMined = state.currentSessionMinedAps + yieldPerTick

                    // Fluctuate hashrate organically
                    val jitter = (Random.nextDouble(-0.03, 0.03)) * state.currentRig.hashrateMhs
                    val rawHash = (state.currentRig.hashrateMhs + jitter) * multiplier
                    val currentHash = (rawHash * 100).toLong() / 100.0

                    // Dynamic temperature curve
                    val targetTemp = state.currentRig.baseTempC + (if (state.isOverclocked) 16.0 else 4.0) + Random.nextDouble(-0.5, 0.5)

                    _uiState.update {
                        it.copy(
                            minedAps = newMined,
                            currentSessionMinedAps = newSessionMined,
                            currentHashrateMhs = currentHash,
                            rigTemperatureC = (it.rigTemperatureC * 0.95 + targetTemp * 0.05)
                        )
                    }

                    tickCount++
                    // Persist state periodically every 5 seconds
                    if (tickCount % 50 == 0) {
                        prefs.minedAps = newMined
                        prefs.lastMiningTimestamp = System.currentTimeMillis()
                        persistCurrentUserToDatabase()
                    }
                    if (tickCount % 150 == 0) {
                        addTerminalLog("Batch verified at slot #${_uiState.value.rpcCurrentSlot ?: 312098450L} | Hash: ${currentHash} MH/s", LogType.MINING)
                    }
                }
            }
        }
    }

    /**
     * Persist current state to user's Room database profile
     */
    private suspend fun persistCurrentUserToDatabase() {
        val userEmail = _uiState.value.currentUserEmail
        val account = userAccountDao.getUserAccount(userEmail) ?: UserAccount(email = userEmail)
        val totalSessions = miningSessionDao.getSessionsForUser(userEmail).stateIn(viewModelScope).value.size
        val totalDuration = miningSessionDao.getTotalMiningDuration(userEmail) ?: 0L

        val updated = account.copy(
            minedAps = _uiState.value.minedAps,
            totalClaimedAps = _uiState.value.totalClaimedAps,
            rigLevel = _uiState.value.currentRigLevel,
            walletAddress = _uiState.value.walletAddress,
            lastActiveTimestamp = System.currentTimeMillis(),
            totalSessionsCount = totalSessions,
            totalMiningDurationSeconds = totalDuration
        )
        userAccountDao.insertOrUpdate(updated)
        _uiState.update { it.copy(currentUserAccount = updated) }
    }

    fun activateOverclock() {
        val state = _uiState.value
        if (state.isOverclocked || state.overclockCooldownSeconds > 0) return

        if (!state.isMining) {
            startMiningSession()
        }

        overclockTimerJob?.cancel()
        audioEngine.playUiSound(SoundEffect.OVERCLOCK)
        audioEngine.setOverclockAmbient(true)
        _uiState.update {
            it.copy(
                isOverclocked = true,
                processStatus = MiningProcessStatus.OVERCLOCKED,
                overclockSecondsRemaining = 30,
                overclockCooldownSeconds = 0
            )
        }
        addTerminalLog("OVERCLOCK ACTIVATED: +100% hashrate boost enabled for 30s!", LogType.WARNING)

        overclockTimerJob = viewModelScope.launch {
            var remaining = 30
            while (remaining > 0 && isActive) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(overclockSecondsRemaining = remaining) }
            }

            audioEngine.setOverclockAmbient(false)
            _uiState.update {
                it.copy(
                    isOverclocked = false,
                    processStatus = if (it.isMining) MiningProcessStatus.ACTIVE else MiningProcessStatus.IDLE,
                    overclockSecondsRemaining = 0,
                    overclockCooldownSeconds = 45
                )
            }
            addTerminalLog("Overclock expired. Cooling system engaged for 45s cooldown.", LogType.INFO)

            var cooldown = 45
            while (cooldown > 0 && isActive) {
                delay(1000)
                cooldown--
                _uiState.update { it.copy(overclockCooldownSeconds = cooldown) }
            }
            _uiState.update { it.copy(overclockCooldownSeconds = 0) }
            addTerminalLog("Overclock ready for reactivation.", LogType.SUCCESS)
        }
    }

    fun upgradeRig(newLevel: Int) {
        val targetRig = MiningRigsCatalog.getRigForLevel(newLevel)
        val state = _uiState.value
        if (newLevel <= state.currentRigLevel) return

        val hasMinedSufficient = state.minedAps >= targetRig.upgradeCostAps
        val hasOnChainSufficient = state.onChainApsBalance >= targetRig.upgradeCostAps

        if (!hasMinedSufficient && !hasOnChainSufficient) {
            _uiState.update {
                it.copy(errorMessage = "Insufficient APS balance. Need ${String.format(Locale.US, "%,.0f", targetRig.upgradeCostAps)} APS in mined balance or verified Phantom wallet to upgrade.")
            }
            return
        }

        val remainingMined = if (hasMinedSufficient) {
            state.minedAps - targetRig.upgradeCostAps
        } else {
            state.minedAps
        }
        prefs.minedAps = remainingMined
        prefs.rigLevel = newLevel
        audioEngine.playUiSound(SoundEffect.UPGRADE)

        _uiState.update {
            it.copy(
                minedAps = remainingMined,
                currentRigLevel = newLevel,
                currentRig = targetRig,
                justUpgradedRig = targetRig,
                powerWatts = targetRig.powerWatts,
                currentHashrateMhs = targetRig.hashrateMhs,
                successMessage = "Rig upgraded to Level $newLevel: ${targetRig.name}!"
            )
        }

        viewModelScope.launch {
            persistCurrentUserToDatabase()
        }

        val source = if (hasOnChainSufficient && !hasMinedSufficient) "Phantom On-Chain (${RpcConfig.TREASURY_WALLET_ADDRESS.take(4)}... Reserve)" else "Mined Yield"
        addTerminalLog("RIG UPGRADED to Level $newLevel (${targetRig.name}) via $source. Hashrate: ${targetRig.hashrateMhs} MH/s", LogType.SUCCESS)
        addTerminalLog("Stored rig upgrade in database for user account [${_uiState.value.currentUserEmail}].", LogType.INFO)
    }

    fun dismissUpgradeAnimationDialog() {
        _uiState.update { it.copy(justUpgradedRig = null) }
    }

    private fun observeWalletSession() {
        viewModelScope.launch {
            walletManager.sessionState.collect { session ->
                when (session) {
                    is WalletSessionState.Connected -> {
                        _uiState.update { current ->
                            current.copy(
                                walletAddress = session.publicKey,
                                errorMessage = null,
                                successMessage = "Connected: ${session.publicKey.take(4)}...${session.publicKey.takeLast(4)}"
                            )
                        }
                        persistCurrentUserToDatabase()
                        if (session.publicKey.isNotBlank()) {
                            heliusPollingService.startPolling(session.publicKey, _uiState.value.rpcConfig.activeRpcUrl)
                            refreshBalances()
                        }
                    }
                    is WalletSessionState.Disconnected -> {
                        _uiState.update { current ->
                            current.copy(
                                walletAddress = "",
                                solBalance = 0.0,
                                onChainApsBalance = 0.0,
                                hasApsTokenAccount = false
                            )
                        }
                        persistCurrentUserToDatabase()
                        heliusPollingService.stopPolling()
                    }
                    is WalletSessionState.Error -> {
                        _uiState.update { current ->
                            current.copy(errorMessage = session.message)
                        }
                        addTerminalLog("Wallet Error: ${session.message}", LogType.ERROR)
                    }
                    is WalletSessionState.Connecting -> {
                        addTerminalLog(session.message, LogType.INFO)
                    }
                }
            }
        }
    }

    private fun observeHeliusPolling() {
        viewModelScope.launch {
            heliusPollingService.pollingState.collect { pollingState ->
                _uiState.update { current ->
                    current.copy(
                        solBalance = pollingState.solBalance,
                        onChainApsBalance = pollingState.onChainApsBalance,
                        hasApsTokenAccount = pollingState.hasApsTokenAccount,
                        isRefreshingWallet = pollingState.isRefreshing,
                        isPollingHelius = pollingState.isPolling,
                        lastHeliusPollTime = pollingState.lastPollTimestamp
                    )
                }
            }
        }
    }

    fun connectWalletWithAdapter(sender: ActivityResultSender) {
        walletManager.connect(
            sender = sender,
            onSuccess = { pubKey ->
                addTerminalLog("Phantom MWA session authorized: ${pubKey.take(6)}...${pubKey.takeLast(6)}", LogType.SUCCESS)
            },
            onError = { err ->
                addTerminalLog("Phantom connection error: $err", LogType.ERROR)
            }
        )
    }

    fun connectWallet(address: String) {
        val trimmed = address.trim()
        if (trimmed.length < 32) {
            _uiState.update { it.copy(errorMessage = "Invalid Solana wallet address. Please provide a valid Base58 address.") }
            return
        }
        val success = walletManager.connectManualAddress(trimmed)
        if (success) {
            addTerminalLog("Phantom wallet linked: ${trimmed.take(6)}...${trimmed.takeLast(6)}", LogType.SUCCESS)
            heliusPollingService.startPolling(trimmed, _uiState.value.rpcConfig.activeRpcUrl)
            refreshBalances()
            viewModelScope.launch {
                persistCurrentUserToDatabase()
            }
        }
    }

    fun disconnectWallet(sender: ActivityResultSender? = null) {
        walletManager.disconnect(sender) {
            addTerminalLog("Wallet disconnected from local session.", LogType.INFO)
        }
    }

    fun refreshBalances() {
        val state = _uiState.value
        val wallet = state.walletAddress
        if (wallet.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingWallet = true) }
            val balances = heliusPollingService.checkBalanceImmediately(
                walletAddress = wallet,
                rpcUrl = state.rpcConfig.activeRpcUrl,
                apsMintAddress = RpcConfig.APS_MINT_ADDRESS
            )
            _uiState.update {
                it.copy(
                    isRefreshingWallet = false,
                    solBalance = balances.solBalance,
                    onChainApsBalance = balances.apsTokenBalance,
                    hasApsTokenAccount = balances.hasApsTokenAccount
                )
            }
            addTerminalLog("Wallet refreshed via Helius: ${String.format(Locale.US, "%.4f", balances.solBalance)} SOL | ${String.format(Locale.US, "%,.2f", balances.apsTokenBalance)} APS", LogType.INFO)
        }
    }

    fun fetchRealtimeOraclePrice() {
        viewModelScope.launch {
            val quote = priceOracleService.fetchCurrentPriceQuote(RpcConfig.APS_MINT_ADDRESS)
            val liveRate = quote.apsPerUsd
            val devFee = quote.calculateApsForFeeUsd(RpcConfig.ORACLE_FEE_USD)
            _uiState.update {
                val updatedConfig = it.rpcConfig.copy(
                    apsPerUsd = liveRate,
                    isRealtimePriceLoaded = true,
                    lastPriceUpdateTimestamp = quote.timestamp
                )
                it.copy(rpcConfig = updatedConfig)
            }
            addTerminalLog(
                "Oracle Feed (${quote.dexSource}): 1 USD = ${String.format(Locale.US, "%,.2f", liveRate)} APS | 1 APS = $${String.format(Locale.US, "%.8f", quote.priceUsdPerToken)} USD (Dev RPC Fee $10 = ${String.format(Locale.US, "%,.2f", devFee)} APS)",
                LogType.INFO
            )
        }
    }

    /**
     * Calculates the dynamic APS token amount required for any USD fee based on current Oracle DEX prices.
     */
    fun calculateRequiredApsForUsdFee(feeUsd: Double = 10.0): Double {
        val currentQuote = priceOracleService.latestQuote.value
        return currentQuote.calculateApsForFeeUsd(feeUsd)
    }

    fun setCustomRpcApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        prefs.customRpcKey = trimmed
        prefs.useDeveloperRpc = trimmed.isBlank()
        _uiState.update {
            val updatedConfig = it.rpcConfig.copy(
                customApiKey = trimmed,
                useDeveloperRpc = trimmed.isBlank()
            )
            it.copy(rpcConfig = updatedConfig)
        }
        addTerminalLog("Custom Helius API key configured. Testing latency...", LogType.INFO)
        testRpcConnection()
    }

    fun useDeveloperRpcPreset() {
        prefs.useDeveloperRpc = true
        prefs.customRpcKey = ""
        val devFee = _uiState.value.rpcConfig.developerFeeAps
        _uiState.update {
            val updated = it.rpcConfig.copy(
                useDeveloperRpc = true,
                customApiKey = "",
                customEndpoint = ""
            )
            it.copy(
                rpcConfig = updated,
                successMessage = "Developer RPC selected. Oracle Fee ($10 = ${String.format(Locale.US, "%,.2f", devFee)} APS)."
            )
        }
        addTerminalLog("Switched to Helius Developer RPC with Oracle integration ($10 = ${String.format(Locale.US, "%,.2f", devFee)} APS).", LogType.INFO)
        testRpcConnection()
        fetchRealtimeOraclePrice()
    }

    fun setSolanaNetwork(network: SolanaNetwork) {
        val isDev = network == SolanaNetwork.DEVNET
        prefs.isDevnet = isDev
        _uiState.update {
            val updated = it.rpcConfig.copy(network = network)
            it.copy(rpcConfig = updated)
        }
        addTerminalLog("Switched network to ${network.displayName}", LogType.WARNING)
        testRpcConnection()
    }

    fun testRpcConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingRpc = true) }
            val config = _uiState.value.rpcConfig
            val result = rpcService.checkRpcHealth(config.activeRpcUrl)
            _uiState.update {
                it.copy(
                    isTestingRpc = false,
                    rpcLatencyMs = result.latencyMs,
                    rpcCurrentSlot = result.slot,
                    rpcConfig = config.copy(
                        isConnected = result.isSuccess,
                        latencyMs = result.latencyMs,
                        currentSlot = result.slot
                    )
                )
            }
            if (result.isSuccess) {
                addTerminalLog("RPC Ping OK: ${result.latencyMs}ms | Slot #${result.slot ?: 0}", LogType.SUCCESS)
            } else {
                addTerminalLog("RPC Ping Error: ${result.errorMessage}", LogType.ERROR)
            }
        }
    }

    fun claimMinedTokens() {
        val state = _uiState.value
        if (state.walletAddress.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Connect your Phantom Solana wallet before claiming APS.") }
            return
        }

        val amountToClaim = state.minedAps
        if (amountToClaim < 0.000001) {
            _uiState.update { it.copy(errorMessage = "Minimum claim amount is 0.000001 APS.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isClaiming = true, errorMessage = null) }
            addTerminalLog("Initiating on-chain claim for ${String.format(Locale.US, "%.8f", amountToClaim)} APS...", LogType.INFO)

            val result = rpcService.claimApsTokens(
                rpcUrl = state.rpcConfig.activeRpcUrl,
                walletAddress = state.walletAddress,
                amountAps = amountToClaim,
                solBalance = state.solBalance
            )

            _uiState.update { it.copy(isClaiming = false) }

            if (result.isSuccess) {
                val newClaimed = state.totalClaimedAps + amountToClaim
                val newOnChain = state.onChainApsBalance + amountToClaim
                prefs.minedAps = 0.0
                prefs.totalClaimedAps = newClaimed
                audioEngine.playUiSound(SoundEffect.CLAIM)

                val record = TransactionRecord(
                    userEmail = state.currentUserEmail,
                    signature = result.signature,
                    amountAps = amountToClaim,
                    destinationWallet = state.walletAddress,
                    feeSol = result.feeSol,
                    timestamp = System.currentTimeMillis(),
                    status = TransactionStatus.CONFIRMED,
                    network = state.rpcConfig.network.displayName,
                    rpcUsed = if (state.rpcConfig.useDeveloperRpc) "Developer Helius Oracle" else "Custom Helius RPC"
                )

                transactionDao.insertTransaction(record)

                _uiState.update {
                    it.copy(
                        minedAps = 0.0,
                        totalClaimedAps = newClaimed,
                        onChainApsBalance = newOnChain,
                        lastClaimRecord = record,
                        successMessage = "Successfully claimed ${String.format(Locale.US, "%.6f", amountToClaim)} APS to your wallet!"
                    )
                }

                persistCurrentUserToDatabase()
                addTerminalLog("CLAIM CONFIRMED! Tx: ${record.shortSignature} | +${String.format(Locale.US, "%.6f", amountToClaim)} APS stored in account [${state.currentUserEmail}].", LogType.SUCCESS)
            } else {
                _uiState.update { it.copy(errorMessage = result.errorMessage ?: "Transaction claim failed.") }
                addTerminalLog("Claim transaction failed: ${result.errorMessage}", LogType.ERROR)
            }
        }
    }

    fun toggleSoundMute() {
        val nextMuted = !_uiState.value.isSoundMuted
        prefs.isSoundMuted = nextMuted
        audioEngine.setMuted(nextMuted)
        _uiState.update { it.copy(isSoundMuted = nextMuted) }
        if (!nextMuted) {
            audioEngine.playUiSound(SoundEffect.MUTE_TOGGLE)
            addTerminalLog("Audio UNMUTED: Ambient mining drone & UI sound FX active.", LogType.INFO)
        } else {
            addTerminalLog("Audio MUTED.", LogType.INFO)
        }
    }

    fun playUiSound(effect: SoundEffect) {
        audioEngine.playUiSound(effect)
    }

    fun onAppPause() {
        audioEngine.pauseAmbientOnAppPause()
    }

    fun onAppResume() {
        audioEngine.resumeAmbientOnAppResume()
    }

    fun dismissClaimDialog() {
        _uiState.update { it.copy(lastClaimRecord = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun addTerminalLog(msg: String, type: LogType = LogType.INFO) {
        val log = TerminalLog(
            timestamp = timeFormatter.format(Date()),
            message = msg,
            type = type
        )
        _uiState.update {
            val list = (listOf(log) + it.terminalLogs).take(30)
            it.copy(terminalLogs = list)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionTimerJob?.cancel()
        idleTimerJob?.cancel()
        miningTickerJob?.cancel()
        overclockTimerJob?.cancel()
        audioEngine.release()
    }
}
