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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.MiningRig
import com.example.data.model.MiningRigsCatalog
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun UpgradeRigDialog(
    currentLevel: Int,
    availableMinedAps: Double,
    apsPerUsd: Double = 338174.39,
    walletAddress: String = "",
    onChainApsBalance: Double = 0.0,
    isPollingHelius: Boolean = false,
    isRefreshingWallet: Boolean = false,
    onRefreshWallet: (() -> Unit)? = null,
    onUpgrade: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var rigToConfirm by remember { mutableStateOf<MiningRig?>(null) }

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
                .testTag("upgrade_rig_dialog")
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
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "Rig Store",
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "MINING RIG DEPOT",
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

                Spacer(modifier = Modifier.height(6.dp))

                // Real Business APY & Buyback Guarantee Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F2236))
                        .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REAL-WORLD ASSET (RWA) BACKING",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                            Text(
                                text = "0.60% / Bulan",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = SolanaGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Dana mesin dikelola di bisnis riil (7% APY). Pemain menerima 0.60%/bulan tetap, dan 0.10%/bulan dialokasikan untuk Dana Pembelian Kembali (Buyback) token APS saat harga turun.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextSecondary,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Helius RPC Wallet Polling & Verification Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF131D2D))
                        .border(1.dp, if (onChainApsBalance >= 338174.0) SolanaGreen.copy(alpha = 0.6f) else SolanaPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                        .testTag("helius_polling_verification_box")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isPollingHelius) SolanaGreen else CyberAmber)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPollingHelius) "HELIUS RPC LIVE POLLING" else "HELIUS RPC ORACLE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPollingHelius) SolanaGreen else CyberAmber
                                )
                            }

                            if (onRefreshWallet != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onRefreshWallet() }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    if (isRefreshingWallet) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(10.dp),
                                            strokeWidth = 1.5.dp,
                                            color = CyberCyan
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Refresh Helius",
                                            tint = CyberCyan,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Poll Now",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = CyberCyan
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Phantom On-Chain APS:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextSecondary
                                )
                                Text(
                                    text = if (walletAddress.isNotBlank()) "${walletAddress.take(4)}...${walletAddress.takeLast(4)}" else "Wallet not connected",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${String.format(Locale.US, "%,.2f", onChainApsBalance)} APS",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (onChainApsBalance >= 338174.0) SolanaGreen else CyberAmber
                                )
                                if (onChainApsBalance >= 338174.0) {
                                    Text(
                                        text = "✓ 338,174 Upgrade Verified",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = SolanaGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "Need ≥ 338,174 APS for Tier 1",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Available Balance
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F1726))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MINED BALANCE:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.2f", availableMinedAps)} APS",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        color = SolanaGreen
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rig Cards (Levels 1 - 5)
                MiningRigsCatalog.RIGS.forEach { rig ->
                    val isCurrent = rig.level == currentLevel
                    val isUnlocked = rig.level < currentLevel
                    val isNext = rig.level == currentLevel + 1
                    val hasMinedSufficient = availableMinedAps >= rig.upgradeCostAps
                    val hasOnChainSufficient = onChainApsBalance >= rig.upgradeCostAps
                    val canAfford = hasMinedSufficient || hasOnChainSufficient

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    isCurrent -> Color(0xFF16253D)
                                    isNext && canAfford -> Color(0xFF1A1F30)
                                    else -> Color(0xFF0E1320)
                                }
                            )
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = when {
                                    isCurrent -> CyberCyan
                                    isNext && canAfford -> SolanaGreen
                                    isNext -> SolanaPurple
                                    else -> Color(0xFF1E283C)
                                },
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "LVL ${rig.level}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) CyberCyan else SolanaPurple
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = rig.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                if (isCurrent) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CyberCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "EQUIPPED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = CyberCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = rig.description,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = TextSecondary,
                                lineHeight = 14.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    if (rig.upgradeCostAps > 0) {
                                        Text(
                                            text = "HASIL: ${String.format(Locale.US, "%,.2f", rig.monthlyYieldAps)} APS/bln (${rig.monthlyYieldPercent}%/bln)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontFamily = FontFamily.Monospace,
                                            color = SolanaGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "RATE: ${String.format(Locale.US, "%,.2f", rig.dailyYieldAps)} APS/hari • ${rig.powerWatts}W",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontFamily = FontFamily.Monospace,
                                            color = TextSecondary
                                        )
                                    } else {
                                        Text(
                                            text = "HASIL: Free Starter Faucet Mode",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontFamily = FontFamily.Monospace,
                                            color = SolanaGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "RATE: ~86.40 APS/hari • ${rig.powerWatts}W",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontFamily = FontFamily.Monospace,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                if (rig.level > currentLevel) {
                                    Button(
                                        onClick = { rigToConfirm = rig },
                                        enabled = canAfford,
                                        modifier = Modifier
                                            .height(34.dp)
                                            .testTag("upgrade_btn_lvl_${rig.level}"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (canAfford) SolanaGreen else Color(0xFF1D2838),
                                            contentColor = if (canAfford) Color.Black else TextMuted
                                        )
                                    ) {
                                        Text(
                                            text = if (canAfford) {
                                                if (hasOnChainSufficient) "UPGRADE (${String.format(Locale.US, "%,.0f", rig.upgradeCostAps)} APS) [VERIFIED]"
                                                else "UPGRADE (${String.format(Locale.US, "%,.0f", rig.upgradeCostAps)} APS)"
                                            } else {
                                                "INSUFFICIENT (${String.format(Locale.US, "%,.0f", rig.upgradeCostAps)} APS)"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = FontWeight.Bold
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

    rigToConfirm?.let { rig ->
        UpgradeConfirmationDialog(
            rig = rig,
            availableBalance = maxOf(availableMinedAps, onChainApsBalance),
            apsPerUsd = apsPerUsd,
            walletAddress = walletAddress,
            onChainApsBalance = onChainApsBalance,
            isPollingHelius = isPollingHelius,
            onConfirmUpgrade = {
                onUpgrade(rig.level)
                rigToConfirm = null
                onDismiss()
            },
            onDismiss = { rigToConfirm = null }
        )
    }
}
