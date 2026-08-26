package com.example.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MiningRig
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun UpgradeSuccessAnimationDialog(
    rig: MiningRig,
    apsPerUsd: Double = 338174.39,
    onDismiss: () -> Unit
) {
    // Animation states
    val scaleAnim = remember { Animatable(0.2f) }
    val alphaAnim = remember { Animatable(0f) }
    val particleProgress = remember { Animatable(0f) }
    val ringPulse = remember { Animatable(0.8f) }

    // Continuous rotation for quantum holographic elements
    val infiniteTransition = rememberInfiniteTransition(label = "quantumGlow")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Particle seed generation
    val particles = remember {
        List(32) {
            val angle = Random.nextDouble(0.0, Math.PI * 2)
            val distance = Random.nextDouble(60.0, 180.0)
            val size = Random.nextFloat() * 5f + 2f
            val color = if (Random.nextBoolean()) SolanaGreen else CyberCyan
            Triple(Offset((cos(angle) * distance).toFloat(), (sin(angle) * distance).toFloat()), size, color)
        }
    }

    LaunchedEffect(Unit) {
        // Step 1: Pop in with spring/overshoot
        alphaAnim.animateTo(1f, tween(300))
        scaleAnim.animateTo(1f, tween(600, easing = FastOutSlowInEasing))

        // Step 2: Explode energy particles
        particleProgress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
        ringPulse.animateTo(1.5f, tween(800, easing = FastOutSlowInEasing))
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scaleAnim.value)
                .thickGlass(
                    shape = RoundedCornerShape(28.dp),
                    borderWidth = 2.dp,
                    opacity = 0.85f,
                    accentColor = SolanaGreen
                )
                .testTag("upgrade_success_animation_dialog")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Particle Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height * 0.28f)

                    // Expanding Energy Shockwave
                    drawCircle(
                        color = SolanaGreen.copy(alpha = (1f - particleProgress.value).coerceAtLeast(0f) * 0.4f),
                        radius = 70.dp.toPx() * ringPulse.value,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Particles explosion
                    particles.forEach { (offset, pSize, color) ->
                        val currentPos = center + offset * particleProgress.value
                        val pAlpha = (1f - particleProgress.value).coerceAtLeast(0f)
                        drawCircle(
                            color = color.copy(alpha = pAlpha),
                            radius = pSize,
                            center = currentPos
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Holographic Ring & Reactor Icon
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer spinning dashed circuit ring
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(rotation)
                        ) {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    listOf(SolanaGreen, CyberCyan, Color.Transparent, SolanaGreen)
                                ),
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        }

                        // Inner glowing core
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            SolanaGreen.copy(alpha = 0.35f * glowAlpha),
                                            Color(0xFF0D1B2A)
                                        )
                                    )
                                )
                                .border(1.5.dp, SolanaGreen.copy(alpha = glowAlpha), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Upgrade Success",
                                tint = SolanaGreen,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        // Tiny Floating Sparkle
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(CyberCyan)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "HARDWARE UPGRADED!",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )

                    Text(
                        text = "Transaction Confirmed & Core Synchronized",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = SolanaGreen
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Machine Specs Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F1A2C))
                            .border(1.dp, Color(0xFF1E355B), RoundedCornerShape(16.dp))
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
                                        text = "NEW ACTIVE RIG",
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SolanaGreen.copy(alpha = 0.2f))
                                        .border(1.dp, SolanaGreen, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "LEVEL ${rig.level}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        fontWeight = FontWeight.Black,
                                        color = SolanaGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Yield & Hashrate Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF080E18))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Mining Speed",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${rig.hashrateMhs} MH/s",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberCyan
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Daily Rate",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "+${String.format(Locale.US, "%,.2f", rig.dailyYieldAps)} APS",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = SolanaGreen
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Monthly Yield",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${rig.monthlyYieldPercent}%/mo",
                                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberAmber
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // RWA Sustainability Confirmation Note
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0C2422))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SolanaGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Upgrade cost (${String.format(Locale.US, "%,.0f", rig.upgradeCostAps)} APS) deployed to RWA Treasury pool with 0.1% buyback protection.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = SolanaGreen,
                            lineHeight = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Continue Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dismiss_upgrade_success_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SolanaGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "START MINING AT LEVEL ${rig.level}",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
