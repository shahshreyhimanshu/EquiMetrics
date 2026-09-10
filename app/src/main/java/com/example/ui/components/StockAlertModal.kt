package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AlertChannel
import com.example.model.Stock
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAlertModal(
    isOpen: Boolean,
    stock: Stock,
    isPro: Boolean,
    onDismiss: () -> Unit,
    onSaveAlert: (condition: String, target: Double, channel: AlertChannel, contact: String) -> Unit,
    onUpgradeRequired: () -> Unit
) {
    if (!isOpen) return

    val conditions = listOf(
        "Price Crosses Above",
        "Price Crosses Below",
        "52-Week High Breakout",
        "Day Move > 3%",
        "Volume Spike (> 2x)"
    )
    var selectedCondition by remember { mutableStateOf(conditions.first()) }
    var targetPriceText by remember(stock.symbol) {
        mutableStateOf(String.format(Locale.US, "%.2f", stock.currentPrice * 1.02))
    }
    var selectedChannel by remember { mutableStateOf(if (isPro) AlertChannel.WHATSAPP else AlertChannel.PUSH) }
    var contactInfo by remember { mutableStateOf("+91 98765 43210") }
    var expandedConditionDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("stock_alert_modal"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Create Price & Event Alert",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${stock.symbol} • LTP ₹${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Condition Selector
                Text("TRIGGER CONDITION", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedConditionDropdown,
                    onExpandedChange = { expandedConditionDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedCondition,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedConditionDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedConditionDropdown,
                        onDismissRequest = { expandedConditionDropdown = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond, fontSize = 12.sp, color = TextPrimary) },
                                onClick = {
                                    selectedCondition = cond
                                    expandedConditionDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target Price Input
                Text("TARGET PRICE (₹)", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = targetPriceText,
                    onValueChange = { targetPriceText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                )

                // Quick percentage offsets
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(-5.0, -2.0, 1.0, 3.0, 5.0).forEach { pct ->
                        val sign = if (pct > 0) "+$pct%" else "$pct%"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                .clickable {
                                    val calc = stock.currentPrice * (1.0 + (pct / 100.0))
                                    targetPriceText = String.format(Locale.US, "%.2f", calc)
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sign, fontSize = 9.sp, color = if (pct > 0) GainGreen else LossRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Delivery Channel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DELIVERY CHANNEL", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    if (!isPro) {
                        Text("⚡ Pro: Instant WhatsApp / Telegram", fontSize = 9.sp, color = AccentBlue)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AlertChannel.values().forEach { channel ->
                        val isSelected = selectedChannel == channel
                        val isLockedForFree = !isPro && channel != AlertChannel.PUSH

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentBlueSubtle else DarkSurfaceVariant)
                                .border(1.dp, if (isSelected) AccentBlue else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isLockedForFree) {
                                        onUpgradeRequired()
                                    } else {
                                        selectedChannel = channel
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = when (channel) {
                                        AlertChannel.PUSH -> Icons.Default.Notifications
                                        AlertChannel.WHATSAPP -> Icons.Default.Chat
                                        AlertChannel.TELEGRAM -> Icons.Default.Send
                                    },
                                    contentDescription = null,
                                    tint = when (channel) {
                                        AlertChannel.WHATSAPP -> Color(0xFF25D366)
                                        AlertChannel.TELEGRAM -> Color(0xFF229ED9)
                                        AlertChannel.PUSH -> TextSecondary
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = channel.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = TextPrimary
                                    )
                                    if (channel == AlertChannel.WHATSAPP) {
                                        Text("< 200ms latency via WhatsApp Cloud API", fontSize = 8.5.sp, color = TextMuted)
                                    } else if (channel == AlertChannel.TELEGRAM) {
                                        Text("Direct bot DM with live tick chart snapshot", fontSize = 8.5.sp, color = TextMuted)
                                    }
                                }
                            }

                            if (isLockedForFree) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2563EB).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("PRO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                }
                            } else {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedChannel = channel },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                                )
                            }
                        }
                    }
                }

                // Contact input if WhatsApp or Telegram
                if (selectedChannel != AlertChannel.PUSH) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = contactInfo,
                        onValueChange = { contactInfo = it },
                        label = {
                            Text(
                                if (selectedChannel == AlertChannel.WHATSAPP) "WhatsApp Number (with country code)" else "Telegram Handle (@username)",
                                fontSize = 10.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val target = targetPriceText.toDoubleOrNull() ?: stock.currentPrice
                            if (selectedChannel != AlertChannel.PUSH && !isPro) {
                                onUpgradeRequired()
                            } else {
                                onSaveAlert(selectedCondition, target, selectedChannel, contactInfo)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_alert_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Text("Set Live Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
