package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChartPoint
import com.example.model.ChartType
import com.example.model.Timeframe
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun InteractiveChart(
    points: List<ChartPoint>,
    selectedTimeframe: Timeframe,
    chartType: ChartType,
    showEma20: Boolean,
    showEma50: Boolean,
    showRsi: Boolean,
    showVolume: Boolean,
    onTimeframeChange: (Timeframe) -> Unit,
    onToggleChartType: () -> Unit,
    onToggleIndicator: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inspectedIndex by remember(points) { mutableStateOf<Int?>(null) }

    val activePoint = inspectedIndex?.let { idx ->
        if (idx in points.indices) points[idx] else null
    } ?: points.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Controls Row: Timeframe, Chart Type & Indicator Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeframe Pills
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Timeframe.values().forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) AccentBlue else DarkSurfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) AccentBlue else BorderSubtle.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .pointerInput(tf) {
                                detectTapGestures { onTimeframeChange(tf) }
                            }
                    ) {
                        Text(
                            text = tf.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            // Chart Type Toggle
            IconButton(
                onClick = onToggleChartType,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = if (chartType == ChartType.CANDLESTICK) Icons.Default.BarChart else Icons.Default.ShowChart,
                    contentDescription = "Toggle Chart Style",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Indicator Toggles & Inspection Stats Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Active point inspect stats
            if (activePoint != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activePoint.timestamp,
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "O: ${String.format(Locale.US, "%.1f", activePoint.open)}",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "H: ${String.format(Locale.US, "%.1f", activePoint.high)}",
                        fontSize = 10.sp,
                        color = GainGreen,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "L: ${String.format(Locale.US, "%.1f", activePoint.low)}",
                        fontSize = 10.sp,
                        color = LossRed,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "C: ${String.format(Locale.US, "%.1f", activePoint.close)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Indicator Pills
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IndicatorChip(
                    label = "EMA 20",
                    isActive = showEma20,
                    activeColor = AccentCyan,
                    onClick = { onToggleIndicator("EMA20") }
                )
                IndicatorChip(
                    label = "EMA 50",
                    isActive = showEma50,
                    activeColor = AccentPurple,
                    onClick = { onToggleIndicator("EMA50") }
                )
                IndicatorChip(
                    label = "RSI",
                    isActive = showRsi,
                    activeColor = AccentAmber,
                    onClick = { onToggleIndicator("RSI") }
                )
                IndicatorChip(
                    label = "VOL",
                    isActive = showVolume,
                    activeColor = Color(0xFF64748B),
                    onClick = { onToggleIndicator("VOLUME") }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                val x = change.position.x
                                val pointWidth = size.width / points.size
                                val index = (x / pointWidth).toInt().coerceIn(0, points.lastIndex)
                                inspectedIndex = index
                            },
                            onDragEnd = {
                                inspectedIndex = null
                            },
                            onDragCancel = {
                                inspectedIndex = null
                            }
                        )
                    }
            ) {
                if (points.isEmpty()) return@Canvas

                val minPrice = points.minOf { minOf(it.low, it.ema50) } * 0.996f
                val maxPrice = points.maxOf { maxOf(it.high, it.ema20) } * 1.004f
                val priceRange = (maxPrice - minPrice).coerceAtLeast(1f)
                val maxVol = points.maxOf { it.volume }.coerceAtLeast(1L).toFloat()

                val chartHeight = size.height * (if (showVolume) 0.80f else 0.95f)
                val volumeHeight = size.height * 0.18f
                val stepX = size.width / points.size

                // Background horizontal gridlines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = chartHeight * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color(0x18FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // Draw Volume bars
                if (showVolume) {
                    points.forEachIndexed { i, p ->
                        val barHeight = (p.volume.toFloat() / maxVol) * volumeHeight
                        val x = i * stepX + (stepX * 0.15f)
                        val w = stepX * 0.7f
                        val y = size.height - barHeight
                        val isBull = p.close >= p.open
                        drawRect(
                            color = (if (isBull) GainGreen else LossRed).copy(alpha = 0.35f),
                            topLeft = Offset(x, y),
                            size = Size(w, barHeight)
                        )
                    }
                }

                // Draw Price: Candlesticks or Line
                if (chartType == ChartType.CANDLESTICK) {
                    points.forEachIndexed { i, p ->
                        val x = i * stepX + (stepX * 0.5f)
                        val isBull = p.close >= p.open
                        val candleColor = if (isBull) GainGreen else LossRed

                        val highY = chartHeight - ((p.high - minPrice) / priceRange) * chartHeight
                        val lowY = chartHeight - ((p.low - minPrice) / priceRange) * chartHeight
                        val openY = chartHeight - ((p.open - minPrice) / priceRange) * chartHeight
                        val closeY = chartHeight - ((p.close - minPrice) / priceRange) * chartHeight

                        // Wick
                        drawLine(
                            color = candleColor,
                            start = Offset(x, highY),
                            end = Offset(x, lowY),
                            strokeWidth = 1.5f
                        )

                        // Body
                        val bodyTop = minOf(openY, closeY)
                        val bodyBottom = maxOf(openY, closeY)
                        val bodyHeight = (bodyBottom - bodyTop).coerceAtLeast(2f)
                        val candleWidth = stepX * 0.7f

                        drawRect(
                            color = candleColor,
                            topLeft = Offset(x - candleWidth / 2f, bodyTop),
                            size = Size(candleWidth, bodyHeight)
                        )
                    }
                } else {
                    // Line chart with fill gradient
                    val linePath = Path()
                    val fillPath = Path()

                    points.forEachIndexed { i, p ->
                        val x = i * stepX + (stepX * 0.5f)
                        val y = chartHeight - ((p.close - minPrice) / priceRange) * chartHeight
                        if (i == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, chartHeight)
                            fillPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }
                    fillPath.lineTo(size.width, chartHeight)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        color = AccentBlueSubtle
                    )
                    drawPath(
                        path = linePath,
                        color = AccentBlue,
                        style = Stroke(width = 2.5f)
                    )
                }

                // EMA 20 overlay
                if (showEma20) {
                    val ema20Path = Path()
                    points.forEachIndexed { i, p ->
                        val x = i * stepX + (stepX * 0.5f)
                        val y = chartHeight - ((p.ema20 - minPrice) / priceRange) * chartHeight
                        if (i == 0) ema20Path.moveTo(x, y) else ema20Path.lineTo(x, y)
                    }
                    drawPath(
                        path = ema20Path,
                        color = AccentCyan,
                        style = Stroke(width = 1.5f)
                    )
                }

                // EMA 50 overlay
                if (showEma50) {
                    val ema50Path = Path()
                    points.forEachIndexed { i, p ->
                        val x = i * stepX + (stepX * 0.5f)
                        val y = chartHeight - ((p.ema50 - minPrice) / priceRange) * chartHeight
                        if (i == 0) ema50Path.moveTo(x, y) else ema50Path.lineTo(x, y)
                    }
                    drawPath(
                        path = ema50Path,
                        color = AccentPurple,
                        style = Stroke(width = 1.5f)
                    )
                }

                // Crosshair cursor when scrubbing
                inspectedIndex?.let { idx ->
                    if (idx in points.indices) {
                        val p = points[idx]
                        val x = idx * stepX + (stepX * 0.5f)
                        val y = chartHeight - ((p.close - minPrice) / priceRange) * chartHeight

                        drawLine(
                            color = Color.White.copy(alpha = 0.5f),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        // Sub-chart: RSI (Relative Strength Index)
        if (showRsi) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RSI (14)",
                    fontSize = 9.sp,
                    color = AccentAmber,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Val: ${String.format(Locale.US, "%.1f", activePoint?.rsi ?: 50f)}",
                    fontSize = 9.sp,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F131A))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stepX = size.width / points.size
                    // 70 and 30 reference lines
                    val y70 = size.height * (1f - (70f / 100f))
                    val y30 = size.height * (1f - (30f / 100f))

                    drawLine(
                        color = LossRed.copy(alpha = 0.4f),
                        start = Offset(0f, y70),
                        end = Offset(size.width, y70),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                    drawLine(
                        color = GainGreen.copy(alpha = 0.4f),
                        start = Offset(0f, y30),
                        end = Offset(size.width, y30),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    val rsiPath = Path()
                    points.forEachIndexed { i, p ->
                        val x = i * stepX + (stepX * 0.5f)
                        val y = size.height * (1f - (p.rsi / 100f))
                        if (i == 0) rsiPath.moveTo(x, y) else rsiPath.lineTo(x, y)
                    }
                    drawPath(
                        path = rsiPath,
                        color = AccentAmber,
                        style = Stroke(width = 1.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun IndicatorChip(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else DarkSurfaceVariant)
            .border(
                1.dp,
                if (isActive) activeColor else BorderSubtle.copy(alpha = 0.5f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .pointerInput(label) {
                detectTapGestures { onClick() }
            }
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) activeColor else TextMuted
        )
    }
}
