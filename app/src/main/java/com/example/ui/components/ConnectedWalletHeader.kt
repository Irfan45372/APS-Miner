package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SolanaGreen
import com.example.ui.theme.SolanaPurple
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextSecondary

@Composable
fun ConnectedWalletHeader(
    walletAddress: String,
    onWalletClicked: () -> Unit,
    onDisconnectClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shortAddress = if (walletAddress.length > 8) {
        "${walletAddress.take(4)}...${walletAddress.takeLast(4)}"
    } else {
        walletAddress
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .thickGlass(
                shape = RoundedCornerShape(14.dp),
                borderWidth = 1.dp,
                opacity = 0.50f,
                accentColor = SolanaGreen
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("connected_wallet_header")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Side: Connected Wallet Info (clickable to view details/balance)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onWalletClicked() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SolanaPurple.copy(alpha = 0.6f), SolanaGreen.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet Icon",
                        tint = SolanaGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SolanaGreen)
                        )
                        Text(
                            text = "PHANTOM CONNECTED",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                            color = SolanaGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = shortAddress,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Side: Red Disconnect Button
            OutlinedButton(
                onClick = onDisconnectClicked,
                modifier = Modifier
                    .height(30.dp)
                    .testTag("btn_disconnect_wallet"),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, StatusError.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = StatusError.copy(alpha = 0.15f),
                    contentColor = StatusError
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LinkOff,
                    contentDescription = "Disconnect",
                    tint = StatusError,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Disconnect",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    color = StatusError
                )
            }
        }
    }
}
