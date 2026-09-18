package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.MiningRig
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RigVisualFrame(
    rig: MiningRig,
    isMining: Boolean,
    currentTempC: Double,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fan_rotation")
    val fanAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isMining) 800 else 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fan_spin"
    )

    val neonPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isMining) 600 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        if (isMining) CyberGreen.copy(alpha = neonPulse) else DarkBorder,
                        if (isMining) CyberCyan.copy(alpha = neonPulse) else DarkBorder,
                        if (isMining) NeonPurple.copy(alpha = neonPulse) else DarkBorder
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LEVEL ${rig.level} • ${rig.modelCode}",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rig.name,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isMining) CyberGreen.copy(alpha = 0.2f) else DarkBorder)
                        .border(1.dp, if (isMining) CyberGreen else TextSecondary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isMining) "HASHING ACTIVE" else "STANDBY",
                        color = if (isMining) CyberGreen else TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val hasImage = rig.level == 1 || rig.imageUrl.isNotBlank()

            if (hasImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF090D14))
                        .border(
                            1.dp,
                            if (isMining) CyberCyan.copy(alpha = 0.5f * neonPulse) else DarkBorder,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient radial glow behind the node
                    if (isMining) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            CyberGreen.copy(alpha = 0.22f * neonPulse),
                                            CyberCyan.copy(alpha = 0.10f * neonPulse),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    val imageModel = if (rig.imageUrl.isNotBlank()) {
                        ImageRequest.Builder(LocalContext.current)
                            .data(rig.imageUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.aps_micro_node)
                            .error(R.drawable.aps_micro_node)
                            .build()
                    } else {
                        R.drawable.aps_micro_node
                    }

                    AsyncImage(
                        model = imageModel,
                        contentDescription = rig.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )

                    // Cyberpunk scanning laser overlay during mining
                    if (isMining) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val scanProgress = (fanAngle / 360f)
                            val scanY = size.height * scanProgress
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        CyberGreen.copy(alpha = 0.7f * neonPulse),
                                        CyberCyan.copy(alpha = 0.8f * neonPulse),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(0f, scanY),
                                end = Offset(size.width, scanY),
                                strokeWidth = 2.5f
                            )
                        }
                    }

                    // Hardware tag badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(0.5.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SOLANA L1 NODE",
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Hashrate badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(0.5.dp, if (isMining) CyberGreen.copy(alpha = 0.6f) else DarkBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${rig.hashrateMhs} MH/s",
                            color = if (isMining) CyberGreen else TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val fanRadius = (height / 2f) - 10f

                    drawLine(
                        color = DarkBorder,
                        start = Offset(20f, height / 2),
                        end = Offset(width - 20f, height / 2),
                        strokeWidth = 2f
                    )

                    val fan1Center = Offset(width * 0.3f, height / 2)
                    drawFan(this, fan1Center, fanRadius, fanAngle, isMining, neonPulse)

                    val fan2Center = Offset(width * 0.7f, height / 2)
                    drawFan(this, fan2Center, fanRadius, -fanAngle, isMining, neonPulse)

                    if (isMining) {
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(CyberGreen, CyberCyan),
                                startX = fan1Center.x + fanRadius,
                                endX = fan2Center.x - fanRadius
                            ),
                            start = Offset(fan1Center.x + fanRadius, height / 2),
                            end = Offset(fan2Center.x - fanRadius, height / 2),
                            strokeWidth = 4f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "POWER: ${rig.powerWatts}W",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "TEMP: ${String.format("%.1f", currentTempC)}°C",
                    color = if (currentTempC > 65) DangerRed else CyberGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "FAN: ${if (isMining) "100%" else "20%"}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private fun drawFan(
    scope: androidx.compose.ui.graphics.drawscope.DrawScope,
    center: Offset,
    radius: Float,
    rotationDeg: Float,
    isMining: Boolean,
    pulse: Float
) {
    scope.drawCircle(
        color = if (isMining) CyberCyan.copy(alpha = 0.7f * pulse) else DarkBorder,
        radius = radius,
        center = center,
        style = Stroke(width = 3f)
    )

    scope.drawCircle(
        color = if (isMining) CyberGreen else DarkBorder,
        radius = radius * 0.25f,
        center = center
    )

    scope.rotate(rotationDeg, pivot = center) {
        val numBlades = 7
        for (i in 0 until numBlades) {
            val angleRad = Math.toRadians((i * (360.0 / numBlades))).toFloat()
            val bladeEnd = Offset(
                center.x + (radius * 0.85f * cos(angleRad)),
                center.y + (radius * 0.85f * sin(angleRad))
            )
            drawLine(
                color = if (isMining) TextPrimary.copy(alpha = 0.8f) else TextSecondary,
                start = center,
                end = bladeEnd,
                strokeWidth = 4f
            )
        }
    }
}
