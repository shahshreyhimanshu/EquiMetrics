package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderAction
import com.example.model.PaperPosition
import com.example.model.PaperTradeLog
import com.example.ui.theme.*
import com.example.viewmodel.MarketViewModel
import java.util.Locale

@Composable
fun PaperTradingScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val positions = uiState.paperPositions
    val logs = uiState.paperTradeLogs

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Virtual Balance Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CurrencyRupee,
                                contentDescription = null,
                                tint = GainGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "VIRTUAL PAPER TRADING PORTFOLIO",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted
                            )
                        }

                        Button(
                            onClick = { viewModel.openOrderDialog(uiState.selectedStockSymbol) },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Order", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹${String.format(Locale.US, "%,.2f", uiState.totalNetWorth)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Virtual Cash Available", fontSize = 9.sp, color = TextMuted)
                            Text("₹${String.format(Locale.US, "%,.2f", uiState.virtualCashBalance)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            Text("Holdings Value", fontSize = 9.sp, color = TextMuted)
                            Text("₹${String.format(Locale.US, "%,.2f", uiState.totalPortfolioCurrent)}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
                        }
                        Column {
                            val isPos = uiState.totalPortfolioPnl >= 0
                            Text("Unrealized P&L", fontSize = 9.sp, color = TextMuted)
                            Text(
                                "${String.format(Locale.US, "%+,.2f", uiState.totalPortfolioPnl)}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPos) GainGreen else LossRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Active Open Positions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OPEN POSITIONS (${positions.size})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text("Real-time Simulated Quotes", fontSize = 9.sp, color = TextMuted)
            }
        }

        if (positions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No open positions. Place an order to start demo trading.", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(positions) { pos ->
                PositionCard(
                    position = pos,
                    onTapStock = { viewModel.selectStock(pos.symbol) },
                    onTrade = { viewModel.openOrderDialog(pos.symbol) }
                )
            }
        }

        // Trade Execution History Journal
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRADE JOURNAL & EXECUTION LOG",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text("${logs.size} Executions", fontSize = 9.sp, color = TextMuted)
            }
        }

        items(logs) { log ->
            TradeJournalItem(log = log)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PositionCard(
    position: PaperPosition,
    onTapStock: () -> Unit,
    onTrade: () -> Unit
) {
    val isPnlPos = position.pnl >= 0
    val pnlColor = if (isPnlPos) GainGreen else LossRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTapStock() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(position.symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("${position.quantity} Shares", fontSize = 9.sp, color = AccentCyan)
                    }
                }

                IconButton(
                    onClick = onTrade,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Order", tint = AccentBlue)
                }
            }

            Text(position.companyName, fontSize = 10.sp, color = TextMuted)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Avg Buy Price", fontSize = 8.5.sp, color = TextMuted)
                    Text("₹${String.format(Locale.US, "%,.2f", position.avgPrice)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                }
                Column {
                    Text("Current LTP", fontSize = 8.5.sp, color = TextMuted)
                    Text("₹${String.format(Locale.US, "%,.2f", position.currentPrice)}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Unrealized P&L", fontSize = 8.5.sp, color = TextMuted)
                    Text(
                        "${String.format(Locale.US, "%+,.2f", position.pnl)} (${String.format(Locale.US, "%+.2f%%", position.pnlPct)})",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = pnlColor
                    )
                }
            }
        }
    }
}

@Composable
fun TradeJournalItem(log: PaperTradeLog) {
    val isBuy = log.action == OrderAction.BUY
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131720)),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isBuy) GainGreen.copy(alpha = 0.2f) else LossRed.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = log.action.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBuy) GainGreen else LossRed
                    )
                }

                Column {
                    Text(
                        text = "${log.symbol} • ${log.quantity} shares",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${log.timestamp} • ${log.orderType.label}",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format(Locale.US, "%,.2f", log.executedPrice)}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary
                )
                Text(
                    text = "Total: ₹${String.format(Locale.US, "%,.0f", log.totalAmount)}",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }
        }
    }
}
