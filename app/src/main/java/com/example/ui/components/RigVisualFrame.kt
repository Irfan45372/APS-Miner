package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MiningRig
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.StatusMining
import java.util.Locale

@Composable
fun RigVisualFrame(
    rig: MiningRig,
    isMining: Boolean,
    isOverclocked: Boolean,
    temperatureC: Double,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isOverclocked) 1.0f else 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isOverclocked) 400 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val borderColor = when {
        isOverclocked -> CyberPink.copy(alpha = glowAlpha)
        isMining -> CyberCyan.copy(alpha = glowAlpha)
        else -> Color(0xFF2A364F)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .thickGlass(
                shape = RoundedCornerShape(20.dp),
                borderWidth = if (isOverclocked) 2.dp else 1.2.dp,
                opacity = 0.50f,
                accentColor = if (isOverclocked) CyberPink else if (isMining) SolanaGreen else null
            )
            .testTag("rig_visual_frame")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar inside Rig Frame
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .subGlass(
                            shape = RoundedCornerShape(8.dp),
                            borderWidth = 1.dp,
                            accentColor = SolanaPurple
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Rig Model",
                        tint = SolanaGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LVL ${rig.level} • ${rig.modelCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Active Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .subGlass(
                            shape = RoundedCornerShape(8.dp),
                            borderWidth = 1.dp,
                            accentColor = if (isOverclocked) CyberPink else if (isMining) SolanaGreen else null
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOverclocked) CyberPink
                                else if (isMining) SolanaGreen
                                else Color(0xFF6E7681)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOverclocked) "OVERCLOCKED" else if (isMining) "ONLINE" else "STANDBY",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverclocked) CyberPink else if (isMining) SolanaGreen else Color(0xFF8B949E),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Machine Image Frame - Crystal Clear Showcase
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .subGlass(
                        shape = RoundedCornerShape(16.dp),
                        borderWidth = 1.dp,
                        opacity = 0.20f,
                        accentColor = if (isOverclocked) CyberPink else if (isMining) CyberCyan else null
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(rig.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = rig.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )

                // Electric Lightning & Plasma Spark Discharge Animation for APS Micro Node Alpha & Mining Rigs
                ElectricLightningOverlay(
                    isMining = isMining,
                    isOverclocked = isOverclocked,
                    isMicroNode = rig.level == 1 || rig.modelCode.contains("MN-01") || rig.name.contains("Micro Node", ignoreCase = true),
                    modifier = Modifier.fillMaxSize()
                )

                // Cyber scanline overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawContent()
                            if (isMining) {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            CyberCyan.copy(alpha = 0.05f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rig Name & Title
            Text(
                text = rig.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Telemetry Gauges (Power, Temp, Oracle Yield)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .subGlass(
                        shape = RoundedCornerShape(10.dp),
                        borderWidth = 1.dp,
                        opacity = 0.35f
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Yield Rate
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "BASE YIELD",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color(0xFF8B949E)
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.6f", rig.yieldPerSecond)}/s",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = SolanaGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Temp Gauge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = "Temp",
                            tint = if (temperatureC > 65) CyberPink else CyberCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "TEMP",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFF8B949E)
                        )
                    }
                    Text(
                        text = "${String.format(Locale.US, "%.1f", temperatureC)}°C",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (temperatureC > 65) CyberPink else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Power Consumption
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Power",
                            tint = NeonYellow,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "POWER",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFF8B949E)
                        )
                    }
                    Text(
                        text = "${rig.powerWatts} W",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
