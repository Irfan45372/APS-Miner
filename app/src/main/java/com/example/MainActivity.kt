package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import com.example.ui.audio.SoundEffect
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ActiveMiningFeedbackBanner
import com.example.ui.components.ClaimSuccessDialog
import com.example.ui.components.ConnectedWalletHeader
import com.example.ui.components.CyberpunkAtmosphericBackground
import com.example.ui.components.DashboardPanel
import com.example.ui.components.MiningSessionHistoryDialog
import com.example.ui.components.OfflineEarningsDialog
import com.example.ui.components.RigVisualFrame
import com.example.ui.components.RpcConfigDialog
import com.example.ui.components.TerminalConsoleView
import com.example.ui.components.TransactionHistoryDialog
import com.example.ui.components.UpgradeRigDialog
import com.example.ui.components.UpgradeSuccessAnimationDialog
import com.example.ui.components.UserAccountDialog
import com.example.ui.components.WalletConnectDialog
import com.example.ui.components.WalletConnectScreen
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.subGlass
import com.example.ui.components.thickGlass
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberPink
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MiningViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val miningViewModel: MiningViewModel by viewModels()
    private lateinit var activityResultSender: ActivityResultSender

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResultSender = ActivityResultSender(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ApsMinerApp(
                    viewModel = miningViewModel,
                    activityResultSender = activityResultSender
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        miningViewModel.onAppPause()
    }

    override fun onResume() {
        super.onResume()
        miningViewModel.onAppResume()
    }
}

