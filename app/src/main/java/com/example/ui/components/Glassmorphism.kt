package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPink
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple

/**
 * High-end thick frosted glass styling for cyberpunk UI surfaces.
 * Translucent deep glass backing with crisp specular rim refraction.
 */
fun Modifier.thickGlass(
    shape: Shape = RoundedCornerShape(18.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null,
    accentGlow: Color? = null,
    accentColor: Color? = null,
    opacity: Float = 0.55f
): Modifier {
    val activeAccent = accentColor ?: accentGlow
    val glassBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF141F36).copy(alpha = opacity + 0.15f),
            Color(0xFF090F1C).copy(alpha = opacity + 0.05f),
            Color(0xFF050912).copy(alpha = opacity + 0.20f)
        )
    )

    val glassBorderBrush = if (borderColor != null) {
        Brush.linearGradient(
            listOf(
                borderColor.copy(alpha = 0.85f),
                borderColor.copy(alpha = 0.35f),
                Color(0x35FFFFFF),
                borderColor.copy(alpha = 0.65f)
            )
        )
    } else if (activeAccent != null) {
        Brush.linearGradient(
            listOf(
                activeAccent.copy(alpha = 0.85f),
                Color(0x40FFFFFF),
                activeAccent.copy(alpha = 0.30f),
                Color(0x15FFFFFF)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0x60FFFFFF),
                Color(0x20FFFFFF),
                Color(0x4000F0FF),
                Color(0x10FFFFFF),
                Color(0x359945FF)
            )
        )
    }

    return this
        .clip(shape)
        .background(glassBg)
        .border(borderWidth, glassBorderBrush, shape)
}

/**
 * Inner secondary glass layer for sub-components, metric chips, and readouts.
 */
fun Modifier.subGlass(
    shape: Shape = RoundedCornerShape(12.dp),
    borderWidth: Dp = 1.dp,
    accentColor: Color? = null,
    opacity: Float = 0.35f
): Modifier {
    val subBg = Brush.verticalGradient(
        listOf(
            Color(0xFF182542).copy(alpha = opacity),
            Color(0xFF0B1120).copy(alpha = opacity * 0.8f)
        )
    )
    val subBorder = if (accentColor != null) {
        Brush.linearGradient(
            listOf(
                accentColor.copy(alpha = 0.70f),
                Color(0x20FFFFFF),
                accentColor.copy(alpha = 0.30f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0x35FFFFFF),
                Color(0x10FFFFFF),
                Color(0x2035507E)
            )
        )
    }

    return this
        .clip(shape)
        .background(subBg)
        .border(borderWidth, subBorder, shape)
}

/**
 * Living ambient aurora backdrop that breathes behind the thick transparent glass surfaces.
 */
@Composable
fun CyberpunkAtmosphericBackground(
    isMining: Boolean,
    isOverclocked: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambientAtmosphere")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isOverclocked) 2000 else 4500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val auroraShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auroraShift"
    )

    val primaryGlow = when {
        isOverclocked -> CyberPink
        isMining -> CyberCyan
        else -> SolanaPurple
    }

    val secondaryGlow = when {
        isOverclocked -> Color(0xFFFFE600)
        isMining -> SolanaGreen
        else -> CyberCyan
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF04060A))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-right Solana Purple nebula orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryGlow.copy(alpha = if (isOverclocked) 0.35f else 0.22f),
                        primaryGlow.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(width * (0.80f + auroraShift * 0.001f), height * 0.18f),
                    radius = width * 0.70f * pulseScale
                ),
                radius = width * 0.70f * pulseScale,
                center = Offset(width * 0.80f, height * 0.18f)
            )

            // Center-left Electric Cyan aurora orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        secondaryGlow.copy(alpha = if (isOverclocked) 0.30f else 0.18f),
                        secondaryGlow.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * (0.45f + auroraShift * 0.0008f)),
                    radius = width * 0.65f * (2f - pulseScale)
                ),
                radius = width * 0.65f * (2f - pulseScale),
                center = Offset(width * 0.15f, height * 0.45f)
            )

            // Bottom-right Solana Green core glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SolanaGreen.copy(alpha = if (isMining) 0.20f else 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.75f, height * 0.78f),
                    radius = width * 0.55f * pulseScale
                ),
                radius = width * 0.55f * pulseScale,
                center = Offset(width * 0.75f, height * 0.78f)
            )

            // Subtle Cyberpunk Grid Lines in the deep background behind the glass
            val gridStep = 48.dp.toPx()
            val gridColor = Color(0x0CFFFFFF)
            var x = 0f
            while (x < width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = 0f
            while (y < height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }
        }
    }
}
