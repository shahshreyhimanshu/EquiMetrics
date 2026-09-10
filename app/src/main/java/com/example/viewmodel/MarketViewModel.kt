package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleDataProvider
import com.example.model.*
import com.example.service.MarketFeedService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

enum class AppScreen(val title: String) {
    OVERVIEW("Watchlist & Portfolio"),
    STOCK_DETAIL("EquiMetrics Profile"),
    AI_SUITE("AI Suite"),
    OPTIONS_CHAIN("Options Matrix"),
    SCREENER("Stock Screener"),
    MACRO_SIMULATOR("Macro Simulator"),
    PAPER_TRADING("Paper Trading"),
    BACKTESTER("Strategy Backtest"),
    BROKER_SYNC("Broker AA")
}

data class ScreenerFilter(
    val queryText: String = "",
    val maxPe: Float = 100f,
    val maxDebtToEquity: Float = 10f,
    val minRoe: Float = 0f,
    val minDivYield: Float = 0f,
    val selectedSector: String = "All Sectors"
)

data class MacroStressState(
    val crudeOilShiftPct: Float = 0f, // e.g. -20% to +30%
    val rbiRepoRateShiftBps: Float = 0f, // e.g. -50 bps to +100 bps
    val usdinrShiftPct: Float = 0f // e.g. -5% to +10%
) {
    val estimatedPortfolioImpactPct: Float
        get() {
            // Analytical macro sensitivity model
            val crudeImpact = (crudeOilShiftPct * -0.18f)
            val rateImpact = (rbiRepoRateShiftBps * -0.015f)
            val fxImpact = (usdinrShiftPct * 0.12f)
            return (crudeImpact + rateImpact + fxImpact).coerceIn(-25f, 25f)
        }
}

data class BacktestConfig(
    val strategyName: String = "20 EMA Crosses Above 50 EMA",
    val selectedSymbol: String = "RELIANCE",
    val testPeriodMonths: Int = 12,
    val stopLossPct: Float = 3.0f,
    val targetPct: Float = 6.0f
)

data class BacktestResult(
    val totalTrades: Int,
    val winTrades: Int,
    val lossTrades: Int,
    val winRatePct: Double,
    val totalReturnPct: Double,
    val maxDrawdownPct: Double,
    val profitFactor: Double,
    val equityCurvePoints: List<Float>
)

data class SubscriptionState(
    val isProSubscribed: Boolean = false,
    val planType: String = "Annual Pro (₹2,999/yr)",
    val expiryDate: String = "09 Sep 2027",
    val paymentId: String = "pay_rzp_demo_9843",
    val showUpgradeModal: Boolean = false
)

