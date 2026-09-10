package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FeedConfig
import com.example.model.MarketFeedSource
import com.example.ui.theme.*

@Composable
fun MarketFeedModal(
    isOpen: Boolean,
    feedConfig: FeedConfig,
    onDismiss: () -> Unit,
    onSelectSource: (MarketFeedSource) -> Unit,
    onUpdateCredentials: (angelKey: String, angelClientCode: String, angelTotp: String, upstoxToken: String) -> Unit,
    onTestAndConnect: () -> Unit,
    onToggleConnection: () -> Unit
) {
    if (!isOpen) return

    var selectedSource by remember(feedConfig.source) { mutableStateOf(feedConfig.source) }
    var angelApiKey by remember(feedConfig.angelOneApiKey) { mutableStateOf(feedConfig.angelOneApiKey) }
    var angelClientCode by remember(feedConfig.angelOneClientCode) { mutableStateOf(feedConfig.angelOneClientCode) }
    var angelTotp by remember(feedConfig.angelOneTotpSecret) { mutableStateOf(feedConfig.angelOneTotpSecret) }
    var upstoxToken by remember(feedConfig.upstoxAccessToken) { mutableStateOf(feedConfig.upstoxAccessToken) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(4.dp)
                .testTag("market_feed_modal"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (feedConfig.isConnected) GainGreen.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = if (feedConfig.isConnected) GainGreen else LossRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Market Feed Gateway",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Angel One SmartAPI & Upstox Developer API",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Live Telemetry Status Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (feedConfig.isConnected) GainGreen.copy(alpha = 0.4f) else BorderSubtle
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (feedConfig.isConnected) GainGreen else LossRed)
                                        )
                                        Text(
                                            text = if (feedConfig.isConnected) "LIVE STREAMING ACTIVE" else "FEED PAUSED",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (feedConfig.isConnected) GainGreen else LossRed
                                        )
                                    }

                                    Text(
                                        text = "${feedConfig.latencyMs} ms",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (feedConfig.latencyMs < 60) GainGreen else AccentCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = feedConfig.lastStatusMessage,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TelemetryStatItem("Tracked Stocks", "115 NSE/BSE")
                                    TelemetryStatItem("Packets Rx", "${feedConfig.totalTicksReceived}")
                                    TelemetryStatItem("Last Tick", feedConfig.lastSyncTime)
                                }
                            }
                        }
                    }

                    // 2. Select API Feed Source
                    item {
                        Text(
                            text = "Select Active Market Feed Provider",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Angel One Provider Card
                            val isAngelSelected = selectedSource == MarketFeedSource.ANGEL_ONE
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedSource = MarketFeedSource.ANGEL_ONE
                                        onSelectSource(MarketFeedSource.ANGEL_ONE)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAngelSelected) AccentBlue.copy(alpha = 0.15f) else DarkSurfaceVariant
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isAngelSelected) AccentBlue else BorderSubtle
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Angel One",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAngelSelected) AccentCyan else TextPrimary
                                        )
                                        if (isAngelSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = AccentBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "SmartAPI Connect",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "NSE & BSE Cash LTP, OHLC, and Level-2 Depth",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Upstox Provider Card
                            val isUpstoxSelected = selectedSource == MarketFeedSource.UPSTOX
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedSource = MarketFeedSource.UPSTOX
                                        onSelectSource(MarketFeedSource.UPSTOX)
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUpstoxSelected) AccentBlue.copy(alpha = 0.15f) else DarkSurfaceVariant
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isUpstoxSelected) AccentBlue else BorderSubtle
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Upstox",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUpstoxSelected) AccentCyan else TextPrimary
                                        )
                                        if (isUpstoxSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = AccentBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Developer API v2",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "High-throughput JSON REST quotes & WebSocket streams",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // 3. Credentials Configuration
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = if (selectedSource == MarketFeedSource.ANGEL_ONE)
                                        "Angel One SmartAPI Configuration"
                                    else
                                        "Upstox Developer API (v2) Configuration",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Keys can also be configured via Secrets panel in AI Studio (ANGEL_ONE_API_KEY, UPSTOX_ACCESS_TOKEN).",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                if (selectedSource == MarketFeedSource.ANGEL_ONE) {
                                    OutlinedTextField(
                                        value = angelApiKey,
                                        onValueChange = {
                                            angelApiKey = it
                                            onUpdateCredentials(angelApiKey, angelClientCode, angelTotp, upstoxToken)
                                        },
                                        label = { Text("SmartAPI Key (X-PrivateKey)", fontSize = 11.sp) },
                                        placeholder = { Text("e.g. your_smartapi_key", fontSize = 11.sp, color = TextMuted) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = BorderSubtle,
                                            focusedContainerColor = DarkSurfaceVariant,
                                            unfocusedContainerColor = DarkSurfaceVariant,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = angelClientCode,
                                            onValueChange = {
                                                angelClientCode = it
                                                onUpdateCredentials(angelApiKey, angelClientCode, angelTotp, upstoxToken)
                                            },
                                            label = { Text("Client Code", fontSize = 11.sp) },
                                            placeholder = { Text("e.g. S12345", fontSize = 11.sp, color = TextMuted) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentBlue,
                                                unfocusedBorderColor = BorderSubtle,
                                                focusedContainerColor = DarkSurfaceVariant,
                                                unfocusedContainerColor = DarkSurfaceVariant,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )

                                        OutlinedTextField(
                                            value = angelTotp,
                                            onValueChange = {
                                                angelTotp = it
                                                onUpdateCredentials(angelApiKey, angelClientCode, angelTotp, upstoxToken)
                                            },
                                            label = { Text("TOTP Secret / Token", fontSize = 11.sp) },
                                            placeholder = { Text("Session JWT/TOTP", fontSize = 11.sp, color = TextMuted) },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = AccentBlue,
                                                unfocusedBorderColor = BorderSubtle,
                                                focusedContainerColor = DarkSurfaceVariant,
                                                unfocusedContainerColor = DarkSurfaceVariant,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                } else {
                                    OutlinedTextField(
                                        value = upstoxToken,
                                        onValueChange = {
                                            upstoxToken = it
                                            onUpdateCredentials(angelApiKey, angelClientCode, angelTotp, upstoxToken)
                                        },
                                        label = { Text("Upstox Access Token (OAuth2 Bearer)", fontSize = 11.sp) },
                                        placeholder = { Text("Paste active JWT access token from developer.upstox.com", fontSize = 11.sp, color = TextMuted) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AccentBlue,
                                            unfocusedBorderColor = BorderSubtle,
                                            focusedContainerColor = DarkSurfaceVariant,
                                            unfocusedContainerColor = DarkSurfaceVariant,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Test & Connection Action Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onTestAndConnect,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_connect_feed_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                enabled = !feedConfig.isConnecting
                            ) {
                                if (feedConfig.isConnecting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Validating Gateway...", fontSize = 12.sp)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.NetworkCheck,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test & Connect Gateway", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = onToggleConnection,
                                modifier = Modifier.testTag("toggle_feed_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (feedConfig.isConnected) LossRed else GainGreen
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (feedConfig.isConnected) LossRed.copy(alpha = 0.5f) else GainGreen.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = if (feedConfig.isConnected) "Pause" else "Resume",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 5. Gateway Activity Console Logs
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "LIVE FEED ACTIVITY LOGS",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentCyan
                                    )
                                    Text(
                                        text = "PORT: 443 (TLS v1.3)",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                feedConfig.feedLogs.takeLast(6).forEach { logLine ->
                                    Text(
                                        text = "• $logLine",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary,
                                        lineHeight = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryStatItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.5.sp, color = TextMuted)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
