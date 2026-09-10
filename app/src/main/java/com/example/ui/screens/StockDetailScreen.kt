package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import com.example.data.SampleDataProvider
import com.example.model.*
import com.example.ui.components.InteractiveChart
import com.example.ui.components.MarketDepthTable
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MarketViewModel
import java.util.Locale

@Composable
fun StockDetailScreen(
    stock: Stock,
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPriceGain = stock.change >= 0
    val tickColor = if (isPriceGain) GainGreen else LossRed

    // Flash highlight animation on simulated price change
    val hasRecentFlash = uiState.lastPriceFlash[stock.symbol]
    val flashBgColor by animateColorAsState(
        targetValue = when (hasRecentFlash) {
            true -> GainGreen.copy(alpha = 0.22f)
            false -> LossRed.copy(alpha = 0.22f)
            null -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 400),
        label = "priceFlash"
    )

    // Stock-specific news
    val stockNews = remember(stock.symbol) {
        SampleDataProvider.stockNewsList.filter { it.stockSymbol == stock.symbol || it.stockSymbol == null }
    }

    // Earnings summary available for this stock
    val earningsSummary = SampleDataProvider.earningsSummaries[stock.symbol]

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Stock Header with Live Quote & Flash Micro-interaction
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(flashBgColor),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Symbol & Company
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stock.symbol,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0x332563EB))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "NSE / BSE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentBlue
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stock.sector,
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stock.companyName,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        // Action Button Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Watchlist Toggle
                            val isInWl = uiState.activeWatchlist.stockSymbols.contains(stock.symbol)
                            IconButton(
                                onClick = { viewModel.toggleStockInActiveWatchlist(stock.symbol) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isInWl) Color(0x33F59E0B) else DarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = if (isInWl) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Watchlist",
                                    tint = if (isInWl) Color(0xFFF59E0B) else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Alert Button
                            IconButton(
                                onClick = { viewModel.openAlertDialog(stock.symbol) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "Alert",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Multi-Chart Toggle
                            IconButton(
                                onClick = { viewModel.toggleMultiChartLayout() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Multi-Chart",
                                    tint = AccentPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // AI Insights Shortcut Button
                            Button(
                                onClick = {
                                    viewModel.selectStock(stock.symbol)
                                    viewModel.setScreen(AppScreen.AI_SUITE)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4400C805)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Insights",
                                    tint = GainGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Insights", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GainGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Price & Change
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = TextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPriceGain) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = tickColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%+,.2f", stock.change)} (${String.format(Locale.US, "%+.2f%%", stock.changePct)})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = tickColor
                                )
                                Text(
                                    text = "• Today",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        // Tags
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            stock.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Stats Bar: Day Range, 52W Range, Vol, Market Cap
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Day High", "₹${String.format(Locale.US, "%,.1f", stock.dayHigh)}")
                        StatItem("Day Low", "₹${String.format(Locale.US, "%,.1f", stock.dayLow)}")
                        StatItem("52W High", "₹${String.format(Locale.US, "%,.1f", stock.fiftyTwoWeekHigh)}")
                        StatItem("52W Low", "₹${String.format(Locale.US, "%,.1f", stock.fiftyTwoWeekLow)}")
                        StatItem("Volume", String.format(Locale.US, "%,d", stock.volume))
                        StatItem("Mkt Cap", "₹${String.format(Locale.US, "%,.0f", stock.marketCapCr / 1000)}k Cr")
                    }
                }
            }
        }

        // 2. Interactive Technical Charts (1D, 1W, 1M, 1Y, ALL + EMA 20/50, RSI, Volume)
        item {
            val chartPoints = stock.chartData[uiState.activeTimeframe] ?: emptyList()
            InteractiveChart(
                points = chartPoints,
                selectedTimeframe = uiState.activeTimeframe,
                chartType = uiState.chartType,
                showEma20 = uiState.showEma20,
                showEma50 = uiState.showEma50,
                showRsi = uiState.showRsi,
                showVolume = uiState.showVolume,
                onTimeframeChange = { viewModel.setTimeframe(it) },
                onToggleChartType = { viewModel.toggleChartType() },
                onToggleIndicator = { viewModel.toggleIndicator(it) }
            )
        }

        // 3. EquiMetrics Key Financial Ratios (P/E, Debt to Equity, ROE, ROCE, Div Yield, EPS, etc.)
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
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "EQUIMETRICS FINANCIAL RATIOS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Factual-only indicator
                        Text(
                            text = "Audited Disclosures",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val r = stock.ratios
                    val ratioItems = listOf(
                        RatioPair("P/E Ratio", "${r.peRatio}", "Price to Earnings (TTM)"),
                        RatioPair("P/B Ratio", "${r.pbRatio}", "Price to Book Value"),
                        RatioPair("Debt to Equity", "${r.debtToEquity}", if (r.debtToEquity < 1.0) "Low Leverage" else "Leveraged"),
                        RatioPair("ROE (%)", "${r.roePercentage}%", "Return on Equity"),
                        RatioPair("ROCE (%)", "${r.rocePercentage}%", "Return on Capital Employed"),
                        RatioPair("Dividend Yield", "${r.dividendYieldPercentage}%", "Annualized Div Yield"),
                        RatioPair("EPS (₹)", "₹${r.earningsPerShare}", "Earnings Per Share"),
                        RatioPair("Operating Margin", "${r.operatingMarginPercentage}%", "EBIT Margin"),
                        RatioPair("Price / Sales", "${r.priceToSales}x", "Revenue Multiple"),
                        RatioPair("Free Cash Flow", "₹${String.format(Locale.US, "%,.0f", r.freeCashFlowCr)} Cr", "Annual FCF"),
                        RatioPair("Promoter Holding", "${r.promoterHoldingPct}%", "Founders / Promoters"),
                        RatioPair("FII / DII Holding", "${r.fiiDiiHoldingPct}%", "Institutional Ownership")
                    )

                    // 2-Column Grid of Financial Ratios
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in ratioItems.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RatioBox(item = ratioItems[i], modifier = Modifier.weight(1f))
                                if (i + 1 < ratioItems.size) {
                                    RatioBox(item = ratioItems[i + 1], modifier = Modifier.weight(1f))
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3B. 10-Year Historical Financial Data & Excel Export (Pro vs Free Gating)
        item {
            val isPro = uiState.isPro
            val allTenYearData = remember(stock.symbol) { SampleDataProvider.get10YearFinancials(stock.symbol) }
            val visibleData = if (isPro) allTenYearData else allTenYearData.take(3)
            var exportFeedback by remember { mutableStateOf<String?>(null) }

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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isPro) "10-Year Historical Financial Statements" else "Historical Financials (3 Years)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isPro) GainGreen.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isPro) "10-YR PRO" else "FREE (3-YR)",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPro) GainGreen else AccentBlue
                                    )
                                }
                            }
                            Text(
                                text = "P&L, Balance Sheet, Free Cash Flow & Debt/Equity Trends",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        if (isPro) {
                            Button(
                                onClick = {
                                    val csv = viewModel.generateFinancialCsv(stock.symbol)
                                    exportFeedback = "Exported 10-Year CSV (${stock.symbol}) successfully"
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export CSV/Excel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (exportFeedback != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(exportFeedback!!, fontSize = 10.sp, color = GainGreen, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("YEAR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(0.9f))
                        Text("REV (₹Cr)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.2f))
                        Text("PAT (₹Cr)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(1.1f))
                        Text("EPS (₹)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(0.9f))
                        Text("OPM %", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(0.9f))
                        Text("D/E", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.weight(0.8f))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Rows
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        visibleData.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(row.year, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.weight(0.9f))
                                Text(String.format(Locale.US, "%,.0f", row.revenueCr), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextPrimary, modifier = Modifier.weight(1.2f))
                                Text(String.format(Locale.US, "%,.0f", row.patCr), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextPrimary, modifier = Modifier.weight(1.1f))
                                Text(row.eps.toString(), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextPrimary, modifier = Modifier.weight(0.9f))
                                Text("${row.operatingMarginPct}%", fontSize = 10.sp, color = GainGreen, modifier = Modifier.weight(0.9f))
                                Text(row.debtToEquity.toString(), fontSize = 10.sp, color = if (row.debtToEquity < 0.5) GainGreen else Color(0xFFF59E0B), modifier = Modifier.weight(0.8f))
                            }
                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }

                    // Free Tier Limitation Gate
                    if (!isPro) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF192233))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                                    Text("Deep 10-Year Histories (FY15 - FY21) Locked", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Free users can access 3 years of audited financials. Pro unlocks 10-year deep balance sheet cycles and 1-click CSV/Excel downloads.",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    lineHeight = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.openUpgradeModal() },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Unlock 10-Year Data & Excel (₹299/mo)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3C. Seasonal Pattern Analysis (Month-by-Month 10-Year Win Rates)
        item {
            val seasonalData = remember(stock.symbol) { SampleDataProvider.getSeasonalReturns(stock.symbol) }
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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Seasonal Pattern Analysis (10-Yr)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2563EB).copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text("HISTORICAL STATS", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                }
                            }
                            Text(
                                text = "Monthly Historical Win-Rate & Average Returns (2015 - 2024)",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 12 Months Grid (4 columns x 3 rows)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (rowIdx in 0 until 3) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (colIdx in 0 until 4) {
                                    val idx = rowIdx * 4 + colIdx
                                    if (idx < seasonalData.size) {
                                        val m = seasonalData[idx]
                                        val isGreenAvg = m.averageReturnPct >= 0
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DarkSurfaceVariant)
                                                .border(1.dp, if (m.winRatePct >= 70.0) GainGreen.copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(6.dp))
                                                .padding(6.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(m.month, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${m.winRatePct.toInt()}% Win",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (m.winRatePct >= 60.0) GainGreen else TextSecondary
                                                )
                                                Text(
                                                    text = String.format(Locale.US, "%+.1f%%", m.averageReturnPct),
                                                    fontSize = 8.5.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (isGreenAvg) GainGreen else LossRed
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3D. Custom Stock Notes & Research Checklist (Pro Feature)
        item {
            val isPro = uiState.isPro
            val notes = uiState.stockNotes[stock.symbol] ?: emptyList()
            val checklist = uiState.stockChecklists[stock.symbol] ?: ResearchChecklist()
            var newNoteText by remember { mutableStateOf("") }

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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Personal Research Notebook & Checklist",
                                fontSize = 13.sp,
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
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Checklist
                    Text("5-POINT FUNDAMENTAL MOAT CHECKLIST", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))

                    val checklistItems = listOf(
                        Triple("Pricing Power & Moat", checklist.hasMoat) { b: Boolean -> viewModel.updateStockChecklist(stock.symbol, checklist.copy(hasMoat = b)) },
                        Triple("Clean Balance Sheet (D/E < 0.5)", checklist.hasCleanBalanceSheet) { b: Boolean -> viewModel.updateStockChecklist(stock.symbol, checklist.copy(hasCleanBalanceSheet = b)) },
                        Triple("High Capital Allocation (ROCE > 18%)", checklist.hasHighCapitalAllocation) { b: Boolean -> viewModel.updateStockChecklist(stock.symbol, checklist.copy(hasHighCapitalAllocation = b)) },
                        Triple("Valuation Margin of Safety", checklist.hasMarginOfSafety) { b: Boolean -> viewModel.updateStockChecklist(stock.symbol, checklist.copy(hasMarginOfSafety = b)) },
                        Triple("Secular Industry Tailwinds", checklist.hasIndustryTailwinds) { b: Boolean -> viewModel.updateStockChecklist(stock.symbol, checklist.copy(hasIndustryTailwinds = b)) }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        checklistItems.forEach { (label, checked, onToggle) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceVariant)
                                    .clickable {
                                        if (!isPro) viewModel.openUpgradeModal() else onToggle(!checked)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontSize = 11.sp, color = TextPrimary)
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        if (!isPro) viewModel.openUpgradeModal() else onToggle(it)
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = AccentBlue)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Saved Notes
                    Text("SAVED RESEARCH NOTES", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(modifier = Modifier.height(4.dp))

                    if (notes.isEmpty()) {
                        Text("No notes added yet for ${stock.symbol}", fontSize = 10.sp, color = TextMuted)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            notes.forEach { note ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF19202D))
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(note.tag, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                                        Text(note.timestamp, fontSize = 8.5.sp, color = TextMuted)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(note.noteText, fontSize = 10.5.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Add Note Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newNoteText,
                            onValueChange = { newNoteText = it },
                            placeholder = { Text("Write thesis note...", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                        )
                        Button(
                            onClick = {
                                if (!isPro) {
                                    viewModel.openUpgradeModal()
                                } else if (newNoteText.isNotBlank()) {
                                    viewModel.addStockNote(stock.symbol, newNoteText.trim(), "Thesis")
                                    newNoteText = ""
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Factual AI Earnings Digest Banner
        if (earningsSummary != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openEarningsDrawer() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A26)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25334D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x338B5CF6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "AI Earnings Call Digest (${earningsSummary.quarter})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Revenue: +${earningsSummary.revenueYoYGrowthPct}% YoY • PAT: +${earningsSummary.patYoYGrowthPct}% YoY (Factual Only)",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 5. 5-Level Bid/Ask Market Depth
        item {
            MarketDepthTable(
                depthItems = stock.marketDepth,
                feedSourceName = uiState.feedConfig.source.title,
                onOpenFeedSettings = { viewModel.openFeedModal() }
            )
        }

        // 6. Regulatory & Compliance Analytical Note
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Factual Analytical Metrics: EquiMetrics provides rule-based calculations and financial ratios. We do not provide Buy or Sell recommendations.",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 13.sp
                    )
                }
            }
        }

        // 7. Recent News Relevant to this Stock
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
                                imageVector = Icons.Default.Newspaper,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "RELEVANT NEWS & PUBLIC SENTIMENT",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Verified Sources",
                            fontSize = 9.5.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        stockNews.forEach { news ->
                            NewsRow(news = news)
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

data class RatioPair(val name: String, val value: String, val note: String)

@Composable
fun RatioBox(item: RatioPair, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = item.name, fontSize = 9.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            Text(text = item.note, fontSize = 8.5.sp, color = TextSecondary)
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 8.5.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary
        )
    }
}

@Composable
fun NewsRow(news: StockNews) {
    val sentimentColor = when (news.sentiment) {
        "Bullish" -> GainGreen
        "Bearish" -> LossRed
        else -> AccentCyan
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF19202D))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = news.source,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
                Text(
                    text = "• ${news.timeAgo}",
                    fontSize = 9.sp,
                    color = TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(sentimentColor.copy(alpha = 0.2f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = news.sentiment,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = sentimentColor
                )
            }
        }

        Text(
            text = news.headline,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            lineHeight = 15.sp
        )

        Text(
            text = news.summary,
            fontSize = 10.sp,
            color = TextSecondary,
            lineHeight = 13.sp
        )
    }
}
