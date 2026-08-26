package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.RpcConfig
import com.example.data.model.SolanaNetwork
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RpcConfigDialog(
    config: RpcConfig,
    isTestingRpc: Boolean,
    latencyMs: Long?,
    currentSlot: Long?,
    onSaveCustomKey: (String) -> Unit,
    onUseDeveloperRpc: () -> Unit,
    onSelectNetwork: (SolanaNetwork) -> Unit,
    onTestPing: () -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyInput by remember(config.customApiKey) { mutableStateOf(config.customApiKey) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .thickGlass(
                    shape = RoundedCornerShape(24.dp),
                    borderWidth = 1.5.dp,
                    opacity = 0.85f,
                    accentColor = CyberCyan
                )
                .testTag("rpc_config_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "RPC Node",
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "HELIUS RPC / ORACLE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Network Selection (Mainnet vs Devnet)
                Text(
                    text = "TARGET SOLANA CLUSTER",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SolanaNetwork.values().forEach { network ->
                        val selected = config.network == network
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) SolanaPurple.copy(alpha = 0.3f) else Color(0xFF131826))
                                .border(
                                    1.dp,
                                    if (selected) SolanaPurple else Color(0xFF202A40),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onSelectNetwork(network) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = network.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active RPC Status Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C1322))
                        .border(1.dp, Color(0xFF1D2C4A), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (config.isConnected) SolanaGreen else StatusError)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (config.useDeveloperRpc) "DEVELOPER RPC & ORACLE" else "CUSTOM HELIUS KEY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (config.useDeveloperRpc) CyberAmber else CyberCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (latencyMs != null) "Latency: ${latencyMs}ms • Slot #${currentSlot ?: "Syncing"}" else "Testing endpoint connection...",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onTestPing,
                        enabled = !isTestingRpc,
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (isTestingRpc) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = CyberCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Ping Test",
                                tint = CyberCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 1: Developer RPC Button & Oracle Explanations
                Text(
                    text = "OPTION 1: DEVELOPER RPC & ORACLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131826))
                        .border(
                            1.dp,
                            if (config.useDeveloperRpc) CyberAmber else Color(0xFF202A40),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Oracle",
                                tint = CyberAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Zero Setup • Protected Backend RPC",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The developer RPC key resides securely on the backend. It uses the real-time APS Oracle rate to calculate Developer fee parity.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = TextSecondary,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Real-time Oracle Live Price Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF090E18))
                                .border(1.dp, CyberAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "LIVE APS ORACLE RATE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = CyberAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "1 USD = ${String.format(java.util.Locale.US, "%,.2f", config.apsPerUsd)} APS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontFamily = FontFamily.Monospace,
                                        color = SolanaGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Dev RPC Fee ($10 equivalent):",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%,.2f", config.developerFeeAps)} APS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onUseDeveloperRpc,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("use_developer_rpc_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (config.useDeveloperRpc) Color(0xFF2A2312) else CyberAmber,
                                contentColor = if (config.useDeveloperRpc) CyberAmber else Color.Black
                            ),
                            border = if (config.useDeveloperRpc) androidx.compose.foundation.BorderStroke(1.dp, CyberAmber) else null
                        ) {
                            Icon(
                                imageVector = if (config.useDeveloperRpc) Icons.Default.CheckCircle else Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (config.useDeveloperRpc) "DEVELOPER RPC ACTIVE" else "USE DEVELOPER RPC KEY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: Custom Helius API Key Input Form
                Text(
                    text = "OPTION 2: CUSTOM HELIUS API KEY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enter your personal Helius RPC API key for prioritized block latency and dedicated APS token read throughput.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = {
                        Text(
                            text = "e.g. 0f5056b5-a8f0-4c80-b768-xxxxxx",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = TextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Key",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF090E18),
                        unfocusedContainerColor = Color(0xFF090E18),
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = Color(0xFF1E2D4A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_helius_api_key_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        onSaveCustomKey(apiKeyInput)
                    },
                    enabled = apiKeyInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_custom_rpc_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFF121B2B),
                        disabledContentColor = TextMuted
                    )
                ) {
                    Text(
                        text = "APPLY CUSTOM HELIUS RPC",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // APS Mint & Treasury Info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF070B13))
                        .border(1.dp, Color(0xFF152033), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "APS SPL TOKEN MINT ADDRESS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = TextSecondary
                        )
                        Text(
                            text = RpcConfig.APS_MINT_ADDRESS,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontFamily = FontFamily.Monospace,
                            color = SolanaGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "OFFICIAL SOLANA TREASURY WALLET",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = TextSecondary
                        )
                        Text(
                            text = RpcConfig.TREASURY_WALLET_ADDRESS,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontFamily = FontFamily.Monospace,
                            color = CyberCyan
                        )
                    }
                }
            }
        }
    }
}
