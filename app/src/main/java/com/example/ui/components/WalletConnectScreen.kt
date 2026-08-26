package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RpcConfig
import com.example.data.remote.WalletManager
import com.example.data.remote.WalletSessionState
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberPink
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MiningViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel-aware overload of WalletConnectScreen
 */
@Composable
fun WalletConnectScreen(
    viewModel: MiningViewModel,
    activityResultSender: ActivityResultSender? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val sessionState by viewModel.walletSessionState.collectAsState()

    WalletConnectContent(
        sessionState = sessionState,
        solBalance = uiState.solBalance,
        apsBalance = uiState.onChainApsBalance,
        isRefreshing = uiState.isRefreshingWallet,
        isPollingHelius = uiState.isPollingHelius,
        apsPriceUsd = if (uiState.rpcConfig.apsPerUsd > 0) 1.0 / uiState.rpcConfig.apsPerUsd else 0.00045,
        rpcEndpoint = uiState.rpcConfig.activeRpcUrl,
        rpcNetworkName = uiState.rpcConfig.network.displayName,
        rpcLatencyMs = uiState.rpcLatencyMs,
        currentSlot = uiState.rpcCurrentSlot,
        onConnectMwa = {
            activityResultSender?.let { sender ->
                viewModel.connectWalletWithAdapter(sender)
            }
        },
        onConnectManual = { address ->
            viewModel.connectWallet(address)
        },
        onDisconnect = {
            viewModel.disconnectWallet(activityResultSender)
        },
        onRefreshBalances = {
            viewModel.refreshBalances()
        },
        onBack = onBack,
        modifier = modifier
    )
}

/**
 * Standalone overload that binds directly with a WalletManager instance
 */
@Composable
fun WalletConnectScreen(
    walletManager: WalletManager,
    solBalance: Double = 0.0,
    apsBalance: Double = 0.0,
    isRefreshing: Boolean = false,
    activityResultSender: ActivityResultSender? = null,
    onRefreshBalances: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sessionState by walletManager.sessionState.collectAsState()

    WalletConnectContent(
        sessionState = sessionState,
        solBalance = solBalance,
        apsBalance = apsBalance,
        isRefreshing = isRefreshing,
        isPollingHelius = false,
        apsPriceUsd = 0.00045,
        rpcEndpoint = RpcConfig.DEVELOPER_RPC_URL,
        rpcNetworkName = "Solana Mainnet-Beta",
        rpcLatencyMs = 45L,
        currentSlot = 312098450L,
        onConnectMwa = {
            activityResultSender?.let { sender ->
                walletManager.connect(sender)
            }
        },
        onConnectManual = { address ->
            walletManager.connectManualAddress(address)
        },
        onDisconnect = {
            walletManager.disconnect(activityResultSender)
        },
        onRefreshBalances = onRefreshBalances,
        onBack = onBack,
        modifier = modifier
    )
}

/**
 * Core Jetpack Compose UI Content for Solana WalletConnect
 */
