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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberPink
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LogType
import com.example.ui.viewmodel.TerminalLog

@Composable
fun TerminalConsoleView(
    logs: List<TerminalLog>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .thickGlass(
                shape = RoundedCornerShape(16.dp),
                borderWidth = 1.dp,
                opacity = 0.55f
            )
            .padding(12.dp)
            .testTag("terminal_console_view")
    ) {
        // Terminal Window Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // macOS/Linux window dots
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusError))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberAmber))
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SolanaGreen))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "APS-NODE-DAEMON // LIVE TELEMETRY",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Logs Output
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp),
            reverseLayout = false
        ) {
            items(logs) { log ->
                val logColor = when (log.type) {
                    LogType.INFO -> TextSecondary
                    LogType.SUCCESS -> SolanaGreen
                    LogType.WARNING -> CyberAmber
                    LogType.ERROR -> StatusError
                    LogType.MINING -> CyberCyan
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "[${log.timestamp}]",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "> ${log.message}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontFamily = FontFamily.Monospace,
                        color = logColor
                    )
                }
            }
        }
    }
}
