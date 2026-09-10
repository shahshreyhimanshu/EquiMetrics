package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Stock
import com.example.ui.components.RrgChartView
import com.example.ui.theme.*
import com.example.viewmodel.MarketViewModel
import java.util.Locale

@Composable
fun ScreenerScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPro = uiState.isPro
    val filter = uiState.screenerFilter

    var activeScreenerTab by remember { mutableStateOf("Custom Formula Screener") }
    var searchQuery by remember { mutableStateOf(filter.queryText) }
    var customFormulaText by remember { mutableStateOf(uiState.customFormula) }
    var maxPe by remember { mutableStateOf(filter.maxPe) }
    var maxDebt by remember { mutableStateOf(filter.maxDebtToEquity) }
    var rrgTimeframe by remember { mutableStateOf(uiState.rrgTimeframe) }

    val basicPresetQueries = listOf(
        "P/E < 25",
        "Debt to Equity < 0.5",
        "Dividend Yield > 1.5%",
        "ROE > 20%"
    )

    val institutionalFormulaPresets = listOf(
        "(ROCE > 15%) AND (Free Cash Flow / Net Profit > 0.8) AND (Debt to Equity < 0.3)",
        "(P/E < 22) AND (Operating Margin > 18%) AND (Promoter > 50%)",
        "(ROE > 22%) AND (Debt to Equity < 0.2) AND (Sales Growth > 15%)",
        "(Free Cash Flow > 1000 Cr) AND (Operating Margin > 20%)"
    )

    // Evaluate stock filter based on basic criteria and custom formula (if Pro)
    val filteredStocks = remember(uiState.stocks, searchQuery, customFormulaText, maxPe, maxDebt, isPro) {
        uiState.stocks.filter { stock ->
            val peMatches = stock.ratios.peRatio <= maxPe
            val debtMatches = stock.ratios.debtToEquity <= maxDebt
            val textMatches = if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase()
                stock.symbol.lowercase().contains(q) ||
                stock.companyName.lowercase().contains(q) ||
                stock.sector.lowercase().contains(q) ||
                (q.contains("pe <") && stock.ratios.peRatio < 20) ||
                (q.contains("debt") && stock.ratios.debtToEquity < 0.5) ||
                (q.contains("dividend") && stock.ratios.dividendYieldPercentage > 1.5) ||
                (q.contains("roe") && stock.ratios.roePercentage > 20)
            }

            val formulaMatches = if (isPro && customFormulaText.isNotBlank()) {
                val f = customFormulaText.lowercase()
                var matches = true
                if (f.contains("roce > 15") && stock.ratios.rocePercentage <= 15.0) matches = false
                if (f.contains("debt to equity < 0.3") && stock.ratios.debtToEquity >= 0.3) matches = false
                if (f.contains("debt to equity < 0.2") && stock.ratios.debtToEquity >= 0.2) matches = false
                if (f.contains("free cash flow / net profit > 0.8") && (stock.ratios.freeCashFlowCr / (stock.marketCapCr * 0.05).coerceAtLeast(1.0)) < 0.8) matches = false
                if (f.contains("operating margin > 18") && stock.ratios.operatingMarginPercentage <= 18.0) matches = false
                if (f.contains("operating margin > 20") && stock.ratios.operatingMarginPercentage <= 20.0) matches = false
                if (f.contains("pe < 22") && stock.ratios.peRatio >= 22.0) matches = false
                if (f.contains("roe > 22") && stock.ratios.roePercentage <= 22.0) matches = false
                if (f.contains("promoter > 50") && stock.ratios.promoterHoldingPct <= 50.0) matches = false
                matches
            } else true

            peMatches && debtMatches && textMatches && formulaMatches
        }.sortedBy { it.ratios.peRatio }
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
            // Screener Tool Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeScreenerTab == "Custom Formula Screener") AccentBlue else Color.Transparent)
                        .clickable { activeScreenerTab = "Custom Formula Screener" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Formula Screener",
                        fontSize = 11.5.sp,
                        fontWeight = if (activeScreenerTab == "Custom Formula Screener") FontWeight.Bold else FontWeight.Normal,
                        color = if (activeScreenerTab == "Custom Formula Screener") Color.White else TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeScreenerTab == "RRG & Sector Heatmap") AccentBlue else Color.Transparent)
                        .clickable { activeScreenerTab = "RRG & Sector Heatmap" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "RRG & Sector Heatmap",
                            fontSize = 11.5.sp,
                            fontWeight = if (activeScreenerTab == "RRG & Sector Heatmap") FontWeight.Bold else FontWeight.Normal,
                            color = if (activeScreenerTab == "RRG & Sector Heatmap") Color.White else TextMuted
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.4f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("PRO", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        if (activeScreenerTab == "RRG & Sector Heatmap") {
            // Relative Rotation Graphs & Sector Heatmap Module
            item {
                RrgChartView(
                    timeframe = rrgTimeframe,
                    onTimeframeChange = {
                        rrgTimeframe = it
                        viewModel.setRrgTimeframe(it)
                    }
                )
            }
        } else {
            // Screener Filter & Custom Formula Card
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "RULE-BASED STOCK SCREENER",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Metric Criteria Only",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Standard Search Filter
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.updateScreenerFilter(filter.copy(queryText = it))
                            },
                            placeholder = {
                                Text(
                                    "Filter by sector, symbol, or basic metric (e.g. IT, Banking)...",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.5.sp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Basic Preset Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(basicPresetQueries) { preset ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceVariant)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                        .clickable {
                                            searchQuery = preset
                                            viewModel.updateScreenerFilter(filter.copy(queryText = preset))
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(preset, fontSize = 9.5.sp, color = AccentCyan)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // PRO TIER: Custom Boolean Metric & Formula Builder
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(15.dp))
                                Text("Custom Metric & Formula Engine", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isPro) GainGreen.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(if (isPro) "PRO ACTIVE" else "PRO LOCKED", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = if (isPro) GainGreen else AccentBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Write arbitrary compound formulas: (ROCE > 15%) AND (Free Cash Flow / Net Profit > 0.8) AND (Debt to Equity < 0.3)",
                            fontSize = 9.5.sp,
                            color = TextSecondary,
                            lineHeight = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom Formula TextField
                        OutlinedTextField(
                            value = customFormulaText,
                            onValueChange = {
                                if (!isPro) {
                                    viewModel.openUpgradeModal()
                                } else {
                                    customFormulaText = it
                                    viewModel.updateCustomFormula(it)
                                }
                            },
                            placeholder = {
                                Text(
                                    "(ROCE > 15%) AND (Free Cash Flow / Net Profit > 0.8) AND (Debt to Equity < 0.3)",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("custom_formula_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = if (isPro) BorderSubtle else Color(0xFF3B2F50),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Institutional Formula Presets
                        Text("INSTITUTIONAL PRESET FORMULAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            institutionalFormulaPresets.forEach { formula ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceVariant)
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (!isPro) {
                                                viewModel.openUpgradeModal()
                                            } else {
                                                customFormulaText = formula
                                                viewModel.updateCustomFormula(formula)
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(formula, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace, color = AccentPurple, modifier = Modifier.weight(1f))
                                        if (!isPro) {
                                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Metric Sliders: Max P/E & Max D/E
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Max P/E Slider
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Max P/E Ratio", fontSize = 10.sp, color = TextMuted)
                                    Text("${maxPe.toInt()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Slider(
                                    value = maxPe,
                                    onValueChange = {
                                        maxPe = it
                                        viewModel.updateScreenerFilter(filter.copy(maxPe = it))
                                    },
                                    valueRange = 10f..80f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AccentBlue,
                                        activeTrackColor = AccentBlue,
                                        inactiveTrackColor = BorderSubtle
                                    )
                                )
                            }

                            // Max Debt to Equity Slider
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Max Debt/Equity", fontSize = 10.sp, color = TextMuted)
                                    Text(String.format(Locale.US, "%.1f", maxDebt), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Slider(
                                    value = maxDebt,
                                    onValueChange = {
                                        maxDebt = it
                                        viewModel.updateScreenerFilter(filter.copy(maxDebtToEquity = it))
                                    },
                                    valueRange = 0.1f..8.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = AccentCyan,
                                        activeTrackColor = AccentCyan,
                                        inactiveTrackColor = BorderSubtle
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCREENER RESULTS (${filteredStocks.size} STOCKS)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Sorted by P/E ascending",
                        fontSize = 9.5.sp,
                        color = TextMuted
                    )
                }
            }

            items(filteredStocks, key = { it.symbol }) { stock ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectStock(stock.symbol) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(stock.symbol, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("₹${String.format(Locale.US, "%,.2f", stock.currentPrice)}", fontSize = 12.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
                            }
                            Text(stock.companyName, fontSize = 10.5.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("P/E: ${stock.ratios.peRatio}", fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = AccentCyan)
                                Text("D/E: ${stock.ratios.debtToEquity}", fontSize = 9.5.sp, color = TextSecondary)
                                Text("ROCE: ${stock.ratios.rocePercentage}%", fontSize = 9.5.sp, color = GainGreen)
                                Text("Div: ${stock.ratios.dividendYieldPercentage}%", fontSize = 9.5.sp, color = TextMuted)
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Inspect Profile",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
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
