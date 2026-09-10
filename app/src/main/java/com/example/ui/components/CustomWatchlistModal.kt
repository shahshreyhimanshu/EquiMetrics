package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Stock
import com.example.ui.theme.*

@Composable
fun CustomWatchlistModal(
    isOpen: Boolean,
    allStocks: List<Stock>,
    onDismiss: () -> Unit,
    onCreateWatchlist: (name: String, symbols: List<String>) -> Unit
) {
    if (!isOpen) return

    var watchlistName by remember { mutableStateOf("High Growth Radar") }
    var selectedSymbols by remember { mutableStateOf(setOf("RELIANCE", "TCS", "INFY", "BHARTIARTL")) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("custom_watchlist_modal"),
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
                    Column {
                        Text(
                            text = "Create Custom Watchlist",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Institutional Power Tool (Pro Tier)",
                            fontSize = 10.sp,
                            color = AccentBlue
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Watchlist Name
                Text("WATCHLIST TITLE", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = watchlistName,
                    onValueChange = { watchlistName = it },
                    placeholder = { Text("e.g. Dividend Compounders", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selected count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SELECT STOCKS TO TRACK", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("${selectedSymbols.size} Selected", fontSize = 10.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Stock selection list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    items(allStocks) { stock ->
                        val isChecked = selectedSymbols.contains(stock.symbol)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isChecked) AccentBlueSubtle else Color.Transparent)
                                .clickable {
                                    selectedSymbols = if (isChecked) {
                                        selectedSymbols - stock.symbol
                                    } else {
                                        selectedSymbols + stock.symbol
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(stock.symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(stock.companyName, fontSize = 9.sp, color = TextMuted, maxLines = 1)
                            }
                            if (isChecked) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AccentBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
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
                            if (watchlistName.isNotBlank() && selectedSymbols.isNotEmpty()) {
                                onCreateWatchlist(watchlistName.trim(), selectedSymbols.toList())
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("create_watchlist_confirm_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        enabled = watchlistName.isNotBlank() && selectedSymbols.isNotEmpty()
                    ) {
                        Text("Create Watchlist", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