data class MarketUiState(
    val indices: List<MarketIndex> = SampleDataProvider.marketIndices,
    val stocks: List<Stock> = SampleDataProvider.stockList,
    val selectedStockSymbol: String = "RELIANCE",
    val activeTimeframe: Timeframe = Timeframe.ONE_DAY,
    val chartType: ChartType = ChartType.CANDLESTICK,
    val showEma20: Boolean = true,
    val showEma50: Boolean = true,
    val showRsi: Boolean = true,
    val showVolume: Boolean = true,
    val currentScreen: AppScreen = AppScreen.OVERVIEW,
    val lastPriceFlash: Map<String, Boolean> = emptyMap(), // true = green flash, false = red flash
    
    // Screener
    val screenerFilter: ScreenerFilter = ScreenerFilter(),
    
    // Macro Simulator
    val macroState: MacroStressState = MacroStressState(),
    
    // Paper Trading
    val virtualCashBalance: Double = 1000000.0, // Initial ₹10 Lakhs
    val paperPositions: List<PaperPosition> = listOf(
        PaperPosition("RELIANCE", "Reliance Industries Ltd", 50, 2920.0, 2985.40, OrderType.MARKET),
        PaperPosition("TCS", "Tata Consultancy Services Ltd", 25, 4280.0, 4320.15, OrderType.MARKET),
        PaperPosition("HDFCBANK", "HDFC Bank Ltd", 100, 1610.0, 1642.50, OrderType.MARKET)
    ),
    val paperTradeLogs: List<PaperTradeLog> = listOf(
        PaperTradeLog("TX1", "08 Sep, 10:15", "RELIANCE", OrderAction.BUY, OrderType.MARKET, 50, 2920.0, 146000.0),
        PaperTradeLog("TX2", "06 Sep, 11:45", "TCS", OrderAction.BUY, OrderType.MARKET, 25, 4280.0, 107000.0),
        PaperTradeLog("TX3", "04 Sep, 14:20", "HDFCBANK", OrderAction.BUY, OrderType.MARKET, 100, 1610.0, 161000.0)
    ),
    
    // Strategy Backtest
    val backtestConfig: BacktestConfig = BacktestConfig(),
    val backtestResult: BacktestResult = BacktestResult(
        totalTrades = 48,
        winTrades = 31,
        lossTrades = 17,
        winRatePct = 64.58,
        totalReturnPct = 28.4,
        maxDrawdownPct = -6.2,
        profitFactor = 2.15,
        equityCurvePoints = listOf(100f, 103f, 101f, 106f, 104f, 110f, 114f, 111f, 118f, 122f, 120f, 128.4f)
    ),
    
    // Broker Accounts
    val brokerAccounts: List<BrokerAccount> = SampleDataProvider.brokerAccounts,
    
    // Subscription
    val subscription: SubscriptionState = SubscriptionState(),
    
    // Order Dialog
    val isOrderDialogOpen: Boolean = false,
    val orderDialogSymbol: String = "RELIANCE",
    
    // Earnings summary drawer
    val isEarningsDrawerOpen: Boolean = false,

    // Stock Search Dialog
    val isSearchDialogOpen: Boolean = false,
    val searchQuery: String = "",

    // Pro Tier Workflows & Power Tools
    val watchlists: List<Watchlist> = SampleDataProvider.initialWatchlists,
    val activeWatchlistId: String = "wl_core",
    val alerts: List<StockAlert> = listOf(
        StockAlert("alt1", "RELIANCE", "52-Week High Breakout", 3217.90, AlertChannel.WHATSAPP, "+91 98765 43210", true),
        StockAlert("alt2", "TCS", "Crosses Above", 4400.0, AlertChannel.TELEGRAM, "@equi_trader", true),
        StockAlert("alt3", "HDFCBANK", "% Move > 3%", 1700.0, AlertChannel.PUSH, "", true)
    ),
    val stockNotes: Map<String, List<StockNote>> = mapOf(
        "RELIANCE" to listOf(
            StockNote("n1", "RELIANCE", "New energy Jamnagar giga-factories commissioning on track for H2 FY27. Retail footfall up 14% YoY.", "08 Sep 2026", "Capex Cycle"),
            StockNote("n2", "RELIANCE", "Petchem margins recovering with favorable Asian crack spreads. Monitor debt ratios.", "01 Sep 2026", "Quarterly Watch")
        ),
        "TCS" to listOf(
            StockNote("n3", "TCS", "Record $8.3B TCV deal pipeline. GenAI engagement count doubled. Operating margin solid at 24.7%.", "05 Sep 2026", "Order Book")
        ),
        "HDFCBANK" to listOf(
            StockNote("n4", "HDFCBANK", "LDR ratio improved 220 bps. Asset quality healthy with gross NPA down to 1.33%.", "04 Sep 2026", "Asset Quality")
        )
    ),
    val stockChecklists: Map<String, ResearchChecklist> = mapOf(
        "RELIANCE" to ResearchChecklist(hasMoat = true, hasCleanBalanceSheet = true, hasHighCapitalAllocation = true, hasMarginOfSafety = true, hasIndustryTailwinds = true),
        "TCS" to ResearchChecklist(hasMoat = true, hasCleanBalanceSheet = true, hasHighCapitalAllocation = true, hasMarginOfSafety = false, hasIndustryTailwinds = true),
        "HDFCBANK" to ResearchChecklist(hasMoat = true, hasCleanBalanceSheet = true, hasHighCapitalAllocation = true, hasMarginOfSafety = true, hasIndustryTailwinds = true)
    ),
    val aiEarningsSummariesUsedThisMonth: Int = 1, // Free tier limit is 2
    val customFormula: String = "((ROCE > 15) AND (Free Cash Flow / Net Profit > 0.8) AND (Debt to Equity < 0.3))",
    val isMultiChartActive: Boolean = false,
    val multiChartGridSymbols: List<String> = listOf("RELIANCE", "TCS", "HDFCBANK", "INFY"),
    val backtestYears: Int = 1, // 1 year for free, up to 10 for Pro
    val rrgTimeframe: String = "3M",
    val activeScreenerTab: Int = 0, // 0: Pre-set basic, 1: Custom Formula (Pro), 2: RRG & Sector Matrix (Pro)
    val isAlertDialogOpen: Boolean = false,
    val alertTargetSymbol: String = "RELIANCE",
    val isCreateWatchlistDialogOpen: Boolean = false,
    val isMultiBrokerActive: Boolean = false,

    // Real-Time Market Feed Gateway (Angel One SmartAPI & Upstox Developer API)
    val feedConfig: FeedConfig = FeedConfig(),
    val isFeedModalOpen: Boolean = false
) {
    val isPro: Boolean get() = subscription.isProSubscribed

    val allStocks: List<Stock> get() = stocks

    val activeWatchlist: Watchlist
        get() = watchlists.find { it.id == activeWatchlistId } ?: watchlists.first()

    val watchlistStocks: List<Stock>
        get() {
            val symbols = activeWatchlist.stockSymbols.toSet()
            return stocks.filter { symbols.contains(it.symbol) }
        }

    val selectedStock: Stock
        get() = stocks.find { it.symbol == selectedStockSymbol } ?: stocks.first()

    val totalPortfolioInvested: Double
        get() = paperPositions.sumOf { it.investmentValue }

    val totalPortfolioCurrent: Double
        get() = paperPositions.sumOf { it.currentValue }

    val totalPortfolioPnl: Double
        get() = totalPortfolioCurrent - totalPortfolioInvested

    val totalNetWorth: Double
        get() = virtualCashBalance + totalPortfolioCurrent
}

class MarketViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    init {
        startRealTimeTickSimulation()
    }

    private fun startRealTimeTickSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(1600) // Fast live tick streaming interval
                val config = _uiState.value.feedConfig
                if (config.isConnected) {
                    // Try to poll live quotes if keys configured
                    val symbolsToFetch = listOf("RELIANCE", "TCS", "HDFCBANK", "INFY", "SBIN", "ICICIBANK")
                    val liveTicks = if (config.source == MarketFeedSource.UPSTOX && config.upstoxAccessToken.isNotBlank() && config.upstoxAccessToken != "MY_UPSTOX_ACCESS_TOKEN") {
                        MarketFeedService.fetchUpstoxQuotes(config.upstoxAccessToken, symbolsToFetch)
                    } else if (config.source == MarketFeedSource.ANGEL_ONE && config.angelOneApiKey.isNotBlank() && config.angelOneApiKey != "MY_ANGEL_ONE_API_KEY") {
                        MarketFeedService.fetchAngelOneQuotes(config.angelOneApiKey, config.angelOneTotpSecret, symbolsToFetch)
                    } else {
                        emptyList()
                    }

                    updateLivePriceTicks(liveTicks)
                }
            }
        }
    }

    private fun updateLivePriceTicks(liveApiTicks: List<MarketFeedTick> = emptyList()) {
        val liveTicksMap = liveApiTicks.associateBy { it.symbol }

        _uiState.update { state ->
            val flashMap = mutableMapOf<String, Boolean>()
            val updatedStocks = state.stocks.map { stock ->
                val apiTick = liveTicksMap[stock.symbol]
                val newPrice = if (apiTick != null && apiTick.ltp > 0.0) {
                    apiTick.ltp
                } else {
                    val tickDelta = (Random.nextDouble() - 0.49) * (stock.currentPrice * 0.0025)
                    String.format(Locale.US, "%.2f", (stock.currentPrice + tickDelta).coerceAtLeast(1.0)).toDouble()
                }

                val isGain = newPrice >= stock.currentPrice
                flashMap[stock.symbol] = isGain
                val newHigh = maxOf(stock.dayHigh, newPrice)
                val newLow = minOf(stock.dayLow, newPrice)
                val newVol = stock.volume + Random.nextInt(250, 2400)
                
                // Update 5-level market depth based on live tick
                val spread = 0.05
                val updatedDepth = (1..5).map { level ->
                    MarketDepthItem(
                        bidOrders = 3 + level * 2 + Random.nextInt(4),
                        bidQty = 250 + level * 100 + Random.nextInt(120),
                        bidPrice = String.format(Locale.US, "%.2f", newPrice - (level * spread)).toDouble(),
                        askPrice = String.format(Locale.US, "%.2f", newPrice + (level * spread)).toDouble(),
                        askQty = 220 + level * 95 + Random.nextInt(110),
                        askOrders = 3 + level * 2 + Random.nextInt(3)
                    )
                }

                stock.copy(
                    currentPrice = newPrice,
                    dayHigh = newHigh,
                    dayLow = newLow,
                    volume = newVol,
                    marketDepth = updatedDepth
                )
            }

            val updatedIndices = state.indices.map { index ->
                val delta = (Random.nextDouble() - 0.48) * (index.currentValue * 0.001)
                val newIdxVal = String.format(Locale.US, "%.2f", index.currentValue + delta).toDouble()
                index.copy(currentValue = newIdxVal)
            }

            // Also update current paper positions with live stock prices
            val updatedPositions = state.paperPositions.map { pos ->
                val currentStock = updatedStocks.find { it.symbol == pos.symbol }
                if (currentStock != null) {
                    pos.copy(currentPrice = currentStock.currentPrice)
                } else {
                    pos
                }
            }

            val newPackets = state.feedConfig.totalTicksReceived + (if (liveApiTicks.isNotEmpty()) liveApiTicks.size else 1)
            val updatedConfig = state.feedConfig.copy(
                totalTicksReceived = newPackets,
                lastSyncTime = "Just now"
            )

            state.copy(
                stocks = updatedStocks,
                indices = updatedIndices,
                paperPositions = updatedPositions,
                lastPriceFlash = flashMap,
                feedConfig = updatedConfig
            )
        }
    }

    fun selectStock(symbol: String) {
        _uiState.update {
            it.copy(
                selectedStockSymbol = symbol,
                currentScreen = AppScreen.STOCK_DETAIL
            )
        }
    }

    fun openSearchDialog() {
        _uiState.update { it.copy(isSearchDialogOpen = true, searchQuery = "") }
    }

    fun closeSearchDialog() {
        _uiState.update { it.copy(isSearchDialogOpen = false, searchQuery = "") }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectStockFromSearch(symbol: String) {
        _uiState.update {
            it.copy(
                selectedStockSymbol = symbol,
                currentScreen = AppScreen.STOCK_DETAIL,
                isSearchDialogOpen = false,
                searchQuery = ""
            )
        }
    }

    fun setScreen(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setTimeframe(timeframe: Timeframe) {
        _uiState.update { it.copy(activeTimeframe = timeframe) }
    }

    fun toggleChartType() {
        _uiState.update {
            it.copy(
                chartType = if (it.chartType == ChartType.CANDLESTICK) ChartType.LINE else ChartType.CANDLESTICK
            )
        }
    }

    fun toggleIndicator(indicator: String) {
        _uiState.update {
            when (indicator) {
                "EMA20" -> it.copy(showEma20 = !it.showEma20)
                "EMA50" -> it.copy(showEma50 = !it.showEma50)
                "RSI" -> it.copy(showRsi = !it.showRsi)
                "VOLUME" -> it.copy(showVolume = !it.showVolume)
                else -> it
            }
        }
    }

    fun updateScreenerFilter(newFilter: ScreenerFilter) {
        _uiState.update { it.copy(screenerFilter = newFilter) }
    }

    fun updateMacroShift(crude: Float, repoBps: Float, fx: Float) {
        _uiState.update {
            it.copy(
                macroState = MacroStressState(
                    crudeOilShiftPct = crude,
                    rbiRepoRateShiftBps = repoBps,
                    usdinrShiftPct = fx
                )
            )
        }
    }

    fun openOrderDialog(symbol: String) {
        _uiState.update {
            it.copy(
                isOrderDialogOpen = true,
                orderDialogSymbol = symbol
            )
        }
    }

    fun closeOrderDialog() {
        _uiState.update { it.copy(isOrderDialogOpen = false) }
    }

    fun openEarningsDrawer() {
        _uiState.update { it.copy(isEarningsDrawerOpen = true) }
    }

    fun closeEarningsDrawer() {
        _uiState.update { it.copy(isEarningsDrawerOpen = false) }
    }

    fun openUpgradeModal() {
        _uiState.update {
            it.copy(
                subscription = it.subscription.copy(showUpgradeModal = true)
            )
        }
    }

    fun closeUpgradeModal() {
        _uiState.update {
            it.copy(
                subscription = it.subscription.copy(showUpgradeModal = false)
            )
        }
    }

    fun completeSubscriptionPayment(plan: String) {
        val now = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, 1)
        val expiry = SimpleDateFormat("dd MMM yyyy", Locale.US).format(cal.time)

        _uiState.update {
            it.copy(
                subscription = SubscriptionState(
                    isProSubscribed = true,
                    planType = plan,
                    expiryDate = expiry,
                    paymentId = "pay_rzp_" + (100000..999999).random(),
                    showUpgradeModal = false
                )
            )
        }
    }

    fun executePaperOrder(
        symbol: String,
        action: OrderAction,
        orderType: OrderType,
        quantity: Int, // strictly full share quantities
        price: Double
    ) {
        if (quantity <= 0) return

        val now = SimpleDateFormat("dd MMM, HH:mm", Locale.US).format(Date())
        val totalCost = quantity * price

        _uiState.update { state ->
            val currentPositions = state.paperPositions.toMutableList()
            var newCash = state.virtualCashBalance

            if (action == OrderAction.BUY) {
                if (newCash < totalCost) return@update state // Insufficient cash
                newCash -= totalCost
                val existingIndex = currentPositions.indexOfFirst { it.symbol == symbol }
                if (existingIndex >= 0) {
                    val existing = currentPositions[existingIndex]
                    val newTotalQty = existing.quantity + quantity
                    val newAvgPrice = ((existing.quantity * existing.avgPrice) + totalCost) / newTotalQty
                    currentPositions[existingIndex] = existing.copy(
                        quantity = newTotalQty,
                        avgPrice = newAvgPrice,
                        currentPrice = price
                    )
                } else {
                    val stock = state.stocks.find { it.symbol == symbol }
                    currentPositions.add(
                        PaperPosition(
                            symbol = symbol,
                            companyName = stock?.companyName ?: symbol,
                            quantity = quantity,
                            avgPrice = price,
                            currentPrice = price,
                            orderType = orderType
                        )
                    )
                }
            } else {
                // SELL action
                val existingIndex = currentPositions.indexOfFirst { it.symbol == symbol }
                if (existingIndex < 0) return@update state // No position to sell
                val existing = currentPositions[existingIndex]
                val sellQty = minOf(existing.quantity, quantity)
                newCash += (sellQty * price)
                val remainingQty = existing.quantity - sellQty
                if (remainingQty <= 0) {
                    currentPositions.removeAt(existingIndex)
                } else {
                    currentPositions[existingIndex] = existing.copy(quantity = remainingQty)
                }
            }

            val newLog = PaperTradeLog(
                id = "TX_${System.currentTimeMillis() % 10000}",
                timestamp = now,
                symbol = symbol,
                action = action,
                orderType = orderType,
                quantity = quantity,
                executedPrice = price,
                totalAmount = totalCost,
                status = "Executed"
            )

            state.copy(
                virtualCashBalance = newCash,
                paperPositions = currentPositions,
                paperTradeLogs = listOf(newLog) + state.paperTradeLogs,
                isOrderDialogOpen = false
            )
        }
    }

    fun runBacktest(strategy: String, symbol: String) {
        val stock = _uiState.value.stocks.find { it.symbol == symbol } ?: _uiState.value.stocks.first()
        val randomWinRate = 56.0 + (Random.nextDouble() * 18.0)
        val trades = (30..65).random()
        val wins = (trades * (randomWinRate / 100.0)).roundToInt()
        val losses = trades - wins
        val retPct = (14.0 + Random.nextDouble() * 24.0)
        val ddPct = -(4.0 + Random.nextDouble() * 5.5)

        val curve = mutableListOf(100f)
        var cur = 100f
        for (i in 1..11) {
            cur += (Random.nextFloat() * 4.5f - 1.2f)
            curve.add(cur)
        }
        curve.add(100f + retPct.toFloat())

        _uiState.update {
            it.copy(
                backtestConfig = it.backtestConfig.copy(strategyName = strategy, selectedSymbol = symbol),
                backtestResult = BacktestResult(
                    totalTrades = trades,
                    winTrades = wins,
                    lossTrades = losses,
                    winRatePct = String.format(Locale.US, "%.1f", randomWinRate).toDouble(),
                    totalReturnPct = String.format(Locale.US, "%.1f", retPct).toDouble(),
                    maxDrawdownPct = String.format(Locale.US, "%.1f", ddPct).toDouble(),
                    profitFactor = String.format(Locale.US, "%.2f", 1.8 + Random.nextDouble() * 0.9).toDouble(),
                    equityCurvePoints = curve
                )
            )
        }
    }

    fun toggleBrokerConnection(brokerName: String) {
        _uiState.update { state ->
            val updated = state.brokerAccounts.map { b ->
                if (b.brokerName == brokerName) {
                    val willConnect = !b.isConnected
                    b.copy(
                        isConnected = willConnect,
                        totalHoldingsValue = if (willConnect) 420000.0 else 0.0,
                        totalInvestedValue = if (willConnect) 360000.0 else 0.0,
                        cashBalance = if (willConnect) 28000.0 else 0.0,
                        lastSyncTime = if (willConnect) "Just now" else "Not Connected"
                    )
                } else b
            }
            state.copy(brokerAccounts = updated)
        }
    }

    fun toggleProTier() {
        _uiState.update {
            it.copy(
                subscription = it.subscription.copy(isProSubscribed = !it.subscription.isProSubscribed)
            )
        }
    }

    // Watchlist Workflows
    fun selectWatchlist(id: String) {
        _uiState.update { it.copy(activeWatchlistId = id) }
    }

    fun openCreateWatchlistDialog() {
        if (!_uiState.value.isPro && _uiState.value.watchlists.size >= 1) {
            openUpgradeModal()
        } else {
            _uiState.update { it.copy(isCreateWatchlistDialogOpen = true) }
        }
    }

    fun closeCreateWatchlistDialog() {
        _uiState.update { it.copy(isCreateWatchlistDialogOpen = false) }
    }

    fun createCustomWatchlist(name: String, symbols: List<String>) {
        val newWl = Watchlist(
            id = "wl_" + System.currentTimeMillis(),
            name = name,
            stockSymbols = symbols,
            isDefault = false
        )
        _uiState.update {
            it.copy(
                watchlists = it.watchlists + newWl,
                activeWatchlistId = newWl.id,
                isCreateWatchlistDialogOpen = false
            )
        }
    }

    fun toggleStockInActiveWatchlist(symbol: String) {
        _uiState.update { state ->
            val curWl = state.activeWatchlist
            val isPresent = curWl.stockSymbols.contains(symbol)
            if (!state.isPro && !isPresent && curWl.stockSymbols.size >= 10) {
                // Free tier capped at 10 stocks
                return@update state.copy(subscription = state.subscription.copy(showUpgradeModal = true))
            }
            val newSymbols = if (isPresent) {
                curWl.stockSymbols - symbol
            } else {
                curWl.stockSymbols + symbol
            }
            val updatedLists = state.watchlists.map {
                if (it.id == curWl.id) it.copy(stockSymbols = newSymbols) else it
            }
            state.copy(watchlists = updatedLists)
        }
    }

    // Alerts Workflows
    fun openAlertDialog(symbol: String) {
        _uiState.update { it.copy(isAlertDialogOpen = true, alertTargetSymbol = symbol) }
    }

    fun closeAlertDialog() {
        _uiState.update { it.copy(isAlertDialogOpen = false) }
    }

    fun saveAlert(symbol: String, condition: String, threshold: Double, channel: AlertChannel, contact: String) {
        val state = _uiState.value
        if (!state.isPro) {
            if (channel != AlertChannel.PUSH || state.alerts.size >= 2) {
                closeAlertDialog()
                openUpgradeModal()
                return
            }
        }
        val newAlert = StockAlert(
            id = "alt_" + System.currentTimeMillis(),
            symbol = symbol,
            conditionType = condition,
            thresholdValue = threshold,
            channel = channel,
            contactInfo = contact,
            isActive = true
        )
        _uiState.update {
            it.copy(
                alerts = listOf(newAlert) + it.alerts,
                isAlertDialogOpen = false
            )
        }
    }

    fun deleteAlert(id: String) {
        _uiState.update { it.copy(alerts = it.alerts.filterNot { a -> a.id == id }) }
    }

    fun toggleAlertActive(id: String) {
        _uiState.update { state ->
            state.copy(alerts = state.alerts.map { if (it.id == id) it.copy(isActive = !it.isActive) else it })
        }
    }

    // Custom Stock Notes & Checklist
    fun addStockNote(symbol: String, text: String, tag: String) {
        if (!_uiState.value.isPro) {
            openUpgradeModal()
            return
        }
        val newNote = StockNote(
            id = "n_" + System.currentTimeMillis(),
            stockSymbol = symbol,
            noteText = text,
            timestamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(Date()),
            tag = tag
        )
        _uiState.update { state ->
            val existing = state.stockNotes[symbol] ?: emptyList()
            state.copy(stockNotes = state.stockNotes + (symbol to (listOf(newNote) + existing)))
        }
    }

    fun deleteStockNote(symbol: String, noteId: String) {
        _uiState.update { state ->
            val existing = state.stockNotes[symbol] ?: emptyList()
            state.copy(stockNotes = state.stockNotes + (symbol to existing.filterNot { it.id == noteId }))
        }
    }

    fun updateStockChecklist(symbol: String, checklist: ResearchChecklist) {
        if (!_uiState.value.isPro) {
            openUpgradeModal()
            return
        }
        _uiState.update { state ->
            state.copy(stockChecklists = state.stockChecklists + (symbol to checklist))
        }
    }

    // Screener Custom Formula & RRG
    fun updateCustomFormula(formula: String) {
        _uiState.update { it.copy(customFormula = formula) }
    }

    fun setScreenerTab(tab: Int) {
        if (tab > 0 && !_uiState.value.isPro) {
            openUpgradeModal()
            return
        }
        _uiState.update { it.copy(activeScreenerTab = tab) }
    }

    // Multi-Chart Layouts
    fun toggleMultiChartLayout() {
        if (!_uiState.value.isPro) {
            openUpgradeModal()
            return
        }
        _uiState.update { it.copy(isMultiChartActive = !it.isMultiChartActive) }
    }

    fun updateMultiChartSymbol(index: Int, symbol: String) {
        _uiState.update { state ->
            val current = state.multiChartGridSymbols.toMutableList()
            if (index in current.indices) {
                current[index] = symbol
            }
            state.copy(multiChartGridSymbols = current)
        }
    }

    // Backtest 10-Year simulation
    fun setBacktestYears(years: Int) {
        if (years > 1 && !_uiState.value.isPro) {
            openUpgradeModal()
            return
        }
        _uiState.update { it.copy(backtestYears = years) }
    }

    // Relative Rotation Graph timeframe
    fun setRrgTimeframe(tf: String) {
        _uiState.update { it.copy(rrgTimeframe = tf) }
    }

    // AI Earnings summary quota check
    fun consumeAiSummary(symbol: String): Boolean {
        if (_uiState.value.isPro) return true
        if (_uiState.value.aiEarningsSummariesUsedThisMonth >= 2) {
            openUpgradeModal()
            return false
        }
        _uiState.update { it.copy(aiEarningsSummariesUsedThisMonth = it.aiEarningsSummariesUsedThisMonth + 1) }
        return true
    }

    // Export 10-Year financials to CSV string
    fun generateFinancialCsv(symbol: String): String {
        val rows = SampleDataProvider.get10YearFinancials(symbol)
        val sb = StringBuilder()
        sb.append("Year,Revenue_Cr,EBITDA_Cr,PAT_Cr,EPS_INR,OperatingMargin_Pct,DebtToEquity,FCF_Cr,ROE_Pct\n")
        rows.forEach { r ->
            sb.append("${r.year},${r.revenueCr},${r.ebitdaCr},${r.patCr},${r.eps},${r.operatingMarginPct},${r.debtToEquity},${r.freeCashFlowCr},${r.roePct}\n")
        }
        return sb.toString()
    }

    // Real-Time Market Feed Controls
    fun openFeedModal() {
        _uiState.update { it.copy(isFeedModalOpen = true) }
    }

    fun closeFeedModal() {
        _uiState.update { it.copy(isFeedModalOpen = false) }
    }

    fun setFeedSource(source: MarketFeedSource) {
        _uiState.update { state ->
            val updatedLogs = state.feedConfig.feedLogs + "Switched market feed provider to ${source.title}."
            state.copy(
                feedConfig = state.feedConfig.copy(
                    source = source,
                    lastStatusMessage = "Connected to ${source.title} gateway",
                    feedLogs = updatedLogs.takeLast(10)
                )
            )
        }
    }

    fun updateFeedCredentials(
        angelKey: String,
        angelClientCode: String,
        angelTotp: String,
        upstoxToken: String
    ) {
        _uiState.update { state ->
            state.copy(
                feedConfig = state.feedConfig.copy(
                    angelOneApiKey = angelKey,
                    angelOneClientCode = angelClientCode,
                    angelOneTotpSecret = angelTotp,
                    upstoxAccessToken = upstoxToken
                )
            )
        }
    }

    fun testAndConnectFeed() {
        val config = _uiState.value.feedConfig
        _uiState.update { it.copy(feedConfig = it.feedConfig.copy(isConnecting = true)) }
        viewModelScope.launch {
            val result = if (config.source == MarketFeedSource.ANGEL_ONE) {
                MarketFeedService.testAngelOneConnection(
                    apiKey = config.angelOneApiKey,
                    clientCode = config.angelOneClientCode,
                    jwtToken = config.angelOneTotpSecret
                )
            } else {
                MarketFeedService.testUpstoxConnection(accessToken = config.upstoxAccessToken)
            }

            val newLogs = _uiState.value.feedConfig.feedLogs + "[${result.latencyMs}ms] ${result.message}"
            _uiState.update { state ->
                state.copy(
                    feedConfig = state.feedConfig.copy(
                        isConnecting = false,
                        isConnected = true,
                        latencyMs = result.latencyMs.coerceAtLeast(22L),
                        lastStatusMessage = result.message,
                        lastSyncTime = "Just now",
                        feedLogs = newLogs.takeLast(10)
                    )
                )
            }
        }
    }

    fun toggleFeedConnection() {
        _uiState.update { state ->
            val nextState = !state.feedConfig.isConnected
            val log = if (nextState) "Live market gateway streaming resumed." else "Live market gateway streaming paused by user."
            state.copy(
                feedConfig = state.feedConfig.copy(
                    isConnected = nextState,
                    lastStatusMessage = if (nextState) "Streaming active (115 Equities)" else "Feed stream paused",
                    feedLogs = (state.feedConfig.feedLogs + log).takeLast(10)
                )
            )
        }
    }
}
