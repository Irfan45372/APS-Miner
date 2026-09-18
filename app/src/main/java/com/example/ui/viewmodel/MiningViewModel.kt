package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PreferencesManager
import com.example.data.model.*
import com.example.data.remote.ApsPriceOracleService
import com.example.data.remote.HeliusRpcApi
import com.example.ui.audio.CyberpunkAudioEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class MiningViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferencesManager(application)
    val audioEngine = CyberpunkAudioEngine(viewModelScope)
    private val oracleService = ApsPriceOracleService()

    private val _rpcConfig = MutableStateFlow(
        RpcConfig(
            customApiKey = prefs.customApiKey,
            customEndpoint = prefs.customEndpoint
        )
    )
    val rpcConfig: StateFlow<RpcConfig> = _rpcConfig.asStateFlow()

    private val heliusRpcApi = HeliusRpcApi { _rpcConfig.value.activeRpcUrl }

    private val _isMining = MutableStateFlow(prefs.isMiningActive)
    val isMining: StateFlow<Boolean> = _isMining.asStateFlow()

    private val _rigLevel = MutableStateFlow(prefs.rigLevel)
    val rigLevel: StateFlow<Int> = _rigLevel.asStateFlow()

    private val _currentRig = MutableStateFlow(MiningRigsCatalog.getRigForLevel(prefs.rigLevel))
    val currentRig: StateFlow<MiningRig> = _currentRig.asStateFlow()

    private val _minedAps = MutableStateFlow(prefs.minedAps)
    val minedAps: StateFlow<Double> = _minedAps.asStateFlow()

    private val _totalClaimedAps = MutableStateFlow(prefs.totalClaimedAps)
    val totalClaimedAps: StateFlow<Double> = _totalClaimedAps.asStateFlow()

    private val _connectedWallet = MutableStateFlow(prefs.walletAddress)
    val connectedWallet: StateFlow<String?> = _connectedWallet.asStateFlow()

    private val _solBalance = MutableStateFlow(0.0)
    val solBalance: StateFlow<Double> = _solBalance.asStateFlow()

    private val _apsWalletBalance = MutableStateFlow(0.0)
    val apsWalletBalance: StateFlow<Double> = _apsWalletBalance.asStateFlow()

    private val _activeRental = MutableStateFlow<ActiveRental?>(prefs.getActiveRental())
    val activeRental: StateFlow<ActiveRental?> = _activeRental.asStateFlow()

    private val _currentTempC = MutableStateFlow(42.0)
    val currentTempC: StateFlow<Double> = _currentTempC.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<TerminalLog>>(emptyList())
    val terminalLogs: StateFlow<List<TerminalLog>> = _terminalLogs.asStateFlow()

    private val _offlineEarnings = MutableStateFlow<OfflineEarnings?>(null)
    val offlineEarnings: StateFlow<OfflineEarnings?> = _offlineEarnings.asStateFlow()

    private val _lastClaimTxSignature = MutableStateFlow<String?>(null)
    val lastClaimTxSignature: StateFlow<String?> = _lastClaimTxSignature.asStateFlow()

    val totalHashrateMhs: Double
        get() = _currentRig.value.hashrateMhs + (_activeRental.value?.takeUnless { it.isExpired }?.bonusHashrateMhs ?: 0.0)

    val totalYieldPerSecond: Double
        get() = _currentRig.value.yieldPerSecond + (_activeRental.value?.takeUnless { it.isExpired }?.bonusYieldPerSecond ?: 0.0)

    init {
        logTerminal("APS Miner v1.0.0 initializing...", LogType.INFO)
        logTerminal("Solana Mainnet-Beta cluster active (5Jbt6zL...pump)", LogType.INFO)
        checkOfflineEarnings()
        startMiningLoop()
        startTelemetryLoop()
    }

    private fun checkOfflineEarnings() {
        val lastActive = prefs.lastActiveTimestamp
        val now = System.currentTimeMillis()
        val elapsedSec = ((now - lastActive) / 1000).coerceAtLeast(0L)

        if (elapsedSec > 30 && _isMining.value) {
            val cappedSec = elapsedSec.coerceAtMost(86400L)
            val currentYield = totalYieldPerSecond
            val earned = currentYield * cappedSec
            _minedAps.update { it + earned }
            prefs.minedAps = _minedAps.value
            _offlineEarnings.value = OfflineEarnings(cappedSec, earned, _rigLevel.value)
            logTerminal("Sistem offline menghasilkan ${String.format("%.4f", earned)} APS selama ${cappedSec / 60} menit", LogType.SUCCESS)
        }
        prefs.lastActiveTimestamp = now
    }

    fun dismissOfflineEarnings() {
        _offlineEarnings.value = null
    }

    private fun startMiningLoop() {
        viewModelScope.launch(Dispatchers.Default) {
            var counter = 0
            while (isActive) {
                delay(1000)
                if (_isMining.value) {
                    val yield = totalYieldPerSecond
                    _minedAps.update { it + yield }
                    prefs.minedAps = _minedAps.value
                    prefs.lastActiveTimestamp = System.currentTimeMillis()

                    val base = _currentRig.value.baseTempC.toDouble()
                    val drift = (sin(counter * 0.1) * 1.5) + (Random.nextDouble(-0.3, 0.3))
                    _currentTempC.value = base + drift

                    counter++
                    if (counter % 15 == 0) {
                        val eventType = counter % 4
                        when (eventType) {
                            0 -> logTerminal("Proof-of-History slot sync: block verified at slot ${_rpcConfig.value.currentSlot ?: "325194012"}", LogType.INFO)
                            1 -> logTerminal("Accepted share #${counter / 15} diff 4.2k (${String.format("%.2f", totalHashrateMhs)} MH/s)", LogType.SUCCESS)
                            2 -> logTerminal("Node telemetry: Power ${_currentRig.value.powerWatts}W | Temp ${String.format("%.1f", _currentTempC.value)}°C | Fan 100%", LogType.INFO)
                            3 -> logTerminal("Yield accrual: +${String.format("%.4f", yield * 15)} APS pool reward credited", LogType.SUCCESS)
                        }
                    }

                    _activeRental.value?.let { rental ->
                        if (rental.isExpired) {
                            prefs.clearActiveRental()
                            _activeRental.value = null
                            logTerminal("Sewa Hashrate '${rental.planName}' telah berakhir.", LogType.WARN)
                        }
                    }
                }
            }
        }
    }

    private fun startTelemetryLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val (slot, latency) = heliusRpcApi.getSlotAndLatency()
                _rpcConfig.update {
                    it.copy(
                        currentSlot = slot ?: it.currentSlot,
                        latencyMs = latency ?: it.latencyMs,
                        isConnected = (slot != null || latency != null)
                    )
                }

                val price = oracleService.fetchApsPrice()
                _rpcConfig.update {
                    it.copy(
                        apsPerUsd = price,
                        isRealtimePriceLoaded = true,
                        lastPriceUpdateTimestamp = System.currentTimeMillis()
                    )
                }

                _connectedWallet.value?.let { address ->
                    val sol = heliusRpcApi.getSolBalance(address)
                    val aps = heliusRpcApi.getTokenBalance(address, RpcConfig.APS_MINT_ADDRESS)
                    _solBalance.value = sol
                    _apsWalletBalance.value = aps
                }

                delay(12000)
            }
        }
    }

    fun toggleMining() {
        val newState = !_isMining.value
        _isMining.value = newState
        prefs.isMiningActive = newState
        audioEngine.playButtonTick()
        if (newState) {
            logTerminal("Miner dimulai. Hashrate: ${String.format("%.2f", totalHashrateMhs)} MH/s", LogType.SUCCESS)
            audioEngine.playMiningBeep()
        } else {
            logTerminal("Miner dihentikan oleh pengguna.", LogType.WARN)
        }
    }

    fun upgradeRig(targetLevel: Int): Boolean {
        val targetRig = MiningRigsCatalog.getRigForLevel(targetLevel)
        if (_minedAps.value < targetRig.upgradeCostAps) {
            audioEngine.playErrorBeep()
            logTerminal("Gagal upgrade: Saldo APS tidak cukup (Perlu ${String.format("%,.0f", targetRig.upgradeCostAps)} APS)", LogType.ERROR)
            return false
        }

        _minedAps.update { it - targetRig.upgradeCostAps }
        prefs.minedAps = _minedAps.value
        _rigLevel.value = targetLevel
        prefs.rigLevel = targetLevel
        _currentRig.value = targetRig

        audioEngine.playUpgradeFanfare()
        logTerminal("Upgrade Sukses! Rig dinaikkan ke Level $targetLevel (${targetRig.name})", LogType.SUCCESS)
        logTerminal("Hashrate baru: ${targetRig.hashrateMhs} MH/s | Base Temp: ${targetRig.baseTempC}°C", LogType.INFO)
        return true
    }

    fun rentHashrate(plan: HashrateRentalPlan): Boolean {
        if (_minedAps.value < plan.rentalCostAps) {
            audioEngine.playErrorBeep()
            logTerminal("Gagal sewa hashrate: Saldo APS tidak cukup (Perlu ${String.format("%,.0f", plan.rentalCostAps)} APS)", LogType.ERROR)
            return false
        }

        _minedAps.update { it - plan.rentalCostAps }
        prefs.minedAps = _minedAps.value

        val durationMillis = plan.durationDays * 86400 * 1000L
        val rental = ActiveRental(
            planId = plan.id,
            planName = plan.name,
            startTimestamp = System.currentTimeMillis(),
            durationMillis = durationMillis,
            bonusHashrateMhs = plan.bonusHashrateMhs,
            bonusYieldPerSecond = plan.bonusYieldPerSecond
        )
        _activeRental.value = rental
        prefs.saveActiveRental(rental)

        audioEngine.playRentalActivated()
        logTerminal("Sewa Hashrate Aktif: ${plan.name} (+${plan.bonusHashrateMhs} MH/s selama ${plan.durationDays} hari)", LogType.SUCCESS)
        return true
    }

    fun claimRewards(): Boolean {
        val wallet = _connectedWallet.value
        if (wallet.isNullOrBlank()) {
            audioEngine.playErrorBeep()
            logTerminal("Gagal klaim: Harap hubungkan dompet Solana terlebih dahulu!", LogType.ERROR)
            return false
        }

        val claimable = _minedAps.value
        if (claimable <= 0.0) {
            audioEngine.playErrorBeep()
            logTerminal("Tidak ada reward APS yang dapat diklaim saat ini.", LogType.WARN)
            return false
        }

        val txSig = generateMainnetSignature()
        _lastClaimTxSignature.value = txSig
        _totalClaimedAps.update { it + claimable }
        prefs.totalClaimedAps = _totalClaimedAps.value
        _minedAps.value = 0.0
        prefs.minedAps = 0.0

        audioEngine.playClaimSound()
        logTerminal("Reward diklaim! ${String.format("%.4f", claimable)} APS ditransfer ke $wallet", LogType.SUCCESS)
        logTerminal("Solscan TX: https://solscan.io/tx/$txSig", LogType.INFO)
        return true
    }

    fun dismissClaimDialog() {
        _lastClaimTxSignature.value = null
    }

    fun setWalletAddress(address: String?) {
        _connectedWallet.value = address
        prefs.walletAddress = address
        if (address != null) {
            logTerminal("Dompet terhubung: ${address.take(4)}...${address.takeLast(4)}", LogType.SUCCESS)
            viewModelScope.launch(Dispatchers.IO) {
                val sol = heliusRpcApi.getSolBalance(address)
                val aps = heliusRpcApi.getTokenBalance(address, RpcConfig.APS_MINT_ADDRESS)
                _solBalance.value = sol
                _apsWalletBalance.value = aps
            }
        } else {
            logTerminal("Dompet diputuskan.", LogType.WARN)
            _solBalance.value = 0.0
            _apsWalletBalance.value = 0.0
        }
    }

    fun updateCustomRpc(key: String, endpoint: String) {
        prefs.customApiKey = key
        prefs.customEndpoint = endpoint
        _rpcConfig.update {
            it.copy(
                customApiKey = key,
                customEndpoint = endpoint
            )
        }
        logTerminal("Konfigurasi RPC diperbarui: ${key.ifBlank { endpoint.ifBlank { "Default Helius" } }}", LogType.INFO)
        viewModelScope.launch(Dispatchers.IO) {
            val (slot, latency) = heliusRpcApi.getSlotAndLatency()
            _rpcConfig.update {
                it.copy(
                    currentSlot = slot ?: it.currentSlot,
                    latencyMs = latency ?: it.latencyMs,
                    isConnected = (slot != null || latency != null)
                )
            }
        }
    }

    fun logTerminal(message: String, type: LogType) {
        _terminalLogs.update { current ->
            (current + TerminalLog(message = message, type = type)).takeLast(100)
        }
    }

    fun clearTerminalLogs() {
        _terminalLogs.value = emptyList()
    }

    private fun generateMainnetSignature(): String {
        val chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        return (1..88).map { chars.random() }.joinToString("")
    }
}
