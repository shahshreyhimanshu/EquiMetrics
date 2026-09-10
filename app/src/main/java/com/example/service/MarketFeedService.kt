package com.example.service

import com.example.BuildConfig
import com.example.model.MarketFeedSource
import com.example.model.MarketFeedTick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

object MarketFeedService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Upstox Instrument Keys for Major NSE Equities
    val upstoxInstrumentKeys: Map<String, String> = mapOf(
        "RELIANCE" to "NSE_EQ|INE002A01018",
        "TCS" to "NSE_EQ|INE467B01029",
        "HDFCBANK" to "NSE_EQ|INE040A01034",
        "INFY" to "NSE_EQ|INE009A01021",
        "ICICIBANK" to "NSE_EQ|INE090A01021",
        "SBIN" to "NSE_EQ|INE062A01020",
        "BHARTIARTL" to "NSE_EQ|INE397D01024",
        "ITC" to "NSE_EQ|INE154A01025",
        "LT" to "NSE_EQ|INE018A01030",
        "KOTAKBANK" to "NSE_EQ|INE237A01028",
        "AXISBANK" to "NSE_EQ|INE238A01034",
        "HINDUNILVR" to "NSE_EQ|INE030A01027",
        "BAJFINANCE" to "NSE_EQ|INE296A01024",
        "MARUTI" to "NSE_EQ|INE585B01010",
        "TATAMOTORS" to "NSE_EQ|INE155A01022",
        "SUNPHARMA" to "NSE_EQ|INE044A01036",
        "TITAN" to "NSE_EQ|INE280A01028",
        "TATASTEEL" to "NSE_EQ|INE081A01020",
        "POWERGRID" to "NSE_EQ|INE752E01010",
        "NTPC" to "NSE_EQ|INE733E01010",
        "HAL" to "NSE_EQ|INE066F01012",
        "BEL" to "NSE_EQ|INE263A01024",
        "BDL" to "NSE_EQ|INE171Z01018",
        "MAZDOCK" to "NSE_EQ|INE249Z01012",
        "COCHINSHIP" to "NSE_EQ|INE704P01025",
        "SOLARINDS" to "NSE_EQ|INE343H01029",
        "DATAATT" to "NSE_EQ|INE087J01028",
        "ZOMATO" to "NSE_EQ|INE758T01015",
        "SWIGGY" to "NSE_EQ|INE00H001014",
        "PAYTM" to "NSE_EQ|INE982J01020",
        "NYKAA" to "NSE_EQ|INE388Y01029",
        "POLICYBZR" to "NSE_EQ|INE417T01026"
    )

    // Angel One SmartAPI Instrument Tokens for Major NSE Equities
    val angelOneTokens: Map<String, String> = mapOf(
        "RELIANCE" to "2885",
        "TCS" to "11536",
        "HDFCBANK" to "1333",
        "INFY" to "1594",
        "ICICIBANK" to "4963",
        "SBIN" to "3045",
        "BHARTIARTL" to "10604",
        "ITC" to "1660",
        "LT" to "11483",
        "KOTAKBANK" to "1922",
        "AXISBANK" to "5900",
        "HINDUNILVR" to "1394",
        "BAJFINANCE" to "317",
        "MARUTI" to "10999",
        "TATAMOTORS" to "3456",
        "SUNPHARMA" to "3351",
        "TITAN" to "3506",
        "TATASTEEL" to "3499",
        "POWERGRID" to "14977",
        "NTPC" to "11630",
        "HAL" to "2303",
        "BEL" to "383",
        "ZOMATO" to "5097",
        "PAYTM" to "772"
    )

    data class ConnectionResult(
        val success: Boolean,
        val latencyMs: Long,
        val message: String,
        val quotesCount: Int = 0
    )

    suspend fun testAngelOneConnection(
        apiKey: String,
        clientCode: String,
        jwtToken: String
    ): ConnectionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val key = if (apiKey.isNotBlank()) apiKey else getBuildConfigAngelOneKey()
            if (key.isBlank() || key == "MY_ANGEL_ONE_API_KEY") {
                return@withContext ConnectionResult(
                    success = false,
                    latencyMs = System.currentTimeMillis() - startTime,
                    message = "Angel One API Key not configured. Please supply your API key or configure Secrets panel."
                )
            }

            val url = "https://apiconnect.angelbroking.com/rest/secure/angelbroking/market/v1/quote/"
            val jsonPayload = JSONObject().apply {
                put("mode", "LTP")
                val exchangeTokens = JSONObject().apply {
                    val tokensArray = JSONArray()
                    tokensArray.put("2885") // RELIANCE
                    tokensArray.put("11536") // TCS
                    tokensArray.put("1333") // HDFCBANK
                    put("NSE", tokensArray)
                }
                put("exchangeTokens", exchangeTokens)
            }

            val requestBuilder = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("X-UserType", "USER")
                .addHeader("X-SourceID", "WEB")
                .addHeader("X-ClientLocalIP", "192.168.1.1")
                .addHeader("X-ClientPublicIP", "106.193.147.98")
                .addHeader("X-MACAddress", "fe80::216e:6507:4b9c:37f9")
                .addHeader("X-PrivateKey", key)

            if (jwtToken.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $jwtToken")
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val latency = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                ConnectionResult(
                    success = true,
                    latencyMs = latency,
                    message = "Connected to Angel One SmartAPI Market Gateway. Latency: ${latency}ms",
                    quotesCount = 3
                )
            } else {
                ConnectionResult(
                    success = false,
                    latencyMs = latency,
                    message = "Angel One returned HTTP ${response.code}: Session or API Key error (${responseBody.take(80)})"
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionResult(
                success = false,
                latencyMs = latency,
                message = "Angel One Gateway connection timeout/error: ${e.message ?: "Network unreachable"}"
            )
        }
    }

    suspend fun testUpstoxConnection(
        accessToken: String
    ): ConnectionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val token = if (accessToken.isNotBlank()) accessToken else getBuildConfigUpstoxToken()
            if (token.isBlank() || token == "MY_UPSTOX_ACCESS_TOKEN") {
                return@withContext ConnectionResult(
                    success = false,
                    latencyMs = System.currentTimeMillis() - startTime,
                    message = "Upstox Access Token not configured. Please supply your OAuth token or configure Secrets panel."
                )
            }

            val testKeys = "NSE_EQ|INE002A01018,NSE_EQ|INE467B01029,NSE_EQ|INE040A01034"
            val url = "https://api.upstox.com/v2/market-quote/ltp?instrument_key=$testKeys"

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val dataObj = json.optJSONObject("data")
                val count = dataObj?.length() ?: 0
                ConnectionResult(
                    success = true,
                    latencyMs = latency,
                    message = "Connected to Upstox Developer API (v2). Latency: ${latency}ms",
                    quotesCount = count
                )
            } else {
                ConnectionResult(
                    success = false,
                    latencyMs = latency,
                    message = "Upstox API returned HTTP ${response.code}: Authentication invalid or expired (${responseBody.take(80)})"
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionResult(
                success = false,
                latencyMs = latency,
                message = "Upstox Gateway connection timeout/error: ${e.message ?: "Network unreachable"}"
            )
        }
    }

    suspend fun fetchUpstoxQuotes(
        accessToken: String,
        symbols: List<String>
    ): List<MarketFeedTick> = withContext(Dispatchers.IO) {
        val token = if (accessToken.isNotBlank()) accessToken else getBuildConfigUpstoxToken()
        if (token.isBlank() || token == "MY_UPSTOX_ACCESS_TOKEN") {
            return@withContext emptyList()
        }

        try {
            val keysToFetch = symbols.mapNotNull { symbol ->
                upstoxInstrumentKeys[symbol] ?: "NSE_EQ|$symbol"
            }.take(30).joinToString(",")

            val url = "https://api.upstox.com/v2/market-quote/ltp?instrument_key=$keysToFetch"
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(bodyStr)
                val data = json.optJSONObject("data") ?: return@withContext emptyList()
                val ticks = mutableListOf<MarketFeedTick>()

                symbols.forEach { sym ->
                    val keyName = "NSE_EQ:$sym"
                    val item = data.optJSONObject(keyName)
                    if (item != null) {
                        val lastPrice = item.optDouble("last_price", 0.0)
                        if (lastPrice > 0.0) {
                            ticks.add(
                                MarketFeedTick(
                                    symbol = sym,
                                    ltp = lastPrice,
                                    source = MarketFeedSource.UPSTOX,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
                return@withContext ticks
            }
        } catch (_: Exception) {
            // Fall through gracefully
        }
        emptyList()
    }

    suspend fun fetchAngelOneQuotes(
        apiKey: String,
        jwtToken: String,
        symbols: List<String>
    ): List<MarketFeedTick> = withContext(Dispatchers.IO) {
        val key = if (apiKey.isNotBlank()) apiKey else getBuildConfigAngelOneKey()
        if (key.isBlank() || key == "MY_ANGEL_ONE_API_KEY") {
            return@withContext emptyList()
        }

        try {
            val tokens = symbols.mapNotNull { sym -> angelOneTokens[sym] }
            if (tokens.isEmpty()) return@withContext emptyList()

            val url = "https://apiconnect.angelbroking.com/rest/secure/angelbroking/market/v1/quote/"
            val jsonPayload = JSONObject().apply {
                put("mode", "FULL")
                val exchangeTokens = JSONObject().apply {
                    val tokensArray = JSONArray()
                    tokens.forEach { tokensArray.put(it) }
                    put("NSE", tokensArray)
                }
                put("exchangeTokens", exchangeTokens)
            }

            val requestBuilder = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("X-UserType", "USER")
                .addHeader("X-SourceID", "WEB")
                .addHeader("X-ClientLocalIP", "192.168.1.1")
                .addHeader("X-ClientPublicIP", "106.193.147.98")
                .addHeader("X-MACAddress", "fe80::216e:6507:4b9c:37f9")
                .addHeader("X-PrivateKey", key)

            if (jwtToken.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $jwtToken")
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(bodyStr)
                val dataObj = json.optJSONObject("data")
                val fetchedArray = dataObj?.optJSONArray("fetched") ?: return@withContext emptyList()
                val ticks = mutableListOf<MarketFeedTick>()

                for (i in 0 until fetchedArray.length()) {
                    val item = fetchedArray.optJSONObject(i) ?: continue
                    val tradingSymbol = item.optString("tradingSymbol", "").replace("-EQ", "")
                    val ltp = item.optDouble("ltp", 0.0)
                    val high = item.optDouble("high", 0.0)
                    val low = item.optDouble("low", 0.0)
                    val open = item.optDouble("open", 0.0)
                    val close = item.optDouble("close", 0.0)

                    if (tradingSymbol.isNotBlank() && ltp > 0.0) {
                        ticks.add(
                            MarketFeedTick(
                                symbol = tradingSymbol,
                                ltp = ltp,
                                open = open,
                                high = high,
                                low = low,
                                close = close,
                                source = MarketFeedSource.ANGEL_ONE,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                return@withContext ticks
            }
        } catch (_: Exception) {
            // Fall through gracefully
        }
        emptyList()
    }

    private fun getBuildConfigAngelOneKey(): String {
        return try {
            val field = BuildConfig::class.java.getField("ANGEL_ONE_API_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun getBuildConfigUpstoxToken(): String {
        return try {
            val field = BuildConfig::class.java.getField("UPSTOX_ACCESS_TOKEN")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}
