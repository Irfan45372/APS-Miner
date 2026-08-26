package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SolanaGreen
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated Electric Lightning Arcs and Plasma Sparks Overlay.
 *
 * Renders procedural branching lightning bolts, plasma ionization glow nodes,
 * and high-voltage electrical spark arcs across the APS Micro Node Alpha chassis.
 */
@Composable
fun ElectricLightningOverlay(
    isMining: Boolean,
    isOverclocked: Boolean,
    modifier: Modifier = Modifier,
    isMicroNode: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "electricLightningAnim")

    // Ultra-fast jitter/flicker step for lightning frame generation (every 60-120ms)
    val flickerStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isOverclocked) 1200 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flickerStep"
    )

    // Lightning discharge pulse brightness
    val lightningPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isOverclocked) 280 else 550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lightningPulse"
    )

    // Secondary plasma wave rotation
    val plasmaAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "plasmaAngle"
    )

    val primaryColor = when {
        isOverclocked -> CyberPink
        isMining -> CyberCyan
        else -> CyberCyan.copy(alpha = 0.5f)
    }

    val secondaryColor = when {
        isOverclocked -> NeonYellow
        isMining -> Color(0xFF64FFDA) // Electric mint / Solana Cyan
        else -> SolanaGreen.copy(alpha = 0.4f)
    }

    val coreColor = Color.White

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        if (width <= 0 || height <= 0) return@Canvas

        // Use integer seed from flickerStep to create lively jumping electric arcs
        val frameIndex = (flickerStep * 3.5f).toInt()
        val random = Random(frameIndex * 1337 + (if (isOverclocked) 777 else 123))

        // Probability of lightning discharge occurring in this frame
        val boltChance = if (isOverclocked) 0.95 else if (isMining) 0.85 else 0.40

        if (random.nextDouble() < boltChance) {
            // Number of concurrent lightning arcs
            val boltCount = if (isOverclocked) 4 else if (isMining) 3 else 1

            for (b in 0 until boltCount) {
                // Key contact anchor points across the Micro Node chassis
                val (startPoint, endPoint) = getLightningEndpoints(b, width, height, random)

                // Generate Jagged Lightning Path with branching
                drawLightningBolt(
                    start = startPoint,
                    end = endPoint,
                    random = random,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    coreColor = coreColor,
                    pulseAlpha = lightningPulse,
                    isOverclocked = isOverclocked
                )
            }
        }

        // Draw Ionization Spark Particles jumping around the rig microchips
        val sparkCount = if (isOverclocked) 14 else if (isMining) 8 else 3
        for (s in 0 until sparkCount) {
            val sparkSeed = frameIndex + s * 41
            val sparkRandom = Random(sparkSeed)
            val sx = width * (0.15f + sparkRandom.nextFloat() * 0.70f)
            val sy = height * (0.20f + sparkRandom.nextFloat() * 0.65f)
            val sparkRadius = (1.5f + sparkRandom.nextFloat() * 3.5f) * (if (isOverclocked) 1.4f else 1.0f)
            val sparkAlpha = (0.4f + sparkRandom.nextFloat() * 0.6f) * lightningPulse

            // Spark Glow
            drawCircle(
                color = (if (isOverclocked) NeonYellow else primaryColor).copy(alpha = sparkAlpha * 0.7f),
                radius = sparkRadius * 2.8f,
                center = Offset(sx, sy)
            )
            // Spark Core
            drawCircle(
                color = coreColor.copy(alpha = sparkAlpha),
                radius = sparkRadius,
                center = Offset(sx, sy)
            )
        }

        // Ambient Corner Corona Rings (Circuit capacitor discharge)
        if (isMining) {
            val cornerAlpha = if (isOverclocked) 0.22f else 0.12f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = cornerAlpha * lightningPulse),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = width * 0.45f
                ),
                radius = width * 0.45f,
                center = Offset(width * 0.5f, height * 0.5f)
            )
        }
    }
}

/**
 * Calculates anchor point pairs on the Micro Node for natural-looking circuit arcing.
 */
