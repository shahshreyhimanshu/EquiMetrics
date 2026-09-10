package com.example.model

enum class Timeframe(val label: String, val pointsCount: Int) {
    ONE_DAY("1D", 24),
    ONE_WEEK("1W", 35),
    ONE_MONTH("1M", 45),
    ONE_YEAR("1Y", 60),
    ALL("ALL", 80)
}

enum class ChartType {
    CANDLESTICK,
    LINE
}

data class ChartPoint(
    val timestamp: String,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Long,
    val ema20: Float,
    val ema50: Float,
    val rsi: Float
)

data class FinancialRatios(
    val peRatio: Double,
    val pbRatio: Double,
    val debtToEquity: Double,
    val roePercentage: Double,
    val rocePercentage: Double,
    val dividendYieldPercentage: Double,
    val earningsPerShare: Double,
    val operatingMarginPercentage: Double,
    val priceToSales: Double,
    val freeCashFlowCr: Double,
    val promoterHoldingPct: Double,
    val fiiDiiHoldingPct: Double
)

data class MarketDepthItem(
    val bidOrders: Int,
    val bidQty: Int,
    val bidPrice: Double,
    val askPrice: Double,
    val askQty: Int,
    val askOrders: Int
)

data class StockNews(
    val id: String,
    val stockSymbol: String?,
    val headline: String,
    val source: String,
    val timeAgo: String,
    val summary: String,
    val sentiment: String, // "Bullish", "Neutral", "Bearish"
    val tags: List<String>
)

data class OptionsStrike(
    val strikePrice: Double,
    val callOI: Long,
    val callOIChange: Long,
    val callIV: Double,
    val callLTP: Double,
    val putOI: Long,
    val putOIChange: Long,
    val putIV: Double,
    val putLTP: Double,
    val callDelta: Double = 0.52,
    val callTheta: Double = -12.4,
    val callVega: Double = 8.6,
    val callGamma: Double = 0.0018,
    val putDelta: Double = -0.48,
    val putTheta: Double = -11.9,
    val putVega: Double = 8.4,
    val putGamma: Double = 0.0018
)

data class EarningsTakeaway(
    val stockSymbol: String,
    val quarter: String,
    val releaseDate: String,
    val revenueCr: Double,
    val revenueYoYGrowthPct: Double,
    val patCr: Double,
    val patYoYGrowthPct: Double,
    val ebitdaMarginPct: Double,
    val managementQuotes: List<String>,
    val factualHighlights: List<String>
)

data class Stock(
    val symbol: String,
    val companyName: String,
    val sector: String,
    val currentPrice: Double,
    val previousClose: Double,
    val dayHigh: Double,
    val dayLow: Double,
    val fiftyTwoWeekHigh: Double,
    val fiftyTwoWeekLow: Double,
    val volume: Long,
    val marketCapCr: Double,
    val tags: List<String>,
    val ratios: FinancialRatios,
    val chartData: Map<Timeframe, List<ChartPoint>>,
    val marketDepth: List<MarketDepthItem>,
    val priceChangeHistory: List<Double> = emptyList()
) {
    val change: Double get() = currentPrice - previousClose
    val changePct: Double get() = if (previousClose > 0) (change / previousClose) * 100 else 0.0
}

data class MarketIndex(
    val symbol: String,
    val name: String,
    val currentValue: Double,
    val previousClose: Double,
    val isDomestic: Boolean = true
) {
    val change: Double get() = currentValue - previousClose
    val changePct: Double get() = if (previousClose > 0) (change / previousClose) * 100 else 0.0
}

enum class OrderType(val label: String) {
    MARKET("Market"),
    LIMIT("Limit"),
    STOP_LOSS("Stop-Loss"),
    COVER_ORDER("Cover Order (CO)")
}

enum class OrderAction(val label: String) {
    BUY("Buy"),
    SELL("Sell")
}

data class PaperPosition(
    val symbol: String,
    val companyName: String,
    val quantity: Int,
    val avgPrice: Double,
    val currentPrice: Double,
    val orderType: OrderType
) {
    val investmentValue: Double get() = quantity * avgPrice
    val currentValue: Double get() = quantity * currentPrice
    val pnl: Double get() = currentValue - investmentValue
    val pnlPct: Double get() = if (investmentValue > 0) (pnl / investmentValue) * 100 else 0.0
}