@Composable
fun ApsMinerApp(
    viewModel: MiningViewModel,
    activityResultSender: ActivityResultSender? = null
) {

    val state by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactionHistory.collectAsState()
    val sessions by viewModel.userSessions.collectAsState()
    val allAccounts by viewModel.allUserAccounts.collectAsState()

    var showRpcDialog by remember { mutableStateOf(false) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showSessionDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Living atmospheric aurora glowing through the thick frosted glass
        CyberpunkAtmosphericBackground(
            isMining = state.isMining,
            isOverclocked = state.isOverclocked
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // 1. Top Cyberpunk App Bar with Brand & Network Status (Thick Frosted Glass)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .thickGlass(
                            shape = RoundedCornerShape(18.dp),
                            borderWidth = 1.2.dp,
                            opacity = 0.45f
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(SolanaPurple, SolanaGreen)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "APS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "APS MINER",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (state.rpcConfig.isConnected) SolanaGreen else StatusError)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.rpcConfig.network.displayName} • ${if (state.rpcConfig.useDeveloperRpc) "Oracle Feed" else "Custom Helius"}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (state.rpcConfig.isConnected) SolanaGreen else StatusError
                                )
                            }
                        }
                    }

                    // Quick Navigation Icon Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Audio Mute/Unmute Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleSoundMute() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("audio_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (state.isSoundMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (state.isSoundMuted) "Unmute Audio" else "Mute Audio",
                                tint = if (state.isSoundMuted) Color(0xFF8E9CB2) else SolanaGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Session History Icon Button
                        IconButton(
                            onClick = {
                                viewModel.playUiSound(SoundEffect.CLICK)
                                showSessionDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("open_session_history_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (sessions.isNotEmpty()) {
                                        Badge(
                                            containerColor = SolanaGreen,
                                            contentColor = Color.Black
                                        ) {
                                            Text(sessions.size.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Session History",
                                    tint = SolanaGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Transaction History Icon
                        IconButton(
                            onClick = {
                                viewModel.playUiSound(SoundEffect.CLICK)
                                showHistoryDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("open_history_dialog_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (transactions.isNotEmpty()) {
                                        Badge(
                                            containerColor = SolanaPurple,
                                            contentColor = Color.White
                                        ) {
                                            Text(transactions.size.toString(), fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = SolanaPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Upgrade Depot Button
                        IconButton(
                            onClick = {
                                viewModel.playUiSound(SoundEffect.CLICK)
                                showUpgradeDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("open_upgrade_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Depot",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // RPC Node Settings Button
                        IconButton(
                            onClick = {
                                viewModel.playUiSound(SoundEffect.CLICK)
                                showRpcDialog = true
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("open_rpc_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "RPC Settings",
                                tint = CyberAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Phantom Wallet Connect Button
                        Box(
                            modifier = Modifier
                                .subGlass(
                                    shape = RoundedCornerShape(10.dp),
                                    borderWidth = 1.dp,
                                    accentColor = if (state.walletAddress.isNotBlank()) SolanaPurple else null
                                )
                                .clickable {
                                    viewModel.playUiSound(SoundEffect.CLICK)
                                    showWalletDialog = true
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("wallet_badge_header")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = if (state.walletAddress.isNotBlank()) SolanaGreen else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.walletAddress.isNotBlank())
                                        "${state.walletAddress.take(4)}...${state.walletAddress.takeLast(4)}"
                                    else "CONNECT",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.walletAddress.isNotBlank()) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1.5 User Gmail Account Database Strip (Thick Frosted Glass)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .thickGlass(
                            shape = RoundedCornerShape(14.dp),
                            borderWidth = 1.dp,
                            opacity = 0.40f
                        )
                        .clickable {
                            viewModel.playUiSound(SoundEffect.CLICK)
                            showAccountDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("user_gmail_account_strip"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SolanaPurple.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User",
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "DATABASE ACCOUNT",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = TextSecondary
                            )
                            Text(
                                text = state.currentUserEmail,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SolanaGreen.copy(alpha = 0.15f))
                                .border(1.dp, SolanaGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ROOM SYNC",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = SolanaGreen
                            )
                        }
                    }
                }

                // 1.8 Connected Wallet Header Strip (with Red Disconnect Button)
                if (state.walletAddress.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ConnectedWalletHeader(
                        walletAddress = state.walletAddress,
                        onWalletClicked = {
                            viewModel.playUiSound(SoundEffect.CLICK)
                            showWalletDialog = true
                        },
                        onDisconnectClicked = {
                            viewModel.playUiSound(SoundEffect.CLICK)
                            viewModel.disconnectWallet(activityResultSender)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Hardware Visual Rig Frame (APS Micro Node Alpha / Turbos)
                RigVisualFrame(
                    rig = state.currentRig,
                    isMining = state.isMining,
                    isOverclocked = state.isOverclocked,
                    temperatureC = state.rigTemperatureC
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2.5 Active Mining Feedback Animation Banner (Quantum Core, Waves & Nonce Particles)
                ActiveMiningFeedbackBanner(
                    isMining = state.isMining,
                    isOverclocked = state.isOverclocked,
                    hashrateMhs = state.currentHashrateMhs,
                    minedRatePerSec = state.currentRig.yieldPerSecond * if (state.isOverclocked) 2.5 else 1.0
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Mining Dashboard Panel (Controls, Yield, Overclock, Claim, Session Timer & Idle State)
                DashboardPanel(
                    minedAps = state.minedAps,
                    hashrateMhs = state.currentHashrateMhs,
                    isMining = state.isMining,
                    processStatus = state.processStatus,
                    sessionDurationFormatted = state.formattedSessionTime,
                    idleDurationFormatted = state.formattedIdleTime,
                    currentSessionMinedAps = state.currentSessionMinedAps,
                    isOverclocked = state.isOverclocked,
                    overclockSecondsRemaining = state.overclockSecondsRemaining,
                    overclockCooldownSeconds = state.overclockCooldownSeconds,
                    isClaiming = state.isClaiming,
                    walletConnected = state.walletAddress.isNotBlank(),
                    apsPerUsd = state.rpcConfig.apsPerUsd,
                    onToggleMining = { viewModel.toggleMining() },
                    onActivateOverclock = { viewModel.activateOverclock() },
                    onClaimClick = {
                        if (state.walletAddress.isBlank()) {
                            viewModel.playUiSound(SoundEffect.CLICK)
                            showWalletDialog = true
                        } else {
                            viewModel.claimMinedTokens()
                        }
                    },
                    onOpenSessionHistory = {
                        viewModel.playUiSound(SoundEffect.CLICK)
                        showSessionDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Quick Oracle & Total Stats Row (Thick Frosted Glass)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Claimed Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .thickGlass(
                                shape = RoundedCornerShape(16.dp),
                                borderWidth = 1.dp,
                                opacity = 0.45f
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "TOTAL CLAIMED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${String.format(Locale.US, "%,.2f", state.totalClaimedAps)} APS",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = SolanaGreen
                            )
                        }
                    }

                    // Oracle Rate Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .thickGlass(
                                shape = RoundedCornerShape(16.dp),
                                borderWidth = 1.dp,
                                opacity = 0.45f
                            )
                            .clickable { showRpcDialog = true }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ORACLE VALUE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SolanaGreen)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$1 ≈ ${String.format(Locale.US, "%,.0f", state.rpcConfig.apsPerUsd)} APS",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = CyberAmber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Live Node Terminal Console
                TerminalConsoleView(logs = state.terminalLogs)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

        // --- Dialogs ---
        if (showAccountDialog) {
            UserAccountDialog(
                currentEmail = state.currentUserEmail,
                currentAccount = state.currentUserAccount,
                allAccounts = allAccounts,
                onSwitchAccount = { newEmail ->
                    viewModel.switchUserAccount(newEmail)
                },
                onDismiss = { showAccountDialog = false }
            )
        }

        if (showSessionDialog) {
            MiningSessionHistoryDialog(
                userEmail = state.currentUserEmail,
                sessions = sessions,
                onDismiss = { showSessionDialog = false }
            )
        }

        state.offlineEarnings?.let { earnings ->
            OfflineEarningsDialog(
                earnings = earnings,
                onCollect = { viewModel.collectOfflineEarnings() },
                onDismiss = { viewModel.dismissOfflineEarnings() }
            )
        }

        if (showRpcDialog) {
            RpcConfigDialog(
                config = state.rpcConfig,
                isTestingRpc = state.isTestingRpc,
                latencyMs = state.rpcLatencyMs,
                currentSlot = state.rpcCurrentSlot,
                onSaveCustomKey = { key ->
                    viewModel.setCustomRpcApiKey(key)
                },
                onUseDeveloperRpc = {
                    viewModel.useDeveloperRpcPreset()
                },
                onSelectNetwork = { net ->
                    viewModel.setSolanaNetwork(net)
                },
                onTestPing = { viewModel.testRpcConnection() },
                onDismiss = { showRpcDialog = false }
            )
        }

        if (showWalletDialog) {
            Dialog(
                onDismissRequest = { showWalletDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                WalletConnectScreen(
                    viewModel = viewModel,
                    activityResultSender = activityResultSender,
                    onBack = { showWalletDialog = false }
                )
            }
        }

        if (showUpgradeDialog) {
            UpgradeRigDialog(
                currentLevel = state.currentRigLevel,
                availableMinedAps = state.minedAps,
                apsPerUsd = state.rpcConfig.apsPerUsd,
                walletAddress = state.walletAddress,
                onChainApsBalance = state.onChainApsBalance,
                isPollingHelius = state.isPollingHelius,
                isRefreshingWallet = state.isRefreshingWallet,
                onRefreshWallet = { viewModel.refreshBalances() },
                onUpgrade = { level ->
                    viewModel.upgradeRig(level)
                },
                onDismiss = { showUpgradeDialog = false }
            )
        }

        if (showHistoryDialog) {
            TransactionHistoryDialog(
                transactions = transactions,
                onDismiss = { showHistoryDialog = false }
            )
        }

        state.lastClaimRecord?.let { record ->
            ClaimSuccessDialog(
                record = record,
                onDismiss = { viewModel.dismissClaimDialog() }
            )
        }

        state.justUpgradedRig?.let { upgradedRig ->
            UpgradeSuccessAnimationDialog(
                rig = upgradedRig,
                apsPerUsd = state.rpcConfig.apsPerUsd,
                onDismiss = { viewModel.dismissUpgradeAnimationDialog() }
            )
        }
    }
