package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MiningProcessStatus
import java.util.Locale

@Composable
fun DashboardPanel(
    minedAps: Double,
    hashrateMhs: Double,
    isMining: Boolean,
    processStatus: MiningProcessStatus = MiningProcessStatus.ACTIVE,
    sessionDurationFormatted: String = "00:00",
    idleDurationFormatted: String = "00:00",
    currentSessionMinedAps: Double = 0.0,
    isOverclocked: Boolean,
    overclockSecondsRemaining: Int,
    overclockCooldownSeconds: Int,
    isClaiming: Boolean,
    walletConnected: Boolean,
    apsPerUsd: Double = 344850.3,
    onToggleMining: () -> Unit,
    onActivateOverclock: () -> Unit,
    onClaimClick: () -> Unit,
    onOpenSessionHistory: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dashPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val estimatedUsd = if (apsPerUsd > 0) minedAps / apsPerUsd else 0.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .thickGlass(
                shape = RoundedCornerShape(20.dp),
                borderWidth = 1.2.dp,
                opacity = 0.50f,
                accentColor = if (isOverclocked) CyberPink else if (isMining) SolanaGreen else null
            )
            .padding(18.dp)
            .testTag("dashboard_panel")
    ) {
        // Top Header: Earned APS Indicator & Hashrate Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACCUMULATED BALANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format(Locale.US, "%,.2f", minedAps),
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isMining) CyberCyan else Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "APS",
                        style = MaterialTheme.typography.titleMedium,
                        color = SolanaGreen,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = "≈ $${String.format(Locale.US, "%.4f", estimatedUsd)} USD (Oracle)",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontFamily = FontFamily.Monospace,
                    color = CyberAmber,
                    fontWeight = FontWeight.Bold
                )
            }

            // Live Hashrate Badge
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .subGlass(
                        shape = RoundedCornerShape(12.dp),
                        borderWidth = 1.dp,
                        accentColor = if (isOverclocked) CyberPink else if (isMining) SolanaGreen else null
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "HASHRATE",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Speed",
                        tint = if (isOverclocked) CyberPink else if (isMining) SolanaGreen else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isMining) "${String.format(Locale.US, "%.2f", hashrateMhs)} MH/s" else "0.00 MH/s (IDLE)",
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverclocked) CyberPink else if (isMining) SolanaGreen else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center Cyberpunk Neon Radial Mining Gauge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .subGlass(
                    shape = RoundedCornerShape(16.dp),
                    borderWidth = 1.dp,
                    opacity = 0.25f
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            NeonMiningProgressBar(
                isMining = isMining,
                isOverclocked = isOverclocked,
                hashrateMhs = hashrateMhs,
                processStatus = processStatus,
                sizeDp = 156.dp,
                strokeWidth = 9.dp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Session Timer & Idle State Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .subGlass(
                    shape = RoundedCornerShape(10.dp),
                    borderWidth = 1.dp,
                    opacity = 0.35f
                )
                .clickable { onOpenSessionHistory?.invoke() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (processStatus) {
                                MiningProcessStatus.ACTIVE -> SolanaGreen
                                MiningProcessStatus.OVERCLOCKED -> CyberPink
                                MiningProcessStatus.IDLE -> CyberAmber
                                MiningProcessStatus.PAUSED -> Color.Gray
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (processStatus) {
                        MiningProcessStatus.ACTIVE -> "MINING SESSION:"
                        MiningProcessStatus.OVERCLOCKED -> "BOOSTED SESSION:"
                        MiningProcessStatus.IDLE -> "IDLE STANDBY:"
                        MiningProcessStatus.PAUSED -> "PAUSED STATE:"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMining) sessionDurationFormatted else idleDurationFormatted,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isMining) SolanaGreen else CyberAmber
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isMining && currentSessionMinedAps > 0) {
                    Text(
                        text = "+${String.format(Locale.US, "%,.2f", currentSessionMinedAps)} APS",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Session History",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row (Mining Toggle & Overclock Boost)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Start/Pause Mining Button
            Button(
                onClick = onToggleMining,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("mining_toggle_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMining) Color(0xFF1B2A4A) else SolanaGreen,
                    contentColor = if (isMining) CyberCyan else Color.Black
                ),
                border = if (isMining) androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f)) else null
            ) {
                Icon(
                    imageVector = if (isMining) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = "Toggle",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMining) "ENTER IDLE MODE" else "START MINING",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // Overclock Nitro Button
            Button(
                onClick = onActivateOverclock,
                enabled = !isOverclocked && overclockCooldownSeconds == 0,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("overclock_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOverclocked) CyberPink else Color(0xFF2E1238),
                    contentColor = if (isOverclocked) Color.White else CyberPink,
                    disabledContainerColor = Color(0xFF181B24),
                    disabledContentColor = TextMuted
                ),
                border = if (isOverclocked) null else androidx.compose.foundation.BorderStroke(1.dp, CyberPink.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Overclock",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        isOverclocked -> "${overclockSecondsRemaining}s BOOST"
                        overclockCooldownSeconds > 0 -> "COOL (${overclockCooldownSeconds}s)"
                        else -> "2X OVERCLOCK"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Claim Button
        Button(
            onClick = onClaimClick,
            enabled = !isClaiming && minedAps >= 0.000001,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("claim_aps_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SolanaPurple,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF1E1730),
                disabledContentColor = Color(0xFF6E6482)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (isClaiming) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = CyberCyan,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "TRANSACTING WITH SOLANA MAINNET...",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Claim",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (!walletConnected) "CONNECT WALLET TO CLAIM"
                    else if (minedAps < 0.000001) "MINING IN PROGRESS..."
                    else "CLAIM ${String.format(Locale.US, "%.6f", minedAps)} APS TO WALLET",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