data class PaperTradeLog(
    val id: String,
    val timestamp: String,
    val symbol: String,
    val action: OrderAction,
    val orderType: OrderType,
    val quantity: Int,
    val executedPrice: Double,
    val totalAmount: Double,
    val status: String = "Executed"
)

data class BrokerAccount(
    val brokerName: String,
    val brokerLogoColor: Long,
    val accountId: String,
    val isConnected: Boolean,
    val totalHoldingsValue: Double,
    val totalInvestedValue: Double,
    val cashBalance: Double,
    val lastSyncTime: String,
    val familyMemberTag: String = "Self"
)

data class TenYearFinancialRow(
    val year: String,
    val revenueCr: Double,
    val ebitdaCr: Double,
    val patCr: Double,
    val eps: Double,
    val operatingMarginPct: Double,
    val debtToEquity: Double,
    val freeCashFlowCr: Double,
    val roePct: Double
)

data class SeasonalMonthReturn(
    val month: String,
    val yearReturns: List<Pair<Int, Double>> // e.g. (2015, 3.2), (2016, -1.1), etc.
) {
    val greenYearsCount: Int get() = yearReturns.count { it.second > 0 }
    val totalYearsCount: Int get() = yearReturns.size
    val winRatePct: Double get() = if (totalYearsCount > 0) (greenYearsCount.toDouble() / totalYearsCount) * 100 else 0.0
    val averageReturnPct: Double get() = if (totalYearsCount > 0) yearReturns.sumOf { it.second } / totalYearsCount else 0.0
}

data class StockNote(
    val id: String,
    val stockSymbol: String,
    val noteText: String,
    val timestamp: String,
    val tag: String = "Research"
)

data class ResearchChecklist(
    val hasMoat: Boolean = false,
    val hasCleanBalanceSheet: Boolean = false,
    val hasHighCapitalAllocation: Boolean = false,
    val hasMarginOfSafety: Boolean = false,
    val hasIndustryTailwinds: Boolean = false
)

data class Watchlist(
    val id: String,
    val name: String,
    val stockSymbols: List<String>,
    val isDefault: Boolean = false
)

enum class AlertChannel(val displayName: String) {
    PUSH("In-App Push (Delayed)"),
    WHATSAPP("Instant WhatsApp"),
    TELEGRAM("Instant Telegram")
}

data class StockAlert(
    val id: String,
    val symbol: String,
    val conditionType: String, // "Crosses Above", "Crosses Below", "52-Week High Breakout", "% Move > 3%"
    val thresholdValue: Double,
    val channel: AlertChannel,
    val contactInfo: String = "", // e.g. "+91 98765 43210" or "@trader_sam"
    val isActive: Boolean = true,
    val createdAt: String = "Today"
)

data class RrgSectorPoint(
    val sectorName: String,
    val rsRatio: Float, // > 100 = Stronger than benchmark, < 100 = Weaker
    val rsMomentum: Float, // > 100 = Gaining momentum, < 100 = Losing momentum
    val quadrant: String, // "Leading", "Weakening", "Lagging", "Improving"
    val colorHex: Long
)

data class FiiDiiFlow(
    val date: String,
    val fiiCashCr: Double,
    val diiCashCr: Double,
    val fiiFnoCr: Double,
    val diiFnoCr: Double
)

data class BrokerHolding(
    val symbol: String,
    val companyName: String,
    val brokerName: String,
    val quantity: Int,
    val averageBuyPrice: Double,
    val currentPrice: Double,
    val sector: String,
    val assetClass: String = "Large-Cap Equity" // "Large-Cap Equity", "Mid-Cap Equity", "Small-Cap Equity", "Mutual Fund", "ETF"
) {
    val totalInvested: Double get() = quantity * averageBuyPrice
    val totalCurrentValue: Double get() = quantity * currentPrice
    val unrealizedPnl: Double get() = totalCurrentValue - totalInvested
    val unrealizedPnlPct: Double get() = if (totalInvested > 0) (unrealizedPnl / totalInvested) * 100 else 0.0
}
