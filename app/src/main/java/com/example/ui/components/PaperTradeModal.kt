package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.OrderAction
import com.example.model.OrderType
import com.example.model.Stock
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperTradeModal(
    stock: Stock,
    availableCash: Double,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onExecuteOrder: (action: OrderAction, orderType: OrderType, quantity: Int, price: Double) -> Unit
) {
    if (!isOpen) return

    var selectedAction by remember { mutableStateOf(OrderAction.BUY) }
    var selectedOrderType by remember { mutableStateOf(OrderType.MARKET) }
    var quantityText by remember { mutableStateOf("10") }
    var limitPriceText by remember(stock.currentPrice) {
        mutableStateOf(String.format(Locale.US, "%.2f", stock.currentPrice))
    }
    var triggerPriceText by remember(stock.currentPrice) {
        mutableStateOf(String.format(Locale.US, "%.2f", stock.currentPrice * 0.98))
    }

    val quantity = quantityText.toIntOrNull() ?: 0
    val executionPrice = when (selectedOrderType) {
        OrderType.MARKET -> stock.currentPrice
        OrderType.LIMIT -> limitPriceText.toDoubleOrNull() ?: stock.currentPrice
        OrderType.STOP_LOSS -> triggerPriceText.toDoubleOrNull() ?: stock.currentPrice
        OrderType.COVER_ORDER -> stock.currentPrice
    }
    val totalRequiredMargin = quantity * executionPrice
    val hasSufficientCash = selectedAction == OrderAction.SELL || availableCash >= totalRequiredMargin

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stock.symbol,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x222563EB))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NSE EQ",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue
                                )
                            }
                        }
                        Text(
                            text = "LTP: ₹${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Toggle: BUY vs SELL
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedAction == OrderAction.BUY) GainGreen else Color.Transparent)
                            .clickable { selectedAction = OrderAction.BUY }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BUY (Long)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedAction == OrderAction.BUY) Color.Black else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedAction == OrderAction.SELL) LossRed else Color.Transparent)
                            .clickable { selectedAction = OrderAction.SELL }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SELL (Short/Exit)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedAction == OrderAction.SELL) Color.White else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Order Type Selector
                Text(
                    text = "ORDER TYPE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OrderType.values().forEach { type ->
                        val isSelected = type == selectedOrderType
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) AccentBlueSubtle else DarkSurfaceVariant)
                                .border(1.dp, if (isSelected) AccentBlue else BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable { selectedOrderType = type }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.label.split(" ").first(),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AccentCyan else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity Input (Full shares only per Indian regulatory norms)
                Text(
                    text = "QUANTITY (Full Shares - No Fractional)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input ->
                        // Allow only positive integers
                        if (input.all { it.isDigit() }) {
                            quantityText = input
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    )
                )

                if (selectedOrderType == OrderType.LIMIT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "LIMIT PRICE (₹)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = limitPriceText,
                        onValueChange = { limitPriceText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }

                if (selectedOrderType == OrderType.STOP_LOSS || selectedOrderType == OrderType.COVER_ORDER) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TRIGGER / STOP-LOSS PRICE (₹)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = triggerPriceText,
                        onValueChange = { triggerPriceText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Margin & Cash Overview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C1017))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Order Value:", fontSize = 11.sp, color = TextMuted)
                            Text(
                                "₹${String.format(Locale.US, "%,.2f", totalRequiredMargin)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Available Virtual Cash:", fontSize = 11.sp, color = TextMuted)
                            Text(
                                "₹${String.format(Locale.US, "%,.2f", availableCash)}",
                                fontSize = 11.sp,
                                color = if (hasSufficientCash) GainGreen else LossRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                if (!hasSufficientCash) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Insufficient virtual cash balance for this order.",
                        fontSize = 10.5.sp,
                        color = LossRed
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Execute Button
                Button(
                    onClick = {
                        if (quantity > 0 && hasSufficientCash) {
                            onExecuteOrder(
                                selectedAction,
                                selectedOrderType,
                                quantity,
                                executionPrice
                            )
                        }
                    },
                    enabled = quantity > 0 && hasSufficientCash,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedAction == OrderAction.BUY) GainGreen else LossRed,
                        disabledContainerColor = DarkSurfaceVariant
                    )
                ) {
                    Text(
                        text = "SUBMIT ${selectedAction.name} ORDER (${quantity} SHARES)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedAction == OrderAction.BUY) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
