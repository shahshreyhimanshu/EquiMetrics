package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
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
import androidx.compose.ui.window.DialogProperties
import com.example.model.ChartType
import com.example.model.Stock
import com.example.model.Timeframe
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiChartGridModal(
    isOpen: Boolean,
    allStocks: List<Stock>,
    gridSymbols: List<String>,
    onSymbolChange: (index: Int, symbol: String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    var gridLayoutMode by remember { mutableStateOf(4) } // 2 (split) or 4 (2x2)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .testTag("multi_chart_grid_modal"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.GridView, contentDescription = null, tint = AccentBlue)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Multi-Chart Power Layout",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2563EB).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("PRO", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                }
                            }
                            Text("Simultaneous Multi-Equity Candlestick & Volume Feeds", fontSize = 10.sp, color = TextSecondary)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 2 vs 4 toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (gridLayoutMode == 2) AccentBlue else Color.Transparent)
                                    .clickable { gridLayoutMode = 2 }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("2 Split", fontSize = 10.sp, color = if (gridLayoutMode == 2) Color.White else TextMuted)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (gridLayoutMode == 4) AccentBlue else Color.Transparent)
                                    .clickable { gridLayoutMode = 4 }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("4 Grid", fontSize = 10.sp, color = if (gridLayoutMode == 4) Color.White else TextMuted)
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chart Grid View
                val displayCount = if (gridLayoutMode == 2) 2 else 4
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (displayCount == 2) {
                        for (i in 0 until 2) {
                            val symbol = gridSymbols.getOrElse(i) { allStocks.first().symbol }
                            val stock = allStocks.find { it.symbol == symbol } ?: allStocks.first()
                            SingleChartCell(
                                stock = stock,
                                allStocks = allStocks,
                                onSelectStock = { newSym -> onSymbolChange(i, newSym) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )
                        }
                    } else {
                        // 2x2 grid
                        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 0..1) {
                                val symbol = gridSymbols.getOrElse(i) { allStocks.first().symbol }
                                val stock = allStocks.find { it.symbol == symbol } ?: allStocks.first()
                                SingleChartCell(
                                    stock = stock,
                                    allStocks = allStocks,
                                    onSelectStock = { newSym -> onSymbolChange(i, newSym) },
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 2..3) {
                                val symbol = gridSymbols.getOrElse(i) { allStocks.first().symbol }
                                val stock = allStocks.find { it.symbol == symbol } ?: allStocks.first()
                                SingleChartCell(
                                    stock = stock,
                                    allStocks = allStocks,
                                    onSelectStock = { newSym -> onSymbolChange(i, newSym) },
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleChartCell(
    stock: Stock,
    allStocks: List<Stock>,
    onSelectStock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDropdownOpen by remember { mutableStateOf(false) }
    var cellTf by remember { mutableStateOf(Timeframe.ONE_DAY) }
    val isGain = stock.change >= 0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            // Header bar in cell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stock Selector
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceVariant)
                            .clickable { isDropdownOpen = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(stock.symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("▼", fontSize = 8.sp, color = TextMuted)
                    }

                    DropdownMenu(
                        expanded = isDropdownOpen,
                        onDismissRequest = { isDropdownOpen = false },
                        modifier = Modifier.background(DarkSurfaceVariant).heightIn(max = 240.dp)
                    ) {
                        allStocks.take(20).forEach { st ->
                            DropdownMenuItem(
                                text = { Text("${st.symbol} (₹${String.format(Locale.US, "%,.2f", st.currentPrice)})", fontSize = 11.sp, color = TextPrimary) },
                                onClick = {
                                    onSelectStock(st.symbol)
                                    isDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                // Price & Change
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${String.format(Locale.US, "%,.2f", stock.currentPrice)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        String.format(Locale.US, "%+.2f%%", stock.changePct),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isGain) GainGreen else LossRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Embedded Mini Chart
            val points = stock.chartData[cellTf] ?: emptyList()
            InteractiveChart(
                points = points,
                selectedTimeframe = cellTf,
                chartType = ChartType.CANDLESTICK,
                showEma20 = true,
                showEma50 = false,
                showRsi = false,
                showVolume = true,
                onTimeframeChange = { cellTf = it },
                onToggleChartType = {},
                onToggleIndicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
