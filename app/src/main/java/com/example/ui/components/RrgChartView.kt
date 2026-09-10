package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleDataProvider
import com.example.model.RrgSectorPoint
import com.example.ui.theme.*

@Composable
fun RrgChartView(
    timeframe: String,
    onTimeframeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sectorData = SampleDataProvider.rrgSectorData
    var selectedSector by remember { mutableStateOf<RrgSectorPoint?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rrg_chart_view"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header & Timeframe selector
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
                            text = "Relative Rotation Graph (RRG)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("PRO UTILITY", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        }
                    }
                    Text(
                        text = "JdK RS-Ratio vs RS-Momentum vs Nifty 50 Benchmark",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }

                // Timeframe Chips (1M, 3M, 6M, 1Y)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("1M", "3M", "6M", "1Y").forEach { tf ->
                        val isSel = timeframe == tf
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSel) AccentBlue else DarkSurfaceVariant)
                                .clickable { onTimeframeChange(tf) }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tf,
                                fontSize = 9.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quadrant Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val centerX = w / 2f
                    val centerY = h / 2f

                    // Subtle background quadrant tints
                    // Top-Right: Leading (Green)
                    drawRect(
                        color = Color(0x1510B981),
                        topLeft = Offset(centerX, 0f),
                        size = androidx.compose.ui.geometry.Size(centerX, centerY)
                    )
                    // Bottom-Right: Weakening (Orange)
                    drawRect(
                        color = Color(0x15F59E0B),
                        topLeft = Offset(centerX, centerY),
                        size = androidx.compose.ui.geometry.Size(centerX, centerY)
                    )
                    // Bottom-Left: Lagging (Red)
                    drawRect(
                        color = Color(0x15EF4444),
                        topLeft = Offset(0f, centerY),
                        size = androidx.compose.ui.geometry.Size(centerX, centerY)
                    )
                    // Top-Left: Improving (Blue)
                    drawRect(
                        color = Color(0x153B82F6),
                        topLeft = Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(centerX, centerY)
                    )

                    // Axes (100 benchmark line)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    // Vertical axis (RS-Ratio = 100)
                    drawLine(
                        color = Color(0x55FFFFFF),
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, h),
                        strokeWidth = 1.5f,
                        pathEffect = dashEffect
                    )
                    // Horizontal axis (RS-Momentum = 100)
                    drawLine(
                        color = Color(0x55FFFFFF),
                        start = Offset(0f, centerY),
                        end = Offset(w, centerY),
                        strokeWidth = 1.5f,
                        pathEffect = dashEffect
                    )

                    // Range bounds: RS-Ratio [92, 108], RS-Momentum [92, 108]
                    val minVal = 92f
                    val maxVal = 108f

                    sectorData.forEach { sector ->
                        val normX = ((sector.rsRatio - minVal) / (maxVal - minVal)).coerceIn(0.08f, 0.92f)
                        val normY = (1f - ((sector.rsMomentum - minVal) / (maxVal - minVal))).coerceIn(0.08f, 0.92f)

                        val px = normX * w
                        val py = normY * h

                        // Tail (history trail vector showing rotation)
                        val tailOffset = when (sector.quadrant) {
                            "Leading" -> Offset(-14f, 10f)
                            "Weakening" -> Offset(-12f, -12f)
                            "Lagging" -> Offset(10f, -14f)
                            else -> Offset(12f, 12f)
                        }
                        drawLine(
                            color = Color(sector.colorHex).copy(alpha = 0.4f),
                            start = Offset(px + tailOffset.x, py + tailOffset.y),
                            end = Offset(px, py),
                            strokeWidth = 3f
                        )

                        // Dot node
                        drawCircle(
                            color = Color(sector.colorHex),
                            radius = 7f,
                            center = Offset(px, py)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 7f,
                            center = Offset(px, py),
                            style = Stroke(width = 1.5f)
                        )
                    }
                }

                // Quadrant Label Watermarks
                Text(
                    text = "IMPROVING ↗",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF60A5FA),
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                )
                Text(
                    text = "LEADING ★",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399),
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                )
                Text(
                    text = "LAGGING ↘",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF87171),
                    modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
                )
                Text(
                    text = "WEAKENING ⚠",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sector Rotation Heatmap List
            Text(
                text = "SECTOR RELATIVE STRENGTH MATRIX",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                sectorData.take(5).forEach { sec ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
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
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(sec.colorHex))
                            )
                            Text(sec.sectorName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("RS: ${sec.rsRatio}", fontSize = 10.sp, color = TextMuted)
                            Text("Mom: ${sec.rsMomentum}", fontSize = 10.sp, color = TextMuted)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(sec.colorHex).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sec.quadrant.uppercase(),
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(sec.colorHex)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
