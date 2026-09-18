package com.example.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RpcConfig
import com.example.data.remote.SolanaWalletAdapterService
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MiningViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import kotlinx.coroutines.launch

@Composable
fun MiningScreen(
    viewModel: MiningViewModel,
    activity: ComponentActivity,
    activityResultSender: ActivityResultSender,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val walletService = remember { SolanaWalletAdapterService(context) }

    val isMining by viewModel.isMining.collectAsState()
    val currentRig by viewModel.currentRig.collectAsState()
    val minedAps by viewModel.minedAps.collectAsState()
    val connectedWallet by viewModel.connectedWallet.collectAsState()
    val solBalance by viewModel.solBalance.collectAsState()
    val apsWalletBalance by viewModel.apsWalletBalance.collectAsState()
    val activeRental by viewModel.activeRental.collectAsState()
    val currentTempC by viewModel.currentTempC.collectAsState()
    val rpcConfig by viewModel.rpcConfig.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val offlineEarnings by viewModel.offlineEarnings.collectAsState()
    val claimTxSignature by viewModel.lastClaimTxSignature.collectAsState()

    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showRentalDialog by remember { mutableStateOf(false) }
    var showWalletDialog by remember { mutableStateOf(false) }
    var showRpcDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ConnectedWalletHeader(
                walletAddress = connectedWallet,
                solBalance = solBalance,
                apsBalance = apsWalletBalance,
                rpcConfig = rpcConfig,
                onWalletClick = { showWalletDialog = true },
                onSettingsClick = { showRpcDialog = true }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RigVisualFrame(
                    rig = currentRig,
                    isMining = isMining,
                    currentTempC = currentTempC
                )

                DashboardPanel(
                    isMining = isMining,
                    rig = currentRig,
                    minedAps = minedAps,
                    totalHashrateMhs = viewModel.totalHashrateMhs,
                    totalYieldPerSecond = viewModel.totalYieldPerSecond,
                    currentTempC = currentTempC,
                    apsPerUsd = rpcConfig.apsPerUsd,
                    activeRental = activeRental,
                    onToggleMining = { viewModel.toggleMining() },
                    onClaimClick = {
                        if (connectedWallet == null) {
                            showWalletDialog = true
                        } else {
                            viewModel.claimRewards()
                        }
                    },
                    onUpgradeClick = { showUpgradeDialog = true },
                    onRentalClick = { showRentalDialog = true }
                )

                TerminalConsoleView(
                    logs = terminalLogs,
                    onClearLogs = { viewModel.clearTerminalLogs() }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SOLANA MAINNET-BETA • PUMP.FUN TOKEN",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MINT: ${RpcConfig.APS_MINT_ADDRESS}",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("APS Mint", RpcConfig.APS_MINT_ADDRESS))
                            Toast.makeText(context, "Alamat Mint disalin ke clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (showUpgradeDialog) {
        UpgradeRigDialog(
            currentRig = currentRig,
            minedAps = minedAps,
            apsPerUsd = rpcConfig.apsPerUsd,
            onUpgrade = { level ->
                if (viewModel.upgradeRig(level)) {
                    showUpgradeDialog = false
                }
            },
            onDismiss = { showUpgradeDialog = false }
        )
    }

    if (showRentalDialog) {
        HashrateRentalDialog(
            minedAps = minedAps,
            onRent = { plan ->
                if (viewModel.rentHashrate(plan)) {
                    showRentalDialog = false
                }
            },
            onDismiss = { showRentalDialog = false }
        )
    }

    if (showWalletDialog) {
        WalletConnectDialog(
            currentWallet = connectedWallet,
            onConnectMwa = {
                showWalletDialog = false
                scope.launch {
                    val address = walletService.connect(activityResultSender)
                    if (address != null) {
                        viewModel.setWalletAddress(address)
                    } else {
                        walletService.openPhantomPlayStore(activity)
                    }
                }
            },
            onManualAddressSubmit = { address ->
                viewModel.setWalletAddress(address)
                showWalletDialog = false
            },
            onDisconnect = {
                viewModel.setWalletAddress(null)
                showWalletDialog = false
            },
            onDismiss = { showWalletDialog = false }
        )
    }

    if (showRpcDialog) {
        RpcConfigDialog(
            rpcConfig = rpcConfig,
            onSave = { key, endpoint ->
                viewModel.updateCustomRpc(key, endpoint)
                showRpcDialog = false
            },
            onDismiss = { showRpcDialog = false }
        )
    }

    offlineEarnings?.let { earnings ->
        OfflineEarningsDialog(
            earnings = earnings,
            onDismiss = { viewModel.dismissOfflineEarnings() }
        )
    }

    claimTxSignature?.let { signature ->
        ClaimSuccessDialog(
            signature = signature,
            onDismiss = { viewModel.dismissClaimDialog() }
        )
    }
}