private fun getLightningEndpoints(
    index: Int,
    width: Float,
    height: Float,
    random: Random
): Pair<Offset, Offset> {
    return when (index % 4) {
        0 -> {
            // Top Left to Center CPU Node
            Offset(width * (0.15f + random.nextFloat() * 0.2f), height * (0.20f + random.nextFloat() * 0.15f)) to
            Offset(width * (0.50f + (random.nextFloat() - 0.5f) * 0.2f), height * (0.50f + (random.nextFloat() - 0.5f) * 0.2f))
        }
        1 -> {
            // Center CPU Node to Bottom Right Capacitor
            Offset(width * (0.50f + (random.nextFloat() - 0.5f) * 0.2f), height * (0.48f + (random.nextFloat() - 0.5f) * 0.2f)) to
            Offset(width * (0.75f + random.nextFloat() * 0.15f), height * (0.70f + random.nextFloat() * 0.15f))
        }
        2 -> {
            // Top Right to Left Heat Sink Arc
            Offset(width * (0.80f - random.nextFloat() * 0.15f), height * (0.22f + random.nextFloat() * 0.18f)) to
            Offset(width * (0.25f + random.nextFloat() * 0.2f), height * (0.65f + random.nextFloat() * 0.2f))
        }
        else -> {
            // Diagonal Across Node Power Core
            Offset(width * (0.20f + random.nextFloat() * 0.15f), height * (0.75f - random.nextFloat() * 0.15f)) to
            Offset(width * (0.80f - random.nextFloat() * 0.15f), height * (0.30f + random.nextFloat() * 0.15f))
        }
    }
}

/**
 * Draws a multi-pass jagged electric lightning bolt with branching sub-arcs.
 */
private fun DrawScope.drawLightningBolt(
    start: Offset,
    end: Offset,
    random: Random,
    primaryColor: Color,
    secondaryColor: Color,
    coreColor: Color,
    pulseAlpha: Float,
    isOverclocked: Boolean
) {
    val segments = 8
    val points = ArrayList<Offset>(segments + 1)
    points.add(start)

    val dx = end.x - start.x
    val dy = end.y - start.y
    val normalX = -dy
    val normalY = dx
    val normalLen = kotlin.math.sqrt(normalX * normalX + normalY * normalY).coerceAtLeast(1f)
    val unitNormalX = normalX / normalLen
    val unitNormalY = normalY / normalLen

    val maxJitter = (size.width * 0.08f).coerceAtLeast(8f)

    for (i in 1 until segments) {
        val t = i.toFloat() / segments
        val baseX = start.x + dx * t
        val baseY = start.y + dy * t
        val jitter = (random.nextFloat() - 0.5f) * 2f * maxJitter * (1f - kotlin.math.abs(t - 0.5f) * 0.8f)

        val px = baseX + unitNormalX * jitter
        val py = baseY + unitNormalY * jitter
        points.add(Offset(px, py))
    }
    points.add(end)

    val mainPath = Path()
    mainPath.moveTo(points[0].x, points[0].y)
    for (i in 1 until points.size) {
        mainPath.lineTo(points[i].x, points[i].y)
    }

    // Branching Sub-Bolts
    val branchPath = Path()
    if (points.size > 4 && random.nextBoolean()) {
        val branchStart = points[points.size / 2]
        branchPath.moveTo(branchStart.x, branchStart.y)
        var bx = branchStart.x
        var by = branchStart.y
        val branchLen = 3
        for (j in 0 until branchLen) {
            bx += (random.nextFloat() - 0.5f) * maxJitter * 2.2f + dx * 0.12f
            by += (random.nextFloat() - 0.5f) * maxJitter * 2.2f + dy * 0.12f
            branchPath.lineTo(bx, by)
        }
    }

    val alpha = (0.75f * pulseAlpha).coerceIn(0f, 1f)

    // Layer 1: Wide Diffuse Plasma Glow
    drawPath(
        path = mainPath,
        color = primaryColor.copy(alpha = alpha * 0.40f),
        style = Stroke(
            width = if (isOverclocked) 9f else 6.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Layer 2: Medium Electric Ionization Beam
    drawPath(
        path = mainPath,
        color = secondaryColor.copy(alpha = alpha * 0.80f),
        style = Stroke(
            width = if (isOverclocked) 4.5f else 3.2f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Layer 3: Razor Sharp White Core Spark
    drawPath(
        path = mainPath,
        color = coreColor.copy(alpha = alpha * 0.95f),
        style = Stroke(
            width = if (isOverclocked) 2.0f else 1.4f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Draw Branch Path
    if (!branchPath.isEmpty) {
        drawPath(
            path = branchPath,
            color = primaryColor.copy(alpha = alpha * 0.60f),
            style = Stroke(
                width = 3.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = branchPath,
            color = coreColor.copy(alpha = alpha * 0.85f),
            style = Stroke(
                width = 1.2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    // Plasma burst ionization at endpoints
    drawCircle(
        color = secondaryColor.copy(alpha = alpha * 0.8f),
        radius = if (isOverclocked) 6f else 4.5f,
        center = start
    )
    drawCircle(
        color = coreColor.copy(alpha = alpha),
        radius = if (isOverclocked) 2.5f else 2.0f,
        center = start
    )

    drawCircle(
        color = primaryColor.copy(alpha = alpha * 0.8f),
        radius = if (isOverclocked) 6f else 4.5f,
        center = end
    )
    drawCircle(
        color = coreColor.copy(alpha = alpha),
        radius = if (isOverclocked) 2.5f else 2.0f,
        center = end
    )
}
