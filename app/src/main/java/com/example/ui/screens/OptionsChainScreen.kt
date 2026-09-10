package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleDataProvider
import com.example.model.OptionsStrike
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun OptionsChainScreen(
    modifier: Modifier = Modifier
) {
    val strikes = SampleDataProvider.optionsChainData
    val maxCallOI = strikes.maxOf { it.callOI }
    val maxPutOI = strikes.maxOf { it.putOI }

    val totalCallOI = strikes.sumOf { it.callOI }
    val totalPutOI = strikes.sumOf { it.putOI }
    val pcrRatio = if (totalCallOI > 0) totalPutOI.toDouble() / totalCallOI.toDouble() else 1.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Header stats
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
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "NIFTY 50 OPTIONS MATRIX & VOLATILITY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x3306B6D4))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Near Weekly Expiry", fontSize = 9.sp, color = AccentCyan)
                        }
                    }

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
                            Text("Put-Call Ratio (PCR)", fontSize = 9.sp, color = TextMuted)
                            Text(
                                String.format(Locale.US, "%.2f", pcrRatio),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pcrRatio > 1.0) GainGreen else LossRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column {
                            Text("Max Pain Strike", fontSize = 9.sp, color = TextMuted)
                            Text(
                                "25,200",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column {
                            Text("ATM Volatility (IV)", fontSize = 9.sp, color = TextMuted)
                            Text(
                                "12.8%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentAmber,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Matrix Column Headers
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1B2230))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CALL OI", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = GainGreen, modifier = Modifier.weight(1.2f))
                Text("CALL IV", fontSize = 9.5.sp, color = TextMuted, modifier = Modifier.weight(0.9f))
                Text("STRIKE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text("PUT IV", fontSize = 9.5.sp, color = TextMuted, modifier = Modifier.weight(0.9f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                Text("PUT OI", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = LossRed, modifier = Modifier.weight(1.2f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }

        // Strike rows with Heatmap formatting
        items(strikes) { s ->
            val callHeatPct = (s.callOI.toFloat() / maxCallOI).coerceIn(0f, 1f)
            val putHeatPct = (s.putOI.toFloat() / maxPutOI).coerceIn(0f, 1f)
            val isAtm = s.strikePrice == 25200.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = if (isAtm) Color(0xFF1E283D) else DarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isAtm) AccentBlue else BorderSubtle.copy(alpha = 0.5f)
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(36.dp)) {
                    // Heatmap fills
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1.5f).fillMaxHeight()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(callHeatPct)
                                    .align(Alignment.CenterStart)
                                    .background(GainGreen.copy(alpha = 0.15f))
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.weight(1.5f).fillMaxHeight()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(putHeatPct)
                                    .align(Alignment.CenterEnd)
                                    .background(LossRed.copy(alpha = 0.15f))
                            )
                        }
                    }

                    // Content text
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${s.callOI / 1000}k",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GainGreen,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = "${s.callIV}%",
                            fontSize = 9.5.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(0.9f)
                        )
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${s.strikePrice.toInt()}${if (isAtm) " ATM" else ""}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAtm) AccentCyan else TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "${s.putIV}%",
                            fontSize = 9.5.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(0.9f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                        Text(
                            text = "${s.putOI / 1000}k",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LossRed,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1.2f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
