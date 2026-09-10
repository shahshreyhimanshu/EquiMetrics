package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun BacktestScreen(
    modifier: Modifier = Modifier
) {
    var selectedStrategy by remember { mutableStateOf("EMA 20/50 Golden Cross") }
    val strategies = listOf(
        "EMA 20/50 Golden Cross",
        "RSI Mean Reversion (< 30 to > 50)",
        "Bollinger Band Squeeze Breakout",
        "52-Week High Momentum"
    )

    val stats = remember(selectedStrategy) {
        when (selectedStrategy) {
            "EMA 20/50 Golden Cross" -> StrategyStats(
                winRatePct = 61.4,
                totalTrades = 124,
                profitFactor = 1.84,
                maxDrawdownPct = 11.2,
                annualizedReturnPct = 21.8,
                sharpeRatio = 1.45,
                equityCurve = listOf(100f, 103f, 101f, 108f, 114f, 110f, 119f, 126f, 122f, 134f, 142f)
            )
            "RSI Mean Reversion (< 30 to > 50)" -> StrategyStats(
                winRatePct = 68.2,
                totalTrades = 98,
                profitFactor = 2.10,
                maxDrawdownPct = 8.5,
                annualizedReturnPct = 18.5,
                sharpeRatio = 1.62,
                equityCurve = listOf(100f, 105f, 104f, 111f, 116f, 118f, 125f, 127f, 132f, 138f)
            )
            else -> StrategyStats(
                winRatePct = 54.8,
                totalTrades = 152,
                profitFactor = 1.62,
                maxDrawdownPct = 14.0,
                annualizedReturnPct = 24.3,
                sharpeRatio = 1.28,
                equityCurve = listOf(100f, 98f, 106f, 103f, 115f, 122f, 118f, 129f, 137f, 145f)
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
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
                            Icon(
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "QUANTITATIVE STRATEGY BACKTESTER",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text("3-Year Historical NSE Data", fontSize = 8.5.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Strategy Selector
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(strategies) { strat ->
                            val isSelected = strat == selectedStrategy
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentBlue else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) AccentBlue else BorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { selectedStrategy = strat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = strat,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Backtest Equity Curve Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HYPOTHETICAL EQUITY GROWTH (BASE 100)", fontSize = 10.sp, color = TextMuted)
                        Text("+${String.format(Locale.US, "%.1f%%", stats.equityCurve.last() - 100f)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GainGreen)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val values = stats.equityCurve
                            val minV = values.minOrNull() ?: 100f
                            val maxV = values.maxOrNull() ?: 150f
                            val range = (maxV - minV).coerceAtLeast(1f)
                            val stepX = size.width / (values.size - 1)

                            val path = Path()
                            values.forEachIndexed { i, v ->
                                val x = i * stepX
                                val y = size.height - ((v - minV) / range) * size.height * 0.85f - 6f
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }

                            drawPath(
                                path = path,
                                color = AccentCyan,
                                style = Stroke(width = 2.5f)
                            )
                        }
                    }
                }
            }
        }

        // Strategy Statistical Performance Grid
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "EMPIRICAL PERFORMANCE METRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricTile("Win Rate", "${stats.winRatePct}%", GainGreen, Modifier.weight(1f))
                        MetricTile("Total Trades", "${stats.totalTrades}", TextPrimary, Modifier.weight(1f))
                        MetricTile("Profit Factor", "${stats.profitFactor}x", AccentCyan, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricTile("Max Drawdown", "-${stats.maxDrawdownPct}%", LossRed, Modifier.weight(1f))
                        MetricTile("CAGR", "+${stats.annualizedReturnPct}%", GainGreen, Modifier.weight(1f))
                        MetricTile("Sharpe Ratio", "${stats.sharpeRatio}", AccentAmber, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class StrategyStats(
    val winRatePct: Double,
    val totalTrades: Int,
    val profitFactor: Double,
    val maxDrawdownPct: Double,
    val annualizedReturnPct: Double,
    val sharpeRatio: Double,
    val equityCurve: List<Float>
)

@Composable
fun MetricTile(title: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .padding(8.dp)
    ) {
        Column {
            Text(title, fontSize = 9.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = valueColor
            )
        }
    }
}