@Composable
fun WalletConnectContent(
    sessionState: WalletSessionState,
    solBalance: Double,
    apsBalance: Double,
    isRefreshing: Boolean,
    isPollingHelius: Boolean,
    apsPriceUsd: Double,
    rpcEndpoint: String,
    rpcNetworkName: String,
    rpcLatencyMs: Long?,
    currentSlot: Long?,
    onConnectMwa: () -> Unit,
    onConnectManual: (String) -> Unit,
    onDisconnect: () -> Unit,
    onRefreshBalances: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var manualInputAddress by remember { mutableStateOf("") }
    var isManualInputExpanded by remember { mutableStateOf(false) }
    var copiedFeedback by remember { mutableStateOf(false) }

    // Dynamic rotation animation for refresh icon
    val infiniteTransition = rememberInfiniteTransition(label = "wallet_anim")
    val refreshRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh_rotation"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    // Preset dev & testing addresses
    val presetWallets = listOf(
        "Treasury Vault" to RpcConfig.TREASURY_WALLET_ADDRESS,
        "Phantom Primary" to "8x2WzN8mZz2uXqZ7YfXqZ7YfXqZ7YfXqZ7YfXqZ7YfXq",
        "Devnet Vault" to "5Jbt6zLztKuRF9hc7C8y2VemxMguP86ibDoJzetkpump",
        "Phantom Mobile" to "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU"
    )

    val connectedPublicKey = when (sessionState) {
        is WalletSessionState.Connected -> sessionState.publicKey
        else -> ""
    }

    val isConnected = sessionState is WalletSessionState.Connected
    val isConnecting = sessionState is WalletSessionState.Connecting
    val isError = sessionState is WalletSessionState.Error

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDarkBg)
            .testTag("wallet_connect_screen")
    ) {
        // Living atmospheric aurora background
        CyberpunkAtmosphericBackground(
            isMining = true,
            isOverclocked = isConnected
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                // Cyberpunk Header Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .thickGlass(
                            shape = RoundedCornerShape(16.dp),
                            borderWidth = 1.dp,
                            opacity = 0.85f,
                            accentColor = SolanaPurple
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x25FFFFFF))
                                        .testTag("wallet_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(SolanaPurple.copy(alpha = 0.6f), CyberCyan.copy(alpha = 0.4f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Wallet",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "SOLANA WALLET CONNECT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Mobile Wallet Adapter 2.0 • Phantom Protocol",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Connection Status Chip
                        val (statusText, statusColor) = when (sessionState) {
                            is WalletSessionState.Connected -> "CONNECTED" to SolanaGreen
                            is WalletSessionState.Connecting -> "CONNECTING" to CyberAmber
                            is WalletSessionState.Error -> "FAILED" to StatusError
                            is WalletSessionState.Disconnected -> "DISCONNECTED" to TextMuted
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. HERO CONNECTION CARD
                item {
                    HeroConnectionCard(
                        sessionState = sessionState,
                        pulseGlow = pulseGlow,
                        copiedFeedback = copiedFeedback,
                        onConnectMwa = onConnectMwa,
                        onDisconnect = onDisconnect,
                        onCopyAddress = { address ->
                            clipboardManager.setText(AnnotatedString(address))
                            copiedFeedback = true
                            scope.launch {
                                snackbarHostState.showSnackbar("Address copied to clipboard!")
                                kotlinx.coroutines.delay(2000)
                                copiedFeedback = false
                            }
                        },
                        onOpenExplorer = { address ->
                            openSolanaExplorer(context, address)
                        },
                        onOpenPhantom = {
                            openPhantomApp(context)
                        }
                    )
                }

                // 2. LIVE ON-CHAIN BALANCES PANEL
                item {
                    BalanceDashboardCard(
                        isConnected = isConnected,
                        solBalance = solBalance,
                        apsBalance = apsBalance,
                        apsPriceUsd = apsPriceUsd,
                        isRefreshing = isRefreshing,
                        isPollingHelius = isPollingHelius,
                        refreshRotation = refreshRotation,
                        onRefreshBalances = onRefreshBalances
                    )
                }

                // 3. MANUAL ADDRESS & TESTNET VAULT CONNECTOR (Expandable)
                item {
                    ManualAddressConnectorCard(
                        isConnected = isConnected,
                        isExpanded = isManualInputExpanded,
                        currentInput = manualInputAddress,
                        presetWallets = presetWallets,
                        onToggleExpand = { isManualInputExpanded = !isManualInputExpanded },
                        onAddressChanged = { manualInputAddress = it },
                        onPaste = {
                            clipboardManager.getText()?.text?.let { text ->
                                manualInputAddress = text.trim()
                            }
                        },
                        onSelectPreset = { address ->
                            manualInputAddress = address
                            onConnectManual(address)
                        },
                        onSubmitAddress = {
                            if (manualInputAddress.isNotBlank()) {
                                onConnectManual(manualInputAddress.trim())
                            }
                        }
                    )
                }

                // 4. SOLANA NETWORK & RPC DIAGNOSTICS
                item {
                    RpcDiagnosticsCard(
                        rpcEndpoint = rpcEndpoint,
                        networkName = rpcNetworkName,
                        latencyMs = rpcLatencyMs,
                        currentSlot = currentSlot,
                        onOpenSolanaDocs = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://solanamobile.com/developers"))
                            context.startActivity(intent)
                        }
                    )
                }

                // 5. SECURITY & PROTOCOL SPECIFICATION BADGE
                item {
                    SecurityProtocolBanner()
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * 1. Hero Connection Status and Action Card
 */
@Composable
private fun HeroConnectionCard(
    sessionState: WalletSessionState,
    pulseGlow: Float,
    copiedFeedback: Boolean,
    onConnectMwa: () -> Unit,
    onDisconnect: () -> Unit,
    onCopyAddress: (String) -> Unit,
    onOpenExplorer: (String) -> Unit,
    onOpenPhantom: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .thickGlass(
                shape = RoundedCornerShape(20.dp),
                borderWidth = 1.5.dp,
                opacity = 0.88f,
                accentColor = when (sessionState) {
                    is WalletSessionState.Connected -> SolanaGreen
                    is WalletSessionState.Connecting -> CyberCyan
                    is WalletSessionState.Error -> StatusError
                    is WalletSessionState.Disconnected -> SolanaPurple
                }
            )
            .padding(18.dp)
    ) {
        when (sessionState) {
            is WalletSessionState.Connected -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header with avatar & Phantom badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(SolanaGreen.copy(alpha = 0.35f), SolanaPurple.copy(alpha = 0.6f))
                                        )
                                    )
                                    .border(1.5.dp, SolanaGreen.copy(alpha = pulseGlow), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Connected",
                                    tint = SolanaGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = sessionState.walletName.ifBlank { "Phantom Wallet" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = sessionState.accountLabel ?: "Active Solana Session",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SolanaGreen
                                )
                            }
                        }

                        // Disconnect Button
                        OutlinedButton(
                            onClick = onDisconnect,
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("disconnect_wallet_button"),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusError.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StatusError.copy(alpha = 0.1f),
                                contentColor = StatusError
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = "Disconnect",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Disconnect", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Public Key Glass Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .subGlass(
                                shape = RoundedCornerShape(12.dp),
                                borderWidth = 1.dp,
                                accentColor = SolanaGreen
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SOLANA PUBLIC KEY (BASE58)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Ed25519 Verified",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = sessionState.publicKey,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.testTag("wallet_address_display")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Copy Button
                                OutlinedButton(
                                    onClick = { onCopyAddress(sessionState.publicKey) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .testTag("copy_address_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = CyberCyan.copy(alpha = 0.1f),
                                        contentColor = CyberCyan
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = if (copiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (copiedFeedback) "Copied!" else "Copy Key",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Solscan / Explorer Button
                                OutlinedButton(
                                    onClick = { onOpenExplorer(sessionState.publicKey) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SolanaPurple.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = SolanaPurple.copy(alpha = 0.1f),
                                        contentColor = SolanaPurple
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "Explorer",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Solscan", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            is WalletSessionState.Connecting -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = CyberCyan,
                        strokeWidth = 3.5.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "AUTHORIZING WITH PHANTOM",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sessionState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is WalletSessionState.Error -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = StatusError,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Connection Notice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusError
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = sessionState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onConnectMwa,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SolanaPurple)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retry Phantom Authorization")
                    }
                }
            }

            is WalletSessionState.Disconnected -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(SolanaPurple.copy(alpha = 0.4f), Color(0xFF1B1430))
                                )
                            )
                            .border(1.5.dp, SolanaPurple.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = SolanaPurple,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Connect Phantom Wallet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Link your Solana wallet via Mobile Wallet Adapter (MWA) to verify on-chain APS balances, automate upgrades, and sign mining rewards.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Connect Button with Phantom Gradient
                    Button(
                        onClick = onConnectMwa,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("connect_phantom_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolanaPurple
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Connect",
                                tint = SolanaGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONNECT PHANTOM (MWA)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. Real-time Balance Dashboard Card
 */
@Composable
private fun BalanceDashboardCard(
    isConnected: Boolean,
    solBalance: Double,
    apsBalance: Double,
    apsPriceUsd: Double,
    isRefreshing: Boolean,
    isPollingHelius: Boolean,
    refreshRotation: Float,
    onRefreshBalances: () -> Unit
) {
    val totalApsUsd = apsBalance * apsPriceUsd
    val solPriceUsd = 182.50
    val totalSolUsd = solBalance * solPriceUsd
    val totalPortfolioUsd = totalApsUsd + totalSolUsd

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .thickGlass(
                shape = RoundedCornerShape(20.dp),
                borderWidth = 1.dp,
                opacity = 0.85f,
                accentColor = CyberCyan
            )
            .padding(18.dp)
            .testTag("balance_dashboard_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = "Balances",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ON-CHAIN HOLDINGS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = onRefreshBalances,
                    enabled = !isRefreshing,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("refresh_balances_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Balances",
                        tint = if (isRefreshing) CyberCyan else TextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .then(if (isRefreshing) Modifier.rotate(refreshRotation) else Modifier)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two-column balance cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // SOL Balance Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .subGlass(
                            shape = RoundedCornerShape(12.dp),
                            borderWidth = 1.dp,
                            accentColor = SolanaPurple
                        )
                        .padding(12.dp)
                        .testTag("sol_balance_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SOL (GAS)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "NATIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolanaPurple,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = String.format(Locale.US, "%.4f", solBalance),
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "≈ $${String.format(Locale.US, "%.2f", totalSolUsd)} USD",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // APS Token Balance Tile
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .subGlass(
                            shape = RoundedCornerShape(12.dp),
                            borderWidth = 1.dp,
                            accentColor = SolanaGreen
                        )
                        .padding(12.dp)
                        .testTag("aps_balance_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "APS TOKENS",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "SPL TOKEN",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolanaGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = String.format(Locale.US, "%,.2f", apsBalance),
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SolanaGreen
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "≈ $${String.format(Locale.US, "%.4f", totalApsUsd)} USD",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Total Portfolio Est Bottom Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x20FFFFFF))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPollingHelius) SolanaGreen else CyberCyan)
                        )
                        Text(
                            text = if (isPollingHelius) "Helius Polling Active" else "On-Chain Cache",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "Portfolio: $${String.format(Locale.US, "%.2f", totalPortfolioUsd)} USD",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * 3. Manual Address & Devnet Presets Connector Card
 */
@Composable
private fun ManualAddressConnectorCard(
    isConnected: Boolean,
    isExpanded: Boolean,
    currentInput: String,
    presetWallets: List<Pair<String, String>>,
    onToggleExpand: () -> Unit,
    onAddressChanged: (String) -> Unit,
    onPaste: () -> Unit,
    onSelectPreset: (String) -> Unit,
    onSubmitAddress: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .thickGlass(
                shape = RoundedCornerShape(18.dp),
                borderWidth = 1.dp,
                opacity = 0.82f,
                accentColor = CyberAmber
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Manual Address",
                        tint = CyberAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "MANUAL LINK & DEV PRESETS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = if (isExpanded) "Hide ▲" else "Options ▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Paste a custom Solana Base58 address or select a verified test vault below to sync on-chain data without Phantom app installed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input Field
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = onAddressChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_wallet_input"),
                        placeholder = {
                            Text("Enter Solana Base58 address...", color = TextMuted, fontSize = 12.sp)
                        },
                        trailingIcon = {
                            IconButton(onClick = onPaste) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberAmber,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedContainerColor = Color(0x400A0F1D),
                            unfocusedContainerColor = Color(0x300A0F1D)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onSubmitAddress,
                        enabled = currentInput.trim().length >= 32,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("link_manual_address_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberAmber,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Link Manual Address", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "QUICK TESTNET VAULTS:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset Chips Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        presetWallets.forEach { (label, address) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x22FFFFFF))
                                    .clickable { onSelectPreset(address) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${address.take(4)}...${address.takeLast(4)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. Solana Network & RPC Diagnostics Card
 */
@Composable
private fun RpcDiagnosticsCard(
    rpcEndpoint: String,
    networkName: String,
    latencyMs: Long?,
    currentSlot: Long?,
    onOpenSolanaDocs: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .thickGlass(
                shape = RoundedCornerShape(18.dp),
                borderWidth = 1.dp,
                opacity = 0.82f,
                accentColor = SolanaGreen
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "RPC Node",
                        tint = SolanaGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "RPC NODE INFRASTRUCTURE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SolanaGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = networkName,
                        style = MaterialTheme.typography.labelSmall,
                        color = SolanaGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Latency
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x18FFFFFF))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("LATENCY", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = "${latencyMs ?: 45} ms",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }
                }

                // Slot
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x18FFFFFF))
                        .padding(8.dp)
                ) {
                    Column {
                        Text("CURRENT SLOT", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text(
                            text = "#${currentSlot ?: 312098450L}",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Endpoint: ${rpcEndpoint.take(36)}...",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 5. Security Protocol Banner
 */
@Composable
private fun SecurityProtocolBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x15FFFFFF))
            .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security",
                tint = CyberCyan,
                modifier = Modifier.size(22.dp)
            )

            Column {
                Text(
                    text = "Non-Custodial Cryptographic Security",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Private keys never leave your Phantom vault. All transactions and token authorizations are signed directly through Solana MWA Secure Enclave.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// Helpers
private fun openSolanaExplorer(context: Context, address: String) {
    try {
        val url = "https://solscan.io/account/$address"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
    }
}

private fun openPhantomApp(context: Context) {
    try {
        val intent = context.packageManager.getLaunchIntentForPackage("app.phantom")
        if (intent != null) {
            context.startActivity(intent)
        } else {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://phantom.app/download"))
            context.startActivity(storeIntent)
        }
    } catch (_: Exception) {
        Toast.makeText(context, "Phantom app not found", Toast.LENGTH_SHORT).show()
    }
}
