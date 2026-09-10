package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
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
import com.example.ui.theme.*
import com.example.viewmodel.MarketViewModel
import java.util.Locale

@Composable
fun MacroSimulatorScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val macro = uiState.macroState

    var crudeOil by remember { mutableStateOf(macro.crudeOilShiftPct) }
    var repoRateBps by remember { mutableStateOf(macro.rbiRepoRateShiftBps) }
    var usdInrPct by remember { mutableStateOf(macro.usdinrShiftPct) }

    val impactPct = macro.estimatedPortfolioImpactPct
    val isImpactPositive = impactPct >= 0
    val impactColor = if (isImpactPositive) GainGreen else LossRed
    val projectedValue = uiState.totalNetWorth * (1.0 + (impactPct / 100.0))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Projected Net Worth Impact Card
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
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "MACRO STRESS-TEST SIMULATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x338B5CF6))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Stress Scenario", fontSize = 9.sp, color = AccentPurple)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("Projected Net Worth", fontSize = 10.sp, color = TextMuted)
                            Text(
                                "₹${String.format(Locale.US, "%,.2f", projectedValue)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(impactColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Impact: ${String.format(Locale.US, "%+.2f%%", impactPct)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = impactColor
                            )
                        }
                    }
                }
            }
        }

        // Interactive Macro Sliders
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MACROECONOMIC SHIFT PARAMETERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Brent Crude Oil
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Brent Crude Oil Shift", fontSize = 11.sp, color = TextPrimary)
                            Text(
                                String.format(Locale.US, "%+.0f%%", crudeOil),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (crudeOil > 0) LossRed else GainGreen
                            )
                        }
                        Slider(
                            value = crudeOil,
                            onValueChange = {
                                crudeOil = it
                                viewModel.updateMacroShift(crudeOil, repoRateBps, usdInrPct)
                            },
                            valueRange = -20f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentAmber,
                                activeTrackColor = AccentAmber,
                                inactiveTrackColor = BorderSubtle
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. RBI Repo Rate
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("RBI Repo Rate Shift", fontSize = 11.sp, color = TextPrimary)
                            Text(
                                String.format(Locale.US, "%+.0f bps (%+.2f%%)", repoRateBps, repoRateBps / 100f),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (repoRateBps > 0) LossRed else GainGreen
                            )
                        }
                        Slider(
                            value = repoRateBps,
                            onValueChange = {
                                repoRateBps = it
                                viewModel.updateMacroShift(crudeOil, repoRateBps, usdInrPct)
                            },
                            valueRange = -50f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentCyan,
                                activeTrackColor = AccentCyan,
                                inactiveTrackColor = BorderSubtle
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. USD/INR Exchange Rate
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("USD/INR Currency Shift", fontSize = 11.sp, color = TextPrimary)
                            Text(
                                String.format(Locale.US, "%+.1f%%", usdInrPct),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (usdInrPct > 0) GainGreen else TextSecondary
                            )
                        }
                        Slider(
                            value = usdInrPct,
                            onValueChange = {
                                usdInrPct = it
                                viewModel.updateMacroShift(crudeOil, repoRateBps, usdInrPct)
                            },
                            valueRange = -5f..10f,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentBlue,
                                activeTrackColor = AccentBlue,
                                inactiveTrackColor = BorderSubtle
                            )
                        )
                    }
                }
            }
        }

        // Sector Sensitivity Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ESTIMATED SECTORAL SENSITIVITIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val sectors = listOf(
                        Triple("IT Services (TCS, INFY)", (usdInrPct * 0.7f - 0.2f), "Gains from INR depreciation against USD revenues"),
                        Triple("Automobile (TATAMOTORS)", (-crudeOil * 0.3f - (repoRateBps * 0.02f)), "Sensitive to auto loan EMIs and fuel input costs"),
                        Triple("Banking & Finance (HDFCBANK, ICICIBANK)", (-repoRateBps * 0.03f + 0.5f), "NIM sensitivity to policy rate repricing cycles"),
                        Triple("Energy & Oil Refining (RELIANCE)", (-crudeOil * 0.15f), "Refining margins fluctuate with global crack spreads")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sectors.forEach { (sec, valPct, reason) ->
                            val isPos = valPct >= 0
                            val secColor = if (isPos) GainGreen else LossRed
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(sec, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        Text(
                                            String.format(Locale.US, "%+.1f%%", valPct),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = secColor,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(reason, fontSize = 9.5.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
