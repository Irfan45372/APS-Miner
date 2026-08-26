package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MiningRig
import com.example.data.model.RpcConfig
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun UpgradeConfirmationDialog(
    rig: MiningRig,
    availableBalance: Double,
    apsPerUsd: Double = 338174.39,
    walletAddress: String = "",
    onChainApsBalance: Double = 0.0,
    isPollingHelius: Boolean = false,
    onConfirmUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    val costUsd = if (apsPerUsd > 0) rig.upgradeCostAps / apsPerUsd else 0.0
    val monthlyUsd = if (apsPerUsd > 0) rig.monthlyYieldAps / apsPerUsd else 0.0
    val isVerifiedByHelius = onChainApsBalance >= rig.upgradeCostAps

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .thickGlass(
                    shape = RoundedCornerShape(24.dp),
                    borderWidth = 1.5.dp,
                    opacity = 0.85f,
                    accentColor = SolanaGreen
                )
                .testTag("upgrade_confirmation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SolanaGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = SolanaGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "UPGRADE CONFIRMATION",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Sustainable RWA Yield Protocol",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = SolanaGreen
                            )
                        }
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

                // Rig Summary Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0F1B2E))
                        .border(1.dp, Color(0xFF223554), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "TARGET HARDWARE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextSecondary
                                )
                                Text(
                                    text = rig.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SolanaPurple.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "LEVEL ${rig.level}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = SolanaPurple
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Cost Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF080D16))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Upgrade Cost:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format(Locale.US, "%,.0f", rig.upgradeCostAps)} APS",
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = SolanaGreen
                                )
                                Text(
                                    text = "≈ $${String.format(Locale.US, "%.2f", costUsd)} USD",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontFamily = FontFamily.Monospace,
                                    color = CyberAmber
                                )
                            }
                        }

                        if (walletAddress.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isVerifiedByHelius) Color(0xFF0E281E) else Color(0xFF281C10))
                                    .border(
                                        1.dp,
                                        if (isVerifiedByHelius) SolanaGreen.copy(alpha = 0.5f) else CyberAmber.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isVerifiedByHelius) Icons.Default.CheckCircle else Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (isVerifiedByHelius) SolanaGreen else CyberAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isVerifiedByHelius) "Helius Verified Phantom Balance:" else "Phantom On-Chain Balance:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (isVerifiedByHelius) SolanaGreen else CyberAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${String.format(Locale.US, "%,.2f", onChainApsBalance)} APS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isVerifiedByHelius) SolanaGreen else CyberAmber
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF090E18))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Solana Treasury Destination:",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = TextMuted
                            )
                            Text(
                                text = "${RpcConfig.TREASURY_WALLET_ADDRESS.take(6)}...${RpcConfig.TREASURY_WALLET_ADDRESS.takeLast(6)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontFamily = FontFamily.Monospace,
                                color = CyberCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Projected Returns Box (0.6% Monthly)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0A1E1E))
                        .border(1.dp, SolanaGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = SolanaGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PROJECTED EARNINGS (0.60% / MONTH)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = SolanaGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Monthly Yield:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary)
                                Text(
                                    text = "+${String.format(Locale.US, "%,.2f", rig.monthlyYieldAps)} APS",
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = SolanaGreen
                                )
                                Text(
                                    text = "≈ $${String.format(Locale.US, "%.4f", monthlyUsd)}/mo",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = CyberAmber
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Daily Rate:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary)
                                Text(
                                    text = "+${String.format(Locale.US, "%,.2f", rig.dailyYieldAps)} APS",
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.6f", rig.yieldPerSecond)} APS/s",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Investment Sustainability & Buyback Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131826))
                        .border(1.dp, Color(0xFF243048), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "INVESTMENT SUSTAINABILITY SUMMARY",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Point 1: 7% APY Real World Business
                        SustainabilityBulletPoint(
                            icon = Icons.Default.LocalAtm,
                            title = "7.0% APY Real-World Asset (RWA) Deployment",
                            description = "User upgrade capital is deployed into verified real business operations generating 7.0% annual yield (~0.58% - 0.70% / month)."
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Point 2: 0.6% User Distribution
                        SustainabilityBulletPoint(
                            icon = Icons.Default.AutoGraph,
                            title = "0.60% / Month User Distribution",
                            description = "You receive a transparent, non-inflationary 0.60% monthly return distributed continually in real-time."
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Point 3: 0.1% Buyback Reserve
                        SustainabilityBulletPoint(
                            icon = Icons.Default.Savings,
                            title = "0.10% / Month Automated Buyback Reserve",
                            description = "The remaining 0.10% monthly yield is locked in the Treasury Buyback Pool to purchase APS tokens if market prices dip, protecting all holders."
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("cancel_upgrade_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "CANCEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onConfirmUpgrade()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(44.dp)
                            .testTag("confirm_upgrade_action_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolanaGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CONFIRM UPGRADE",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SustainabilityBulletPoint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1B2438)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SolanaGreen,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                color = TextSecondary,
                lineHeight = 12.sp
            )
        }
    }
}
