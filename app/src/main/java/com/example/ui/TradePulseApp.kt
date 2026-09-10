package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleDataProvider
import com.example.model.MarketFeedSource
import com.example.model.OrderAction
import com.example.model.OrderType
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MarketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradePulseApp(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Handle system back button when in Stock Profile
    BackHandler(enabled = uiState.currentScreen == AppScreen.STOCK_DETAIL) {
        viewModel.setScreen(AppScreen.OVERVIEW)
    }

    val activeStock = remember(uiState.stocks, uiState.selectedStockSymbol) {
        uiState.stocks.find { it.symbol == uiState.selectedStockSymbol } ?: uiState.stocks.first()
    }

    val earningsSummary = remember(uiState.selectedStockSymbol) {
        SampleDataProvider.earningsSummaries[uiState.selectedStockSymbol]
    }

    val isPro = uiState.subscription.isProSubscribed

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(DarkSurface)) {
                // Main App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.currentScreen == AppScreen.STOCK_DETAIL) {
                            IconButton(
                                onClick = { viewModel.setScreen(AppScreen.OVERVIEW) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back to Watchlist",
                                    tint = TextPrimary
                                )
                            }
                        }

                        // Logo & Brand Name
                        Text(
                            text = "EQUIMETRICS",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2563EB))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "INDIA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Stock Search Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                                .clickable { viewModel.openSearchDialog() }
                                .testTag("stock_search_button")
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Equities",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Search",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }

                        // Live Market Feed Gateway Chip (Angel One / Upstox)
                        val feedSourceLabel = if (uiState.feedConfig.source == MarketFeedSource.ANGEL_ONE) "Angel" else "Upstox"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (uiState.feedConfig.isConnected) GainGreen.copy(alpha = 0.5f) else LossRed.copy(alpha = 0.4f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.openFeedModal() }
                                .testTag("feed_gateway_chip")
                                .padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(if (uiState.feedConfig.isConnected) GainGreen else LossRed)
                                )
                                Text(
                                    text = "$feedSourceLabel • ${uiState.feedConfig.latencyMs}ms",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (uiState.feedConfig.isConnected) GainGreen else TextMuted
                                )
                            }
                        }

                        // Pro / Subscription Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isPro) GainGreen.copy(alpha = 0.2f)
                                    else Color(0x332563EB)
                                )
                                .border(
                                    1.dp,
                                    if (isPro) GainGreen else Color(0xFF2563EB),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { viewModel.openUpgradeModal() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPro) Icons.Default.CheckCircle else Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = if (isPro) GainGreen else Color(0xFF60A5FA),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isPro) "PRO ACTIVE" else "UPGRADE PRO",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPro) GainGreen else Color(0xFF93C5FD)
                                )
                            }
                        }
                    }
                }

                // Global Indices Marquee Ticker
                GlobalIndicesTicker(indices = uiState.indices)

                // Navigation Category Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NavTabItem("Watchlist", Icons.Default.ShowChart, uiState.currentScreen == AppScreen.OVERVIEW || uiState.currentScreen == AppScreen.STOCK_DETAIL) {
                        viewModel.setScreen(AppScreen.OVERVIEW)
                    }
                    NavTabItem("AI Suite", Icons.Default.AutoAwesome, uiState.currentScreen == AppScreen.AI_SUITE) {
                        viewModel.setScreen(AppScreen.AI_SUITE)
                    }
                    NavTabItem("Options Chain", Icons.Default.Layers, uiState.currentScreen == AppScreen.OPTIONS_CHAIN) {
                        viewModel.setScreen(AppScreen.OPTIONS_CHAIN)
                    }
                    NavTabItem("Screener", Icons.Default.FilterAlt, uiState.currentScreen == AppScreen.SCREENER) {
                        viewModel.setScreen(AppScreen.SCREENER)
                    }
                    NavTabItem("Macro Sim", Icons.Default.Speed, uiState.currentScreen == AppScreen.MACRO_SIMULATOR) {
                        viewModel.setScreen(AppScreen.MACRO_SIMULATOR)
                    }
                    NavTabItem("Backtest", Icons.Default.QueryStats, uiState.currentScreen == AppScreen.BACKTESTER) {
                        viewModel.setScreen(AppScreen.BACKTESTER)
                    }
                    NavTabItem("Brokers (AA)", Icons.Default.AccountBalance, uiState.currentScreen == AppScreen.BROKER_SYNC) {
                        viewModel.setScreen(AppScreen.BROKER_SYNC)
                    }
                }

                Divider(color = BorderSubtle, thickness = 1.dp)
            }
        },
        bottomBar = {
            // Mandatory Sticky SEBI Disclaimer Banner
            StickySEBIDisclaimer()
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                AppScreen.OVERVIEW -> WatchlistOverviewScreen(viewModel = viewModel)
                AppScreen.STOCK_DETAIL -> StockDetailScreen(stock = activeStock, viewModel = viewModel)
                AppScreen.AI_SUITE -> AiSuiteScreen(viewModel = viewModel)
                AppScreen.OPTIONS_CHAIN -> OptionsChainScreen()
                AppScreen.SCREENER -> ScreenerScreen(viewModel = viewModel)
                AppScreen.MACRO_SIMULATOR -> MacroSimulatorScreen(viewModel = viewModel)
                AppScreen.PAPER_TRADING -> WatchlistOverviewScreen(viewModel = viewModel)
                AppScreen.BACKTESTER -> BacktestScreen()
                AppScreen.BROKER_SYNC -> BrokerAggregatorScreen(viewModel = viewModel)
            }
        }
    }

    // Stock Search Modal
    StockSearchModal(
        stocks = uiState.allStocks,
        isOpen = uiState.isSearchDialogOpen,
        onDismiss = { viewModel.closeSearchDialog() },
        onSelectStock = { symbol -> viewModel.selectStockFromSearch(symbol) }
    )

    // Razorpay Subscription Modal
    RazorpayUpgradeModal(
        isOpen = uiState.subscription.showUpgradeModal,
        onDismiss = { viewModel.closeUpgradeModal() },
        onPaymentComplete = { planName ->
            viewModel.completeSubscriptionPayment(planName)
        }
    )

    // Earnings Call Digest Modal
    EarningsSummaryModal(
        summary = earningsSummary,
        isOpen = uiState.isEarningsDrawerOpen,
        onDismiss = { viewModel.closeEarningsDrawer() }
    )

    // Custom Watchlist Creation Modal
    CustomWatchlistModal(
        isOpen = uiState.isCreateWatchlistDialogOpen,
        allStocks = uiState.allStocks,
        onDismiss = { viewModel.closeCreateWatchlistDialog() },
        onCreateWatchlist = { name, symbols ->
            viewModel.createCustomWatchlist(name, symbols)
        }
    )

    // Stock Price Alert Modal (WhatsApp / Telegram / Push)
    val alertStock = uiState.allStocks.find { it.symbol == uiState.alertTargetSymbol } ?: activeStock
    StockAlertModal(
        isOpen = uiState.isAlertDialogOpen,
        stock = alertStock,
        isPro = isPro,
        onDismiss = { viewModel.closeAlertDialog() },
        onSaveAlert = { condition, targetPrice, channel, contact ->
            viewModel.saveAlert(alertStock.symbol, condition, targetPrice, channel, contact)
        },
        onUpgradeRequired = {
            viewModel.openUpgradeModal()
        }
    )

    // Multi-Chart Grid Modal (2x2 / 4-split Layout for Power Users)
    MultiChartGridModal(
        isOpen = uiState.isMultiChartActive,
        allStocks = uiState.allStocks,
        gridSymbols = uiState.multiChartGridSymbols,
        onSymbolChange = { index, symbol ->
            viewModel.updateMultiChartSymbol(index, symbol)
        },
        onDismiss = { viewModel.toggleMultiChartLayout() }
    )

    // Market Feed Gateway Modal (Angel One SmartAPI & Upstox Developer API)
    MarketFeedModal(
        isOpen = uiState.isFeedModalOpen,
        feedConfig = uiState.feedConfig,
        onDismiss = { viewModel.closeFeedModal() },
        onSelectSource = { source -> viewModel.setFeedSource(source) },
        onUpdateCredentials = { angelKey, clientCode, totp, upstoxToken ->
            viewModel.updateFeedCredentials(angelKey, clientCode, totp, upstoxToken)
        },
        onTestAndConnect = { viewModel.testAndConnectFeed() },
        onToggleConnection = { viewModel.toggleFeedConnection() }
    )
}

@Composable
fun NavTabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AccentBlue else DarkSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) AccentBlue else BorderSubtle.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextMuted,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}
