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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom High-Fidelity Animated Neon Circular Progress Bar.
 *
 * Implements a multi-layered cyberpunk HUD radial gauge with:
 * - Ambient neon bloom halo diffusion
 * - Dynamic color gradient based on active/overclocked/idle mining state
 * - Rotating telemetry orbital scanner & radial tick graduation marks
 * - Leading neon spark bead with trailing light emission
 * - Monospace digital telemetry HUD in the core center
 */
@Composable
fun NeonMiningProgressBar(
    isMining: Boolean,
    isOverclocked: Boolean,
    hashrateMhs: Double,
    processStatus: MiningProcessStatus = MiningProcessStatus.ACTIVE,
    manualProgress: Float? = null,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 150.dp,
    strokeWidth: Dp = 9.dp,
    showCenterDetails: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonMiningLoop")

    // Rotation angle for the scanner sweep / particle head
    val rotationSpeed = when {
        isOverclocked -> 1200
        isMining -> 2600
        else -> 7000
    }

    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuousRotation"
    )

    // Pulse alpha for the neon bloom and halo
    val neonGlowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isOverclocked) 0.55f else 0.35f,
        targetValue = if (isOverclocked) 1.0f else 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isOverclocked) 450 else 900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neonGlowAlpha"
    )

    // Secondary pulse for internal radar ring
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarPulse"
    )

    // Dynamic Color Palette for Neon Tubes
    val neonPrimary = when {
        isOverclocked -> CyberPink
        isMining -> CyberCyan
        else -> CyberAmber.copy(alpha = 0.6f)
    }

    val neonSecondary = when {
        isOverclocked -> NeonYellow
        isMining -> SolanaGreen
        else -> SolanaPurple.copy(alpha = 0.5f)
    }

    val neonTertiary = when {
        isOverclocked -> Color(0xFFFF0055)
        isMining -> SolanaPurple
        else -> Color(0xFF1E283C)
    }

    val sweepAngle = manualProgress?.let { (it.coerceIn(0f, 1f) * 360f) }
        ?: if (isMining) (240f + (if (isOverclocked) 60f else 0f)) else 90f

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("neon_mining_progress_bar"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val strokePx = strokeWidth.toPx()
            val radius = (size.minDimension - strokePx * 3) / 2f
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            // 1. Draw Background Outer Tick Marks (Cyber HUD Dial)
            val numTicks = 36
            val tickRadiusOuter = radius + strokePx * 1.2f
            val tickRadiusInner = radius + strokePx * 0.7f

            for (i in 0 until numTicks) {
                val angleDeg = i * (360f / numTicks)
                val angleRad = (angleDeg - 90f) * (PI / 180f).toFloat()
                val isMajorTick = (i % 3 == 0)

                val innerR = if (isMajorTick) tickRadiusInner - 2.dp.toPx() else tickRadiusInner
                val start = Offset(
                    centerOffset.x + innerR * cos(angleRad),
                    centerOffset.y + innerR * sin(angleRad)
                )
                val end = Offset(
                    centerOffset.x + tickRadiusOuter * cos(angleRad),
                    centerOffset.y + tickRadiusOuter * sin(angleRad)
                )

                val tickColor = when {
                    isMajorTick && isOverclocked -> CyberPink.copy(alpha = 0.7f)
                    isMajorTick && isMining -> CyberCyan.copy(alpha = 0.6f)
                    isMajorTick -> Color(0xFF2C3E60)
                    else -> Color(0xFF141C2B)
                }

                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = if (isMajorTick) 1.5.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 2. Draw Track Bed (Deep dark neon groove)
            drawCircle(
                color = Color(0xFF090D18),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawCircle(
                color = Color(0xFF162238),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokePx * 0.5f, cap = StrokeCap.Round)
            )

            // 3. Draw Ambient Glowing Radar Mesh (Inner circle)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        neonPrimary.copy(alpha = if (isMining) 0.08f * neonGlowAlpha else 0.02f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = radius * 0.95f
                ),
                radius = radius * 0.95f,
                center = centerOffset
            )

            // 4. Draw Rotating Neon Progress Arcs
            val startAngle = if (manualProgress != null) -90f else (continuousRotation - 90f)

            // Pass A: Outer Diffused Glow Layer (Wide soft halo)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        neonTertiary.copy(alpha = 0.05f),
                        neonSecondary.copy(alpha = 0.25f * neonGlowAlpha),
                        neonPrimary.copy(alpha = 0.55f * neonGlowAlpha),
                        neonPrimary.copy(alpha = 0.75f * neonGlowAlpha)
                    ),
                    center = centerOffset
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokePx * 2.2f, cap = StrokeCap.Round)
            )

            // Pass B: Mid Neon Tube Diffusion Layer
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        neonTertiary.copy(alpha = 0.1f),
                        neonSecondary.copy(alpha = 0.6f * neonGlowAlpha),
                        neonPrimary.copy(alpha = 0.9f * neonGlowAlpha),
                        neonPrimary.copy(alpha = 1.0f)
                    ),
                    center = centerOffset
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokePx * 1.3f, cap = StrokeCap.Round)
            )

            // Pass C: Sharp Core Laser Core (High-intensity inner beam)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        neonTertiary.copy(alpha = 0.2f),
                        neonSecondary,
                        Color.White.copy(alpha = 0.9f),
                        Color.White
                    ),
                    center = centerOffset
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokePx * 0.6f, cap = StrokeCap.Round)
            )

            // 5. Draw Leading Photon Bead (Glowing spark at head of arc)
            val headAngleRad = (startAngle + sweepAngle) * (PI / 180f).toFloat()
            val beadCenter = Offset(
                centerOffset.x + radius * cos(headAngleRad),
                centerOffset.y + radius * sin(headAngleRad)
            )

            // Bead Outer Glow
            drawCircle(
                color = (if (isOverclocked) NeonYellow else CyberCyan).copy(alpha = 0.5f * neonGlowAlpha),
                radius = strokePx * 1.4f,
                center = beadCenter
            )
            // Bead Mid Halo
            drawCircle(
                color = if (isOverclocked) CyberPink else SolanaGreen,
                radius = strokePx * 0.85f,
                center = beadCenter
            )
            // Bead White Core
            drawCircle(
                color = Color.White,
                radius = strokePx * 0.45f,
                center = beadCenter
            )
        }

        // Center Monospace HUD Telemetry Readout
        if (showCenterDetails) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                // Top Mini Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                isOverclocked -> CyberPink.copy(alpha = 0.2f)
                                isMining -> SolanaGreen.copy(alpha = 0.15f)
                                else -> Color(0xFF1B2436)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isOverclocked -> Icons.Default.FlashOn
                            isMining -> Icons.Default.ElectricBolt
                            else -> Icons.Default.PowerSettingsNew
                        },
                        contentDescription = "Mining State",
                        tint = when {
                            isOverclocked -> CyberPink
                            isMining -> SolanaGreen
                            else -> CyberAmber
                        },
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = when {
                            isOverclocked -> "2X NITRO"
                            isMining -> "MINING"
                            else -> "STANDBY"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        ),
                        color = when {
                            isOverclocked -> CyberPink
                            isMining -> SolanaGreen
                            else -> CyberAmber
                        }
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Core Hashrate Display
                Text(
                    text = if (isMining) String.format(Locale.US, "%.1f", hashrateMhs) else "0.0",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "MH/S HASHRATE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    ),
                    color = if (isMining) CyberCyan else TextMuted
                )

                // Bottom Engine indicator
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(width = 6.dp, height = 3.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(
                                    if (isMining) {
                                        if (isOverclocked) CyberPink else SolanaGreen
                                    } else {
                                        Color(0xFF222C3E)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}
