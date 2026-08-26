package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Stream
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.StatusMining
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-performance, rich Jetpack Compose Animation Banner for active APS token mining.
 *
 * Provides vibrant visual feedback with:
 * - Matrix hash stream particle waves floating upward
 * - Quantum plasma orb with pulsing particle rings & revolving token nodes
 * - Live cryptographic nonce matrix stream (Hex / SHA256 hashes)
 * - Dynamic color schemes (Overclocked Cyber Pink/Amber vs Active Solana Green/Cyan)
 */
@Composable
fun ActiveMiningFeedbackBanner(
    isMining: Boolean,
    isOverclocked: Boolean,
    hashrateMhs: Double,
    minedRatePerSec: Double,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "activeMiningAnim")

    // Particle flow progression
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isOverclocked) 900 else 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particlePhase"
    )

    // Quantum core rotation
    val coreRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isOverclocked) 1600 else 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "coreRotation"
    )

    // Pulse alpha for neon beacon
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isOverclocked) 350 else 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Wave ripple radius
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isOverclocked) 800 else 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleProgress"
    )

    val primaryColor = when {
        !isMining -> Color(0xFF6E7681)
        isOverclocked -> CyberPink
        else -> SolanaGreen
    }

    val secondaryColor = when {
        !isMining -> Color(0xFF30363D)
        isOverclocked -> NeonYellow
        else -> CyberCyan
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .thickGlass(
                shape = RoundedCornerShape(16.dp),
                borderWidth = if (isMining) 1.5.dp else 1.dp,
                opacity = if (isMining) 0.65f else 0.35f,
                accentColor = if (isMining) primaryColor else null
            )
            .testTag("active_mining_feedback_banner")
    ) {
        // Background animated particle canvas
        if (isMining) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                if (width <= 0 || height <= 0) return@Canvas

                // Ambient glow behind the banner
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.20f * pulseAlpha),
                            secondaryColor.copy(alpha = 0.08f * pulseAlpha),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.18f, height * 0.5f),
                        radius = width * 0.35f
                    )
                )

                // Draw upward rising quantum particle dots
                val particleCount = if (isOverclocked) 28 else 16
                val random = Random(42)
                for (i in 0 until particleCount) {
                    val startX = random.nextFloat() * width
                    val speed = 0.6f + random.nextFloat() * 0.8f
                    val sizeDot = 2.5f + random.nextFloat() * 3.5f
                    val currentY = (height + 20f) - ((particlePhase * speed + (i.toFloat() / particleCount)) % 1f) * (height + 40f)

                    val particleAlpha = (1f - (currentY / height).coerceIn(0f, 1f)) * 0.7f * pulseAlpha
                    val color = if (i % 2 == 0) primaryColor else secondaryColor

                    drawCircle(
                        color = color.copy(alpha = particleAlpha),
                        radius = sizeDot,
                        center = Offset(startX, currentY)
                    )
                }

                // Grid cyber scanlines
                val stepY = 16f
                var y = 0f
                while (y < height) {
                    drawLine(
                        color = secondaryColor.copy(alpha = 0.04f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    y += stepY
                }
            }
        }

        // Foreground Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Interactive Quantum Reactor Core & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quantum Visual Core Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CyberDarkBg.copy(alpha = 0.85f))
                        .border(
                            width = 1.dp,
                            color = primaryColor.copy(alpha = pulseAlpha),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isMining) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val center = Offset(w / 2, h / 2)

                            // Expanding concentric ripple rings
                            val rippleRadius = (w * 0.48f) * rippleProgress
                            val rippleAlpha = (1f - rippleProgress) * 0.8f
                            drawCircle(
                                color = primaryColor.copy(alpha = rippleAlpha),
                                radius = rippleRadius,
                                center = center,
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // Revolving orbital electron beads
                            rotate(coreRotation, center) {
                                val orbitR = w * 0.35f
                                for (angleDeg in listOf(0, 120, 240)) {
                                    val rad = angleDeg * PI / 180.0
                                    val ox = center.x + orbitR * cos(rad).toFloat()
                                    val oy = center.y + orbitR * sin(rad).toFloat()
                                    drawCircle(
                                        color = secondaryColor,
                                        radius = 3.dp.toPx(),
                                        center = Offset(ox, oy)
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = if (isOverclocked) Icons.Default.Bolt else Icons.Default.ElectricBolt,
                            contentDescription = "Active Core",
                            tint = if (isOverclocked) CyberPink else SolanaGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Standby Core",
                            tint = TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Status & Hash Info
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                        Text(
                            text = when {
                                !isMining -> "RIG IN STANDBY"
                                isOverclocked -> "OVERCLOCKED MINING (2.5x)"
                                else -> "ACTIVELY MINING APS"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryColor,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (isMining) {
                            "${String.format(Locale.US, "%.2f", hashrateMhs)} MH/s • ${String.format(Locale.US, "+%.4f", minedRatePerSec)} APS/s"
                        } else {
                            "Press 'START MINING' to begin hash generation"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontFamily = FontFamily.Monospace,
                        color = if (isMining) Color.White else TextSecondary,
                        fontWeight = if (isMining) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            // Right side: Active Energy Pulse Pill
            if (isMining) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.15f * pulseAlpha))
                        .border(1.dp, primaryColor.copy(alpha = 0.5f * pulseAlpha), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stream,
                            contentDescription = "Live Flow",
                            tint = primaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}
