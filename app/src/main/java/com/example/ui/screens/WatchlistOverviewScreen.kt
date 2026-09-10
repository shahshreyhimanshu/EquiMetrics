package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Stock
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MarketViewModel
import java.util.Locale

@Composable
fun WatchlistOverviewScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf("All Listed") }
    var activeTabMode by remember { mutableStateOf("Watchlists") } // "Watchlists" or "All Stocks"
    val categories = listOf("All Listed", "Nifty 50", "Banking", "IT", "Auto", "Energy", "FMCG", "Pharma", "Metals", "Defence", "Consumer")

    val baseStockPool = if (activeTabMode == "Watchlists") uiState.watchlistStocks else uiState.allStocks
    val filteredStocks = remember(baseStockPool, selectedCategory) {
        baseStockPool.filter { stock ->
            val matchesCategory = selectedCategory == "All Listed" || selectedCategory == "All" ||
                stock.tags.any { it.contains(selectedCategory, ignoreCase = true) } ||
                stock.sector.contains(selectedCategory, ignoreCase = true)

            matchesCategory
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Pro Tier Status Banner & Quick Switcher
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleProTier() },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = if (uiState.isPro) Color(0xFF0D2818) else Color(0xFF1B2333)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.isPro) GainGreen.copy(alpha = 0.5f) else BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPro) Icons.Default.CheckCircle else Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (uiState.isPro) GainGreen else AccentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (uiState.isPro) "PRO TIER ACTIVE (₹299/mo)" else "FREE TIER (Standard)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (uiState.isPro) GainGreen.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (uiState.isPro) "UNLIMITED" else "RESTRICTED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.isPro) GainGreen else AccentBlue
                                    )
                                }
                            }
                            Text(
                                text = if (uiState.isPro) "10-Yr Data • WhatsApp Alerts • Multi-Broker • Uncapped Watchlists" else "3-Yr Data • 1 Watchlist • Delayed Push • Tap to toggle/test Pro",
                                fontSize = 9.5.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Text(
                        text = if (uiState.isPro) "Switch Free" else "Upgrade ₹299",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isPro) TextMuted else AccentBlue
                    )
                }
            }
        }

        item {
            // Portfolio Net Worth & Analytics Overview Card
            PortfolioOverviewCard(
                totalNetWorth = uiState.totalNetWorth,
                totalPnl = uiState.totalPortfolioPnl,
                invested = uiState.totalPortfolioInvested,
                cash = uiState.virtualCashBalance
            )
        }

        item {
            // Watchlist Navigation & Multi-Chart Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab Mode Selector (Custom Watchlists vs All 50 Stocks)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTabMode == "Watchlists") AccentBlue else Color.Transparent)
                            .clickable { activeTabMode = "Watchlists" }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Watchlists (${uiState.watchlists.size})",
                            fontSize = 11.sp,
                            fontWeight = if (activeTabMode == "Watchlists") FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTabMode == "Watchlists") Color.White else TextMuted
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (activeTabMode == "All Stocks") AccentBlue else Color.Transparent)
                            .clickable { activeTabMode = "All Stocks" }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "All Stocks (${uiState.allStocks.size})",
                            fontSize = 11.sp,
                            fontWeight = if (activeTabMode == "All Stocks") FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTabMode == "All Stocks") Color.White else TextMuted
                        )
                    }
                }

                // Power-user Multi-Chart button
                Button(
                    onClick = { viewModel.toggleMultiChartLayout() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.isPro) AccentPurple else BorderSubtle),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.GridView, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Multi-Chart", fontSize = 10.5.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (activeTabMode == "Watchlists") {
            item {
                // Watchlist List Pills with + Add Custom Watchlist button
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.watchlists) { wl ->
                        val isSelected = wl.id == uiState.activeWatchlistId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentBlueSubtle else DarkSurfaceVariant)
                                .border(1.dp, if (isSelected) AccentBlue else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { viewModel.selectWatchlist(wl.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = wl.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x33FFFFFF))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("${wl.stockSymbols.size}", fontSize = 8.5.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    // Add Custom Watchlist Button
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { viewModel.openCreateWatchlistDialog() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(12.dp))
                                Text("+ New List", fontSize = 11.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            // Category Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AccentBlue else DarkSurfaceVariant)
                            .border(1.dp, if (isSelected) AccentBlue else BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary
                        )
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
                    text = "ALL LISTED EQUITIES (${filteredStocks.size} OF ${uiState.stocks.size})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, if (uiState.feedConfig.isConnected) GainGreen.copy(alpha = 0.5f) else BorderSubtle, RoundedCornerShape(4.dp))
                            .clickable { viewModel.openFeedModal() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.feedConfig.isConnected) GainGreen else LossRed)
                            )
                            Text(
                                text = "${uiState.feedConfig.source.title.split(" ").first()} Feed",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.feedConfig.isConnected) GainGreen else TextMuted
                            )
                        }
                    }
                    Text(
                        text = "No Buy/Sell Advice",
                        fontSize = 9.5.sp,
                        color = AccentCyan
                    )
                }
            }
        }

        item {
            // Non-advisory compliance banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "EquiMetrics does not recommend buying or selling stocks. All metrics are factual & neutral.",
                        fontSize = 9.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        items(filteredStocks, key = { it.symbol }) { stock ->
            val hasRecentFlash = uiState.lastPriceFlash[stock.symbol]
            val isInActiveWatchlist = uiState.activeWatchlist.stockSymbols.contains(stock.symbol)
            WatchlistStockItem(
                stock = stock,
                hasFlash = hasRecentFlash,
                isInWatchlist = isInActiveWatchlist,
                onClick = { viewModel.selectStock(stock.symbol) },
                onOpenAlert = { viewModel.openAlertDialog(stock.symbol) },
                onToggleWatchlist = { viewModel.toggleStockInActiveWatchlist(stock.symbol) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PortfolioOverviewCard(
    totalNetWorth: Double,
    totalPnl: Double,
    invested: Double,
    cash: Double
) {
    val isPnlPositive = totalPnl >= 0
    val pnlColor = if (isPnlPositive) GainGreen else LossRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "TOTAL PORTFOLIO NET WORTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x222563EB))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Factual Metric",
                        fontSize = 9.sp,
                        color = AccentCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Total Net worth amount
            Text(
                text = "₹${String.format(Locale.US, "%,.2f", totalNetWorth)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )

            // P&L
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isPnlPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = pnlColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${String.format(Locale.US, "%+,.2f", totalPnl)} (${String.format(Locale.US, "%+.2f%%", if (invested > 0) (totalPnl / invested) * 100 else 0.0)})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = pnlColor
                )
                Text(
                    text = "• Overall P&L",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Balance curve mini Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val curveValues = listOf(100f, 101.5f, 100.8f, 103f, 102.4f, 105f, 107.2f, 106f, 109.5f)
                    val minV = curveValues.minOrNull() ?: 100f
                    val maxV = curveValues.maxOrNull() ?: 110f
                    val range = (maxV - minV).coerceAtLeast(1f)
                    val stepX = size.width / (curveValues.size - 1)

                    val path = Path()
                    curveValues.forEachIndexed { i, v ->
                        val x = i * stepX
                        val y = size.height - ((v - minV) / range) * size.height * 0.85f - 4f
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }

                    drawPath(
                        path = path,
                        color = AccentBlue,
                        style = Stroke(width = 2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Allocation breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Invested in Equities", fontSize = 9.sp, color = TextMuted)
                    Text("₹${String.format(Locale.US, "%,.0f", invested)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("Current Value", fontSize = 9.sp, color = TextMuted)
                    Text("₹${String.format(Locale.US, "%,.0f", invested + totalPnl)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("Liquid Cash", fontSize = 9.sp, color = TextMuted)
                    Text("₹${String.format(Locale.US, "%,.0f", cash)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun WatchlistStockItem(
    stock: Stock,
    hasFlash: Boolean?,
    isInWatchlist: Boolean,
    onClick: () -> Unit,
    onOpenAlert: () -> Unit,
    onToggleWatchlist: () -> Unit
) {
    val isGain = stock.change >= 0
    val color = if (isGain) GainGreen else LossRed

    val flashBgColor by animateColorAsState(
        targetValue = when (hasFlash) {
            true -> GainGreen.copy(alpha = 0.18f)
            false -> LossRed.copy(alpha = 0.18f)
            null -> DarkSurface
        },
        animationSpec = tween(durationMillis = 350),
        label = "stockFlash"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = flashBgColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Symbol, Company & Sector
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stock.symbol,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = stock.sector,
                            fontSize = 8.5.sp,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stock.companyName,
                    fontSize = 10.5.sp,
                    color = TextMuted,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Vol: ${String.format(Locale.US, "%,d", stock.volume)} • P/E: ${stock.ratios.peRatio}",
                    fontSize = 9.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Right: Live Price, Change, and Quick Trade/Alert/Star
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${String.format(Locale.US, "%,.2f", stock.currentPrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isGain) Color(0x2200C805) else Color(0x22FF3B30))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%+,.2f", stock.change)} (${String.format(Locale.US, "%+.2f%%", stock.changePct)})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = color
                        )
                    }
                }

                // Alert Button
                IconButton(
                    onClick = onOpenAlert,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Set Alert",
                        tint = AccentCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Watchlist Star Toggle
                IconButton(
                    onClick = onToggleWatchlist,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isInWatchlist) Color(0x33F59E0B) else DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Watchlist",
                        tint = if (isInWatchlist) Color(0xFFF59E0B) else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
