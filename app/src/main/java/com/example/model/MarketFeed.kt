package com.example.model

enum class MarketFeedSource(
    val title: String,
    val providerName: String,
    val apiDocUrl: String,
    val defaultEndpoint: String
) {
    ANGEL_ONE(
        title = "Angel One SmartAPI",
        providerName = "Angel One Ltd",
        apiDocUrl = "https://smartapi.angelbroking.com",
        defaultEndpoint = "https://apiconnect.angelbroking.com"
    ),
    UPSTOX(
        title = "Upstox Developer API (v2)",
        providerName = "Upstox (RKSV)",
        apiDocUrl = "https://upstox.com/developer/api-documentation",
        defaultEndpoint = "https://api.upstox.com/v2"
    )
}

data class FeedConfig(
    val source: MarketFeedSource = MarketFeedSource.ANGEL_ONE,
    val angelOneApiKey: String = "",
    val angelOneClientCode: String = "",
    val angelOneTotpSecret: String = "",
    val upstoxAccessToken: String = "",
    val pollIntervalMs: Long = 2000L,
    val isConnected: Boolean = true,
    val isConnecting: Boolean = false,
    val latencyMs: Long = 34L,
    val lastSyncTime: String = "Just now",
    val totalTicksReceived: Long = 1420L,
    val lastStatusMessage: String = "Streaming active (115 NSE/BSE Equities)",
    val feedLogs: List<String> = listOf(
        "Initial handshake established with NSE Market Gateway.",
        "Angel One SmartAPI market quote session active.",
        "Subscribed to 115 equity instrument tokens on NSE Cash Segment."
    )
)

data class MarketFeedTick(
    val symbol: String,
    val ltp: Double,
    val open: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val close: Double = 0.0,
    val volume: Long = 0,
    val source: MarketFeedSource = MarketFeedSource.ANGEL_ONE,
    val timestamp: Long = System.currentTimeMillis()
)
