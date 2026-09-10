package com.example.data

import com.example.model.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object SampleDataProvider {

    val marketIndices = listOf(
        MarketIndex("NIFTY 50", "Nifty 50", 25180.45, 25050.20, isDomestic = true),
        MarketIndex("SENSEX", "BSE Sensex", 82450.80, 82120.15, isDomestic = true),
        MarketIndex("BANK NIFTY", "Nifty Bank", 51840.10, 51610.75, isDomestic = true),
        MarketIndex("NIFTY IT", "Nifty IT", 42120.30, 41890.10, isDomestic = true),
        MarketIndex("NIFTY AUTO", "Nifty Auto", 26450.60, 26210.40, isDomestic = true),
        MarketIndex("S&P 500", "S&P 500", 5648.40, 5625.80, isDomestic = false),
        MarketIndex("NASDAQ", "Nasdaq Comp", 17820.25, 17710.60, isDomestic = false)
    )

    fun generateChartData(basePrice: Double): Map<Timeframe, List<ChartPoint>> {
        val result = mutableMapOf<Timeframe, List<ChartPoint>>()
        Timeframe.values().forEach { tf ->
            val points = mutableListOf<ChartPoint>()
            var currentClose = basePrice.toFloat() * when (tf) {
                Timeframe.ONE_DAY -> 0.995f
                Timeframe.ONE_WEEK -> 0.97f
                Timeframe.ONE_MONTH -> 0.93f
                Timeframe.ONE_YEAR -> 0.82f
                Timeframe.ALL -> 0.65f
            }
            var ema20 = currentClose
            var ema50 = currentClose * 0.98f

            for (i in 0 until tf.pointsCount) {
                val cycle = (sin(i * 0.3) * 0.015f + cos(i * 0.15) * 0.01f).toFloat()
                val noise = ((Random.nextFloat() - 0.48f) * 0.012f)
                val delta = (cycle + noise) * currentClose

                val open = currentClose
                val close = open + delta
                val high = maxOf(open, close) + (Random.nextFloat() * 0.008f * close)
                val low = minOf(open, close) - (Random.nextFloat() * 0.008f * close)
                val volume = (150000 + Random.nextInt(450000)).toLong()

                ema20 = (close * 0.095f) + (ema20 * 0.905f)
                ema50 = (close * 0.039f) + (ema50 * 0.961f)
                val rsiVal = (45.0 + (sin(i * 0.4) * 22.0) + (Random.nextDouble() * 8.0)).coerceIn(20.0, 85.0).toFloat()

                val label = when (tf) {
                    Timeframe.ONE_DAY -> String.format("%02d:%02d", 9 + (i * 15 / 60), (i * 15) % 60)
                    Timeframe.ONE_WEEK -> "Day ${i + 1}"
                    Timeframe.ONE_MONTH -> "D${i + 1}"
                    Timeframe.ONE_YEAR -> "M${(i % 12) + 1}"
                    Timeframe.ALL -> "${2018 + (i / 10)}"
                }

                points.add(
                    ChartPoint(
                        timestamp = label,
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = volume,
                        ema20 = ema20,
                        ema50 = ema50,
                        rsi = rsiVal
                    )
                )
                currentClose = close
            }
            result[tf] = points
        }
        return result
    }

    private fun generateMarketDepth(price: Double): List<MarketDepthItem> {
        val spread = 0.05
        return (1..5).map { level ->
            val bidP = price - (level * spread)
            val askP = price + (level * spread)
            MarketDepthItem(
                bidOrders = 4 + level * 2 + Random.nextInt(5),
                bidQty = (350 + level * 120 + Random.nextInt(150)),
                bidPrice = String.format("%.2f", bidP).toDouble(),
                askPrice = String.format("%.2f", askP).toDouble(),
                askQty = (320 + level * 110 + Random.nextInt(140)),
                askOrders = 3 + level * 2 + Random.nextInt(4)
            )
        }
    }

    private fun buildStock(
        symbol: String,
        companyName: String,
        sector: String,
        price: Double,
        prevClose: Double,
        dayHigh: Double,
        dayLow: Double,
        fiftyTwoHigh: Double,
        fiftyTwoLow: Double,
        volume: Long,
        marketCapCr: Double,
        tags: List<String>,
        pe: Double,
        pb: Double,
        de: Double,
        roe: Double,
        roce: Double,
        divYield: Double,
        eps: Double,
        opMargin: Double,
        ps: Double,
        fcfCr: Double,
        promoterPct: Double,
        fiiDiiPct: Double
    ): Stock {
        return Stock(
            symbol = symbol,
            companyName = companyName,
            sector = sector,
            currentPrice = price,
            previousClose = prevClose,
            dayHigh = dayHigh,
            dayLow = dayLow,
            fiftyTwoWeekHigh = fiftyTwoHigh,
            fiftyTwoWeekLow = fiftyTwoLow,
            volume = volume,
            marketCapCr = marketCapCr,
            tags = tags,
            ratios = FinancialRatios(
                peRatio = pe,
                pbRatio = pb,
                debtToEquity = de,
                roePercentage = roe,
                rocePercentage = roce,
                dividendYieldPercentage = divYield,
                earningsPerShare = eps,
                operatingMarginPercentage = opMargin,
                priceToSales = ps,
                freeCashFlowCr = fcfCr,
                promoterHoldingPct = promoterPct,
                fiiDiiHoldingPct = fiiDiiPct
            ),
            chartData = generateChartData(price),
            marketDepth = generateMarketDepth(price)
        )
    }

    // Comprehensive list covering major NSE & BSE listed equities across sectors
    val stockList: List<Stock> = listOf(
        // 1. Reliance Industries
        buildStock(
            symbol = "RELIANCE",
            companyName = "Reliance Industries Ltd",
            sector = "Energy & Retail",
            price = 2985.40, prevClose = 2954.10, dayHigh = 3012.00, dayLow = 2948.50,
            fiftyTwoHigh = 3217.90, fiftyTwoLow = 2221.05, volume = 4850120, marketCapCr = 2020540.0,
            tags = listOf("Nifty 50", "Energy", "Large Cap"),
            pe = 27.4, pb = 2.45, de = 0.38, roe = 9.4, roce = 10.8, divYield = 0.35,
            eps = 108.9, opMargin = 16.2, ps = 2.1, fcfCr = 34200.0, promoterPct = 50.3, fiiDiiPct = 37.8
        ),
        // 2. TCS
        buildStock(
            symbol = "TCS",
            companyName = "Tata Consultancy Services Ltd",
            sector = "Information Technology",
            price = 4320.15, prevClose = 4350.80, dayHigh = 4370.00, dayLow = 4295.10,
            fiftyTwoHigh = 4592.25, fiftyTwoLow = 3311.00, volume = 1980420, marketCapCr = 1563200.0,
            tags = listOf("Nifty 50", "IT", "Large Cap"),
            pe = 31.8, pb = 15.2, de = 0.05, roe = 48.6, roce = 61.2, divYield = 1.62,
            eps = 135.8, opMargin = 26.4, ps = 6.4, fcfCr = 42100.0, promoterPct = 71.8, fiiDiiPct = 21.4
        ),
        // 3. HDFC Bank
        buildStock(
            symbol = "HDFCBANK",
            companyName = "HDFC Bank Ltd",
            sector = "Banking & Finance",
            price = 1642.50, prevClose = 1628.00, dayHigh = 1655.80, dayLow = 1621.00,
            fiftyTwoHigh = 1794.00, fiftyTwoLow = 1363.45, volume = 14205000, marketCapCr = 1249800.0,
            tags = listOf("Nifty 50", "Banking", "Large Cap"),
            pe = 18.9, pb = 2.65, de = 7.10, roe = 16.4, roce = 7.8, divYield = 1.18,
            eps = 86.9, opMargin = 34.5, ps = 4.2, fcfCr = 18900.0, promoterPct = 0.0, fiiDiiPct = 81.2
        ),
        // 4. Infosys
        buildStock(
            symbol = "INFY",
            companyName = "Infosys Ltd",
            sector = "Information Technology",
            price = 1894.20, prevClose = 1876.40, dayHigh = 1910.00, dayLow = 1868.10,
            fiftyTwoHigh = 1991.45, fiftyTwoLow = 1351.65, volume = 5230000, marketCapCr = 786400.0,
            tags = listOf("Nifty 50", "IT", "Large Cap"),
            pe = 28.5, pb = 8.9, de = 0.08, roe = 31.8, roce = 40.5, divYield = 2.25,
            eps = 66.4, opMargin = 21.6, ps = 4.9, fcfCr = 22400.0, promoterPct = 14.8, fiiDiiPct = 68.4
        ),
        // 5. ICICI Bank
        buildStock(
            symbol = "ICICIBANK",
            companyName = "ICICI Bank Ltd",
            sector = "Banking & Finance",
            price = 1248.60, prevClose = 1239.10, dayHigh = 1256.40, dayLow = 1232.00,
            fiftyTwoHigh = 1300.90, fiftyTwoLow = 912.00, volume = 8940000, marketCapCr = 878200.0,
            tags = listOf("Nifty 50", "Banking", "Large Cap"),
            pe = 17.2, pb = 3.1, de = 6.4, roe = 18.2, roce = 8.1, divYield = 0.85,
            eps = 72.5, opMargin = 38.2, ps = 3.8, fcfCr = 14200.0, promoterPct = 0.0, fiiDiiPct = 78.5
        ),
        // 6. Tata Motors
        buildStock(
            symbol = "TATAMOTORS",
            companyName = "Tata Motors Ltd",
            sector = "Automobile",
            price = 984.30, prevClose = 998.40, dayHigh = 1008.00, dayLow = 978.20,
            fiftyTwoHigh = 1179.05, fiftyTwoLow = 608.50, volume = 11200000, marketCapCr = 362400.0,
            tags = listOf("Nifty 50", "Auto", "EV"),
            pe = 10.4, pb = 3.8, de = 0.62, roe = 36.8, roce = 24.5, divYield = 0.61,
            eps = 94.6, opMargin = 14.1, ps = 0.82, fcfCr = 28500.0, promoterPct = 46.4, fiiDiiPct = 34.2
        ),
        // 7. Bharti Airtel
        buildStock(
            symbol = "BHARTIARTL",
            companyName = "Bharti Airtel Ltd",
            sector = "Telecom",
            price = 1588.75, prevClose = 1562.10, dayHigh = 1598.00, dayLow = 1555.00,
            fiftyTwoHigh = 1680.00, fiftyTwoLow = 890.10, volume = 4320000, marketCapCr = 942500.0,
            tags = listOf("Nifty 50", "Telecom", "Large Cap"),
            pe = 54.2, pb = 9.4, de = 1.95, roe = 17.5, roce = 15.2, divYield = 0.52,
            eps = 29.3, opMargin = 51.4, ps = 6.2, fcfCr = 31200.0, promoterPct = 53.1, fiiDiiPct = 39.4
        ),
        // 8. State Bank of India
        buildStock(
            symbol = "SBIN",
            companyName = "State Bank of India",
            sector = "Banking & Finance",
            price = 814.20, prevClose = 808.50, dayHigh = 822.00, dayLow = 805.00,
            fiftyTwoHigh = 912.10, fiftyTwoLow = 555.30, volume = 15400000, marketCapCr = 726500.0,
            tags = listOf("Nifty 50", "Banking", "PSU"),
            pe = 10.8, pb = 1.62, de = 8.4, roe = 16.8, roce = 6.2, divYield = 1.68,
            eps = 75.4, opMargin = 28.6, ps = 1.4, fcfCr = 21000.0, promoterPct = 57.5, fiiDiiPct = 33.1
        ),
        // 9. ITC
        buildStock(
            symbol = "ITC",
            companyName = "ITC Ltd",
            sector = "FMCG",
            price = 502.40, prevClose = 499.10, dayHigh = 506.80, dayLow = 497.50,
            fiftyTwoHigh = 525.00, fiftyTwoLow = 399.30, volume = 9870000, marketCapCr = 627800.0,
            tags = listOf("Nifty 50", "FMCG", "High Dividend"),
            pe = 29.1, pb = 8.6, de = 0.01, roe = 29.8, roce = 39.4, divYield = 2.85,
            eps = 17.2, opMargin = 37.6, ps = 8.8, fcfCr = 16800.0, promoterPct = 0.0, fiiDiiPct = 84.1
        ),
        // 10. Hindustan Unilever
        buildStock(
            symbol = "HINDUNILVR",
            companyName = "Hindustan Unilever Ltd",
            sector = "FMCG",
            price = 2724.80, prevClose = 2740.00, dayHigh = 2755.00, dayLow = 2710.00,
            fiftyTwoHigh = 3034.50, fiftyTwoLow = 2172.05, volume = 2100000, marketCapCr = 640200.0,
            tags = listOf("Nifty 50", "FMCG", "Large Cap"),
            pe = 62.4, pb = 12.8, de = 0.03, roe = 20.5, roce = 27.8, divYield = 1.54,
            eps = 43.6, opMargin = 23.8, ps = 10.1, fcfCr = 9800.0, promoterPct = 61.9, fiiDiiPct = 25.4
        ),
        // 11. Larsen & Toubro
        buildStock(
            symbol = "LT",
            companyName = "Larsen & Toubro Ltd",
            sector = "Infrastructure",
            price = 3652.00, prevClose = 3615.40, dayHigh = 3680.00, dayLow = 3602.00,
            fiftyTwoHigh = 3948.60, fiftyTwoLow = 2871.00, volume = 2850000, marketCapCr = 502100.0,
            tags = listOf("Nifty 50", "Infra", "Large Cap"),
            pe = 37.2, pb = 5.4, de = 1.25, roe = 15.2, roce = 14.1, divYield = 0.92,
            eps = 98.1, opMargin = 11.2, ps = 2.2, fcfCr = 11500.0, promoterPct = 0.0, fiiDiiPct = 64.2
        ),
        // 12. Kotak Mahindra Bank
        buildStock(
            symbol = "KOTAKBANK",
            companyName = "Kotak Mahindra Bank Ltd",
            sector = "Banking & Finance",
            price = 1782.30, prevClose = 1795.00, dayHigh = 1805.00, dayLow = 1770.00,
            fiftyTwoHigh = 1925.00, fiftyTwoLow = 1543.00, volume = 3450000, marketCapCr = 354200.0,
            tags = listOf("Nifty 50", "Banking"),
            pe = 19.5, pb = 2.8, de = 3.8, roe = 14.8, roce = 7.9, divYield = 0.12,
            eps = 91.4, opMargin = 31.2, ps = 4.8, fcfCr = 8400.0, promoterPct = 25.9, fiiDiiPct = 63.4
        ),
        // 13. Axis Bank
        buildStock(
            symbol = "AXISBANK",
            companyName = "Axis Bank Ltd",
            sector = "Banking & Finance",
            price = 1194.50, prevClose = 1182.20, dayHigh = 1206.00, dayLow = 1175.00,
            fiftyTwoHigh = 1339.65, fiftyTwoLow = 951.40, volume = 6800000, marketCapCr = 369800.0,
            tags = listOf("Nifty 50", "Banking"),
            pe = 14.2, pb = 2.2, de = 6.8, roe = 17.1, roce = 7.4, divYield = 0.08,
            eps = 84.1, opMargin = 29.5, ps = 3.2, fcfCr = 9200.0, promoterPct = 8.1, fiiDiiPct = 82.5
        ),
        // 14. Bajaj Finance
        buildStock(
            symbol = "BAJFINANCE",
            companyName = "Bajaj Finance Ltd",
            sector = "Financial Services",
            price = 7248.00, prevClose = 7180.00, dayHigh = 7320.00, dayLow = 7140.00,
            fiftyTwoHigh = 8192.00, fiftyTwoLow = 6187.80, volume = 1420000, marketCapCr = 448500.0,
            tags = listOf("Nifty 50", "NBFC", "Financial Services"),
            pe = 29.8, pb = 5.8, de = 3.4, roe = 22.4, roce = 11.2, divYield = 0.50,
            eps = 243.2, opMargin = 64.2, ps = 8.1, fcfCr = 12400.0, promoterPct = 54.8, fiiDiiPct = 34.5
        ),
        // 15. Bajaj Finserv
        buildStock(
            symbol = "BAJAJFINSV",
            companyName = "Bajaj Finserv Ltd",
            sector = "Financial Services",
            price = 1824.50, prevClose = 1810.00, dayHigh = 1845.00, dayLow = 1800.00,
            fiftyTwoHigh = 1980.00, fiftyTwoLow = 1419.00, volume = 1950000, marketCapCr = 291200.0,
            tags = listOf("Nifty 50", "Insurance", "Financial Services"),
            pe = 34.5, pb = 4.2, de = 1.8, roe = 14.2, roce = 12.1, divYield = 0.06,
            eps = 52.8, opMargin = 22.4, ps = 3.4, fcfCr = 6200.0, promoterPct = 60.7, fiiDiiPct = 26.8
        ),
        // 16. Maruti Suzuki
        buildStock(
            symbol = "MARUTI",
            companyName = "Maruti Suzuki India Ltd",
            sector = "Automobile",
            price = 12450.00, prevClose = 12380.00, dayHigh = 12580.00, dayLow = 12320.00,
            fiftyTwoHigh = 13680.00, fiftyTwoLow = 9737.00, volume = 580000, marketCapCr = 391400.0,
            tags = listOf("Nifty 50", "Auto"),
            pe = 28.4, pb = 4.6, de = 0.01, roe = 16.8, roce = 22.4, divYield = 1.01,
            eps = 438.4, opMargin = 11.8, ps = 2.8, fcfCr = 9400.0, promoterPct = 58.2, fiiDiiPct = 36.1
        ),
        // 17. Mahindra & Mahindra
        buildStock(
            symbol = "MM",
            companyName = "Mahindra & Mahindra Ltd",
            sector = "Automobile",
            price = 2824.50, prevClose = 2790.00, dayHigh = 2855.00, dayLow = 2775.00,
            fiftyTwoHigh = 3014.00, fiftyTwoLow = 1452.00, volume = 3100000, marketCapCr = 351200.0,
            tags = listOf("Nifty 50", "Auto", "EV"),
            pe = 29.5, pb = 5.1, de = 1.12, roe = 18.4, roce = 17.2, divYield = 0.75,
            eps = 95.8, opMargin = 13.6, ps = 2.4, fcfCr = 8800.0, promoterPct = 18.9, fiiDiiPct = 68.2
        ),
        // 18. Sun Pharma
        buildStock(
            symbol = "SUNPHARMA",
            companyName = "Sun Pharmaceutical Industries Ltd",
            sector = "Pharma & Healthcare",
            price = 1742.00, prevClose = 1728.50, dayHigh = 1758.00, dayLow = 1718.00,
            fiftyTwoHigh = 1810.00, fiftyTwoLow = 1110.00, volume = 2400000, marketCapCr = 417800.0,
            tags = listOf("Nifty 50", "Pharma"),
            pe = 38.6, pb = 5.8, de = 0.04, roe = 16.4, roce = 18.9, divYield = 0.78,
            eps = 45.1, opMargin = 28.2, ps = 8.1, fcfCr = 9200.0, promoterPct = 54.5, fiiDiiPct = 35.8
        ),
        // 19. Titan Company
        buildStock(
            symbol = "TITAN",
            companyName = "Titan Company Ltd",
            sector = "Consumer Goods",
            price = 3780.00, prevClose = 3745.00, dayHigh = 3810.00, dayLow = 3730.00,
            fiftyTwoHigh = 3886.95, fiftyTwoLow = 3055.65, volume = 1450000, marketCapCr = 335600.0,
            tags = listOf("Nifty 50", "Consumer", "Jewellery"),
            pe = 88.2, pb = 28.4, de = 0.65, roe = 32.5, roce = 36.8, divYield = 0.29,
            eps = 42.8, opMargin = 10.4, ps = 6.5, fcfCr = 2800.0, promoterPct = 52.9, fiiDiiPct = 32.8
        ),
        // 20. Tata Steel
        buildStock(
            symbol = "TATASTEEL",
            companyName = "Tata Steel Ltd",
            sector = "Metals & Mining",
            price = 154.20, prevClose = 152.80, dayHigh = 156.00, dayLow = 151.50,
            fiftyTwoHigh = 184.60, fiftyTwoLow = 114.25, volume = 32400000, marketCapCr = 192500.0,
            tags = listOf("Nifty 50", "Metals"),
            pe = 44.5, pb = 2.1, de = 0.95, roe = 5.2, roce = 7.8, divYield = 2.34,
            eps = 3.46, opMargin = 12.1, ps = 0.85, fcfCr = 8400.0, promoterPct = 33.2, fiiDiiPct = 45.8
        ),
        // 21. NTPC
        buildStock(
            symbol = "NTPC",
            companyName = "NTPC Ltd",
            sector = "Power Generation",
            price = 412.50, prevClose = 407.20, dayHigh = 418.00, dayLow = 404.00,
            fiftyTwoHigh = 448.45, fiftyTwoLow = 230.15, volume = 14500000, marketCapCr = 400100.0,
            tags = listOf("Nifty 50", "Energy", "PSU"),
            pe = 18.2, pb = 2.4, de = 1.45, roe = 13.8, roce = 11.2, divYield = 1.88,
            eps = 22.6, opMargin = 26.5, ps = 2.1, fcfCr = 14800.0, promoterPct = 51.1, fiiDiiPct = 42.5
        ),
        // 22. Power Grid
        buildStock(
            symbol = "POWERGRID",
            companyName = "Power Grid Corp of India Ltd",
            sector = "Power Transmission",
            price = 332.40, prevClose = 328.60, dayHigh = 336.00, dayLow = 326.00,
            fiftyTwoHigh = 366.25, fiftyTwoLow = 194.20, volume = 12800000, marketCapCr = 309100.0,
            tags = listOf("Nifty 50", "Utilities", "PSU"),
            pe = 19.8, pb = 3.4, de = 1.52, roe = 18.2, roce = 12.8, divYield = 3.45,
            eps = 16.8, opMargin = 88.4, ps = 6.4, fcfCr = 17200.0, promoterPct = 51.3, fiiDiiPct = 37.8
        ),
        // 23. ONGC
        buildStock(
            symbol = "ONGC",
            companyName = "Oil & Natural Gas Corp Ltd",
            sector = "Oil & Gas",
            price = 302.80, prevClose = 298.40, dayHigh = 306.50, dayLow = 296.00,
            fiftyTwoHigh = 344.75, fiftyTwoLow = 179.80, volume = 18900000, marketCapCr = 380900.0,
            tags = listOf("Nifty 50", "Energy", "PSU"),
            pe = 7.6, pb = 1.15, de = 0.42, roe = 14.8, roce = 16.2, divYield = 4.02,
            eps = 39.8, opMargin = 22.1, ps = 0.58, fcfCr = 28400.0, promoterPct = 58.9, fiiDiiPct = 31.4
        ),
        // 24. Coal India
        buildStock(
            symbol = "COALINDIA",
            companyName = "Coal India Ltd",
            sector = "Metals & Mining",
            price = 498.60, prevClose = 492.00, dayHigh = 504.00, dayLow = 488.00,
            fiftyTwoHigh = 543.55, fiftyTwoLow = 275.25, volume = 11400000, marketCapCr = 307200.0,
            tags = listOf("Nifty 50", "Mining", "High Dividend", "PSU"),
            pe = 8.4, pb = 3.8, de = 0.08, roe = 48.2, roce = 62.5, divYield = 5.12,
            eps = 59.3, opMargin = 28.9, ps = 2.1, fcfCr = 24100.0, promoterPct = 63.1, fiiDiiPct = 30.5
        ),
        // 25. Adani Enterprises
        buildStock(
            symbol = "ADANIENT",
            companyName = "Adani Enterprises Ltd",
            sector = "Infrastructure & Energy",
            price = 3054.00, prevClose = 3010.00, dayHigh = 3105.00, dayLow = 2995.00,
            fiftyTwoHigh = 3743.00, fiftyTwoLow = 2142.00, volume = 2200000, marketCapCr = 348200.0,
            tags = listOf("Nifty 50", "Adani Group", "Infra"),
            pe = 86.4, pb = 7.8, de = 1.48, roe = 9.8, roce = 10.4, divYield = 0.04,
            eps = 35.3, opMargin = 7.4, ps = 3.6, fcfCr = 4200.0, promoterPct = 74.7, fiiDiiPct = 19.8
        ),
        // 26. Adani Ports
        buildStock(
            symbol = "ADANIPORTS",
            companyName = "Adani Ports and SEZ Ltd",
            sector = "Ports & Logistics",
            price = 1442.80, prevClose = 1425.00, dayHigh = 1460.00, dayLow = 1412.00,
            fiftyTwoHigh = 1621.40, fiftyTwoLow = 754.50, volume = 4100000, marketCapCr = 311700.0,
            tags = listOf("Nifty 50", "Ports", "Logistics"),
            pe = 34.2, pb = 5.2, de = 1.05, roe = 16.2, roce = 14.8, divYield = 0.42,
            eps = 42.2, opMargin = 58.4, ps = 10.8, fcfCr = 8900.0, promoterPct = 65.9, fiiDiiPct = 28.4
        ),
        // 27. Asian Paints
        buildStock(
            symbol = "ASIANPAINT",
            companyName = "Asian Paints Ltd",
            sector = "Paints & Chemicals",
            price = 3182.00, prevClose = 3150.00, dayHigh = 3210.00, dayLow = 3130.00,
            fiftyTwoHigh = 3568.00, fiftyTwoLow = 2670.10, volume = 1350000, marketCapCr = 305200.0,
            tags = listOf("Nifty 50", "Paints", "FMCG"),
            pe = 56.8, pb = 16.5, de = 0.12, roe = 30.4, roce = 39.2, divYield = 1.05,
            eps = 56.0, opMargin = 19.4, ps = 8.4, fcfCr = 4800.0, promoterPct = 52.6, fiiDiiPct = 28.5
        ),
        // 28. HCL Technologies
        buildStock(
            symbol = "HCLTECH",
            companyName = "HCL Technologies Ltd",
            sector = "Information Technology",
            price = 1784.50, prevClose = 1762.00, dayHigh = 1798.00, dayLow = 1750.00,
            fiftyTwoHigh = 1860.00, fiftyTwoLow = 1175.00, volume = 3600000, marketCapCr = 484300.0,
            tags = listOf("Nifty 50", "IT", "High Dividend"),
            pe = 28.9, pb = 7.4, de = 0.09, roe = 24.8, roce = 31.5, divYield = 2.92,
            eps = 61.8, opMargin = 18.2, ps = 4.2, fcfCr = 16800.0, promoterPct = 60.8, fiiDiiPct = 34.2
        ),
        // 29. Wipro
        buildStock(
            symbol = "WIPRO",
            companyName = "Wipro Ltd",
            sector = "Information Technology",
            price = 536.40, prevClose = 530.00, dayHigh = 542.00, dayLow = 525.00,
            fiftyTwoHigh = 580.00, fiftyTwoLow = 375.00, volume = 7800000, marketCapCr = 280400.0,
            tags = listOf("Nifty 50", "IT"),
            pe = 23.4, pb = 3.6, de = 0.22, roe = 15.2, roce = 18.4, divYield = 0.19,
            eps = 22.9, opMargin = 16.1, ps = 3.1, fcfCr = 11200.0, promoterPct = 72.8, fiiDiiPct = 17.5
        ),
        // 30. Tech Mahindra
        buildStock(
            symbol = "TECHM",
            companyName = "Tech Mahindra Ltd",
            sector = "Information Technology",
            price = 1622.00, prevClose = 1605.00, dayHigh = 1638.00, dayLow = 1595.00,
            fiftyTwoHigh = 1680.00, fiftyTwoLow = 1082.00, volume = 2200000, marketCapCr = 158400.0,
            tags = listOf("Nifty 50", "IT"),
            pe = 48.2, pb = 5.8, de = 0.14, roe = 12.4, roce = 15.6, divYield = 2.45,
            eps = 33.6, opMargin = 10.8, ps = 2.9, fcfCr = 4900.0, promoterPct = 35.1, fiiDiiPct = 52.8
        ),
        // 31. UltraTech Cement
        buildStock(
            symbol = "ULTRACEMCO",
            companyName = "UltraTech Cement Ltd",
            sector = "Cement & Materials",
            price = 11340.00, prevClose = 11210.00, dayHigh = 11480.00, dayLow = 11150.00,
            fiftyTwoHigh = 12150.00, fiftyTwoLow = 7880.00, volume = 480000, marketCapCr = 327400.0,
            tags = listOf("Nifty 50", "Cement"),
            pe = 46.2, pb = 5.2, de = 0.35, roe = 12.8, roce = 15.4, divYield = 0.62,
            eps = 245.4, opMargin = 17.8, ps = 4.4, fcfCr = 6800.0, promoterPct = 59.9, fiiDiiPct = 30.5
        ),
        // 32. Nestle India
        buildStock(
            symbol = "NESTLEIND",
            companyName = "Nestle India Ltd",
            sector = "FMCG",
            price = 2512.00, prevClose = 2530.00, dayHigh = 2545.00, dayLow = 2498.00,
            fiftyTwoHigh = 2770.00, fiftyTwoLow = 2145.00, volume = 850000, marketCapCr = 242200.0,
            tags = listOf("Nifty 50", "FMCG"),
            pe = 74.5, pb = 78.2, de = 0.15, roe = 108.5, roce = 142.1, divYield = 1.25,
            eps = 33.7, opMargin = 22.4, ps = 11.2, fcfCr = 3400.0, promoterPct = 62.8, fiiDiiPct = 28.4
        ),
        // 33. Britannia
        buildStock(
            symbol = "BRITANNIA",
            companyName = "Britannia Industries Ltd",
            sector = "FMCG",
            price = 5882.00, prevClose = 5840.00, dayHigh = 5920.00, dayLow = 5810.00,
            fiftyTwoHigh = 6040.00, fiftyTwoLow = 4450.00, volume = 520000, marketCapCr = 141600.0,
            tags = listOf("Nifty 50", "FMCG"),
            pe = 64.2, pb = 36.4, de = 0.72, roe = 58.2, roce = 46.5, divYield = 1.24,
            eps = 91.6, opMargin = 17.5, ps = 7.8, fcfCr = 2100.0, promoterPct = 50.5, fiiDiiPct = 34.8
        ),
        // 34. Cipla
        buildStock(
            symbol = "CIPLA",
            companyName = "Cipla Ltd",
            sector = "Pharma & Healthcare",
            price = 1624.00, prevClose = 1610.00, dayHigh = 1640.00, dayLow = 1600.00,
            fiftyTwoHigh = 1690.00, fiftyTwoLow = 1130.00, volume = 1950000, marketCapCr = 131200.0,
            tags = listOf("Nifty 50", "Pharma"),
            pe = 28.5, pb = 4.4, de = 0.02, roe = 16.8, roce = 22.4, divYield = 0.80,
            eps = 56.9, opMargin = 24.2, ps = 4.8, fcfCr = 3800.0, promoterPct = 33.4, fiiDiiPct = 48.2
        ),
        // 35. Dr. Reddy's
        buildStock(
            symbol = "DRREDDY",
            companyName = "Dr. Reddy's Laboratories Ltd",
            sector = "Pharma & Healthcare",
            price = 6752.00, prevClose = 6690.00, dayHigh = 6810.00, dayLow = 6650.00,
            fiftyTwoHigh = 7100.00, fiftyTwoLow = 5210.00, volume = 650000, marketCapCr = 112600.0,
            tags = listOf("Nifty 50", "Pharma"),
            pe = 20.4, pb = 3.6, de = 0.05, roe = 20.1, roce = 26.2, divYield = 0.60,
            eps = 331.0, opMargin = 26.8, ps = 3.8, fcfCr = 4200.0, promoterPct = 26.6, fiiDiiPct = 56.8
        ),
        // 36. Apollo Hospitals
        buildStock(
            symbol = "APOLLOHOSP",
            companyName = "Apollo Hospitals Enterprise Ltd",
            sector = "Healthcare",
            price = 6890.00, prevClose = 6820.00, dayHigh = 6960.00, dayLow = 6780.00,
            fiftyTwoHigh = 7240.00, fiftyTwoLow = 4710.00, volume = 720000, marketCapCr = 99100.0,
            tags = listOf("Nifty 50", "Healthcare", "Hospitals"),
            pe = 84.5, pb = 14.2, de = 0.68, roe = 15.4, roce = 16.8, divYield = 0.22,
            eps = 81.5, opMargin = 12.8, ps = 5.2, fcfCr = 1400.0, promoterPct = 29.3, fiiDiiPct = 61.2
        ),
        // 37. Divi's Laboratories
        buildStock(
            symbol = "DIVISLAB",
            companyName = "Divi's Laboratories Ltd",
            sector = "Pharma API",
            price = 5120.00, prevClose = 5080.00, dayHigh = 5180.00, dayLow = 5030.00,
            fiftyTwoHigh = 5380.00, fiftyTwoLow = 3350.00, volume = 580000, marketCapCr = 135900.0,
            tags = listOf("Nifty 50", "Pharma"),
            pe = 72.8, pb = 9.8, de = 0.01, roe = 13.8, roce = 17.5, divYield = 0.58,
            eps = 70.3, opMargin = 28.5, ps = 14.2, fcfCr = 1800.0, promoterPct = 51.9, fiiDiiPct = 34.5
        ),
        // 38. JSW Steel
        buildStock(
            symbol = "JSWSTEEL",
            companyName = "JSW Steel Ltd",
            sector = "Metals & Mining",
            price = 945.80, prevClose = 938.20, dayHigh = 955.00, dayLow = 930.00,
            fiftyTwoHigh = 1015.00, fiftyTwoLow = 737.00, volume = 4800000, marketCapCr = 231400.0,
            tags = listOf("Nifty 50", "Metals"),
            pe = 26.4, pb = 2.8, de = 1.15, roe = 12.4, roce = 14.1, divYield = 0.78,
            eps = 35.8, opMargin = 15.2, ps = 1.3, fcfCr = 7200.0, promoterPct = 44.8, fiiDiiPct = 36.2
        ),
        // 39. Hindalco
        buildStock(
            symbol = "HINDALCO",
            companyName = "Hindalco Industries Ltd",
            sector = "Metals & Mining",
            price = 685.20, prevClose = 678.00, dayHigh = 694.00, dayLow = 672.00,
            fiftyTwoHigh = 715.00, fiftyTwoLow = 448.00, volume = 6500000, marketCapCr = 154100.0,
            tags = listOf("Nifty 50", "Metals", "Aluminium"),
            pe = 14.8, pb = 1.48, de = 0.55, roe = 11.2, roce = 13.5, divYield = 0.51,
            eps = 46.3, opMargin = 10.4, ps = 0.72, fcfCr = 8900.0, promoterPct = 34.6, fiiDiiPct = 49.8
        ),
        // 40. Grasim
        buildStock(
            symbol = "GRASIM",
            companyName = "Grasim Industries Ltd",
            sector = "Materials & Chemicals",
            price = 2680.00, prevClose = 2650.00, dayHigh = 2710.00, dayLow = 2635.00,
            fiftyTwoHigh = 2870.00, fiftyTwoLow = 1845.00, volume = 1100000, marketCapCr = 182400.0,
            tags = listOf("Nifty 50", "Chemicals", "Cement"),
            pe = 32.1, pb = 2.1, de = 0.88, roe = 7.4, roce = 8.6, divYield = 0.38,
            eps = 83.5, opMargin = 12.8, ps = 1.4, fcfCr = 3900.0, promoterPct = 43.1, fiiDiiPct = 38.2
        ),
        // 41. Bajaj Auto
        buildStock(
            symbol = "BAJAJ-AUTO",
            companyName = "Bajaj Auto Ltd",
            sector = "Automobile",
            price = 11620.00, prevClose = 11480.00, dayHigh = 11750.00, dayLow = 11400.00,
            fiftyTwoHigh = 12774.00, fiftyTwoLow = 4950.00, volume = 620000, marketCapCr = 324100.0,
            tags = listOf("Nifty 50", "Auto", "EV"),
            pe = 41.8, pb = 10.4, de = 0.03, roe = 26.5, roce = 34.8, divYield = 1.72,
            eps = 278.0, opMargin = 19.8, ps = 6.2, fcfCr = 6800.0, promoterPct = 54.9, fiiDiiPct = 28.5
        ),
        // 42. Eicher Motors
        buildStock(
            symbol = "EICHERMOT",
            companyName = "Eicher Motors Ltd",
            sector = "Automobile",
            price = 4880.00, prevClose = 4820.00, dayHigh = 4920.00, dayLow = 4780.00,
            fiftyTwoHigh = 5104.00, fiftyTwoLow = 3377.00, volume = 850000, marketCapCr = 133800.0,
            tags = listOf("Nifty 50", "Auto", "Royal Enfield"),
            pe = 34.2, pb = 7.1, de = 0.04, roe = 22.8, roce = 28.4, divYield = 1.05,
            eps = 142.7, opMargin = 26.2, ps = 7.4, fcfCr = 3200.0, promoterPct = 49.1, fiiDiiPct = 41.2
        ),
        // 43. Hero MotoCorp
        buildStock(
            symbol = "HEROMOTOCO",
            companyName = "Hero MotoCorp Ltd",
            sector = "Automobile",
            price = 5420.00, prevClose = 5380.00, dayHigh = 5480.00, dayLow = 5340.00,
            fiftyTwoHigh = 5894.00, fiftyTwoLow = 2925.00, volume = 950000, marketCapCr = 108300.0,
            tags = listOf("Nifty 50", "Auto"),
            pe = 27.5, pb = 5.8, de = 0.02, roe = 22.4, roce = 29.8, divYield = 2.58,
            eps = 197.1, opMargin = 14.5, ps = 2.8, fcfCr = 4100.0, promoterPct = 34.7, fiiDiiPct = 54.2
        ),
        // 44. Bharat Electronics
        buildStock(
            symbol = "BEL",
            companyName = "Bharat Electronics Ltd",
            sector = "Aerospace & Defence",
            price = 295.40, prevClose = 291.00, dayHigh = 301.00, dayLow = 288.00,
            fiftyTwoHigh = 340.50, fiftyTwoLow = 127.00, volume = 16800000, marketCapCr = 215900.0,
            tags = listOf("Defence", "PSU", "Capital Goods"),
            pe = 48.6, pb = 11.2, de = 0.01, roe = 25.8, roce = 34.2, divYield = 0.75,
            eps = 6.08, opMargin = 24.5, ps = 9.8, fcfCr = 3600.0, promoterPct = 51.1, fiiDiiPct = 34.8
        ),
        // 45. Hindustan Aeronautics
        buildStock(
            symbol = "HAL",
            companyName = "Hindustan Aeronautics Ltd",
            sector = "Aerospace & Defence",
            price = 4750.00, prevClose = 4690.00, dayHigh = 4820.00, dayLow = 4650.00,
            fiftyTwoHigh = 5675.00, fiftyTwoLow = 1930.00, volume = 2800000, marketCapCr = 317600.0,
            tags = listOf("Defence", "PSU", "Aviation"),
            pe = 42.1, pb = 10.8, de = 0.01, roe = 27.4, roce = 36.8, divYield = 0.85,
            eps = 112.8, opMargin = 31.4, ps = 9.4, fcfCr = 6200.0, promoterPct = 71.6, fiiDiiPct = 21.2
        ),
        // 46. Trent
        buildStock(
            symbol = "TRENT",
            companyName = "Trent Ltd",
            sector = "Retail & Fashion",
            price = 7150.00, prevClose = 7020.00, dayHigh = 7240.00, dayLow = 6980.00,
            fiftyTwoHigh = 7450.00, fiftyTwoLow = 1945.00, volume = 1850000, marketCapCr = 254200.0,
            tags = listOf("Nifty 50", "Retail", "Zudio"),
            pe = 148.0, pb = 42.0, de = 0.85, roe = 31.2, roce = 32.5, divYield = 0.05,
            eps = 48.3, opMargin = 16.4, ps = 16.2, fcfCr = 1800.0, promoterPct = 37.0, fiiDiiPct = 48.5
        ),
        // 47. Zomato
        buildStock(
            symbol = "ZOMATO",
            companyName = "Zomato Ltd",
            sector = "Consumer Tech",
            price = 265.40, prevClose = 260.10, dayHigh = 272.00, dayLow = 258.00,
            fiftyTwoHigh = 298.25, fiftyTwoLow = 95.10, volume = 48500000, marketCapCr = 234100.0,
            tags = listOf("Tech", "Quick Commerce", "Blinkit"),
            pe = 115.0, pb = 11.4, de = 0.02, roe = 6.8, roce = 8.2, divYield = 0.0,
            eps = 2.31, opMargin = 8.6, ps = 14.8, fcfCr = 2400.0, promoterPct = 0.0, fiiDiiPct = 76.8
        ),
        // 48. Avenue Supermarts (DMart)
        buildStock(
            symbol = "DMART",
            companyName = "Avenue Supermarts Ltd",
            sector = "Retail & Supermarkets",
            price = 5140.00, prevClose = 5090.00, dayHigh = 5210.00, dayLow = 5050.00,
            fiftyTwoHigh = 5484.00, fiftyTwoLow = 3615.00, volume = 920000, marketCapCr = 334500.0,
            tags = listOf("Retail", "Large Cap"),
            pe = 118.0, pb = 17.5, de = 0.03, roe = 15.8, roce = 20.4, divYield = 0.0,
            eps = 43.5, opMargin = 8.4, ps = 6.4, fcfCr = 2100.0, promoterPct = 74.6, fiiDiiPct = 17.2
        ),
        // 49. Pidilite Industries
        buildStock(
            symbol = "PIDILITIND",
            companyName = "Pidilite Industries Ltd",
            sector = "Adhesives & Chemicals",
            price = 3190.00, prevClose = 3160.00, dayHigh = 3230.00, dayLow = 3140.00,
            fiftyTwoHigh = 3350.00, fiftyTwoLow = 2360.00, volume = 780000, marketCapCr = 162100.0,
            tags = listOf("Chemicals", "FMCG"),
            pe = 82.5, pb = 18.2, de = 0.05, roe = 23.4, roce = 30.8, divYield = 0.50,
            eps = 38.6, opMargin = 22.8, ps = 12.8, fcfCr = 1850.0, promoterPct = 69.8, fiiDiiPct = 19.4
        ),
        // 50. Siemens India
        buildStock(
            symbol = "SIEMENS",
            companyName = "Siemens Ltd",
            sector = "Capital Goods",
            price = 6980.00, prevClose = 6890.00, dayHigh = 7080.00, dayLow = 6840.00,
            fiftyTwoHigh = 7940.00, fiftyTwoLow = 3520.00, volume = 650000, marketCapCr = 248600.0,
            tags = listOf("Capital Goods", "Engineering", "Automation"),
            pe = 78.4, pb = 14.8, de = 0.01, roe = 20.8, roce = 27.2, divYield = 0.18,
            eps = 89.0, opMargin = 14.2, ps = 10.4, fcfCr = 2600.0, promoterPct = 75.0, fiiDiiPct = 16.5
        ),
        // 51. HAL
        buildStock(
            symbol = "HAL",
            companyName = "Hindustan Aeronautics Ltd",
            sector = "Defense & Aerospace",
            price = 4780.00, prevClose = 4720.00, dayHigh = 4840.00, dayLow = 4690.00,
            fiftyTwoHigh = 5675.00, fiftyTwoLow = 1980.00, volume = 1450000, marketCapCr = 319600.0,
            tags = listOf("Defense", "PSU", "Nifty Next 50"),
            pe = 41.2, pb = 10.5, de = 0.0, roe = 28.5, roce = 36.2, divYield = 0.72,
            eps = 116.0, opMargin = 27.4, ps = 10.8, fcfCr = 4900.0, promoterPct = 71.6, fiiDiiPct = 21.8
        ),
        // 52. BEL
        buildStock(
            symbol = "BEL",
            companyName = "Bharat Electronics Ltd",
            sector = "Defense & Aerospace",
            price = 302.50, prevClose = 298.00, dayHigh = 306.00, dayLow = 296.00,
            fiftyTwoHigh = 340.50, fiftyTwoLow = 127.00, volume = 8200000, marketCapCr = 221100.0,
            tags = listOf("Defense", "PSU", "Nifty 50"),
            pe = 52.8, pb = 12.4, de = 0.0, roe = 26.2, roce = 35.1, divYield = 0.74,
            eps = 5.72, opMargin = 25.6, ps = 10.2, fcfCr = 3200.0, promoterPct = 51.1, fiiDiiPct = 38.2
        ),
        // 53. BDL
        buildStock(
            symbol = "BDL",
            companyName = "Bharat Dynamics Ltd",
            sector = "Defense & Aerospace",
            price = 1195.00, prevClose = 1175.00, dayHigh = 1215.00, dayLow = 1165.00,
            fiftyTwoHigh = 1795.00, fiftyTwoLow = 470.00, volume = 950000, marketCapCr = 43800.0,
            tags = listOf("Defense", "PSU", "Missiles"),
            pe = 68.4, pb = 11.8, de = 0.0, roe = 18.2, roce = 23.5, divYield = 0.45,
            eps = 17.5, opMargin = 22.0, ps = 12.4, fcfCr = 750.0, promoterPct = 74.9, fiiDiiPct = 16.2
        ),
        // 54. Mazagon Dock
        buildStock(
            symbol = "MAZDOCK",
            companyName = "Mazagon Dock Shipbuilders Ltd",
            sector = "Defense & Shipbuilding",
            price = 4420.00, prevClose = 4360.00, dayHigh = 4490.00, dayLow = 4310.00,
            fiftyTwoHigh = 5860.00, fiftyTwoLow = 1740.00, volume = 1120000, marketCapCr = 89100.0,
            tags = listOf("Defense", "Shipbuilding", "PSU"),
            pe = 44.5, pb = 14.2, de = 0.0, roe = 34.8, roce = 46.2, divYield = 0.65,
            eps = 99.3, opMargin = 21.5, ps = 9.4, fcfCr = 1850.0, promoterPct = 84.8, fiiDiiPct = 9.5
        ),
        // 55. Cochin Shipyard
        buildStock(
            symbol = "COCHINSHIP",
            companyName = "Cochin Shipyard Ltd",
            sector = "Defense & Shipbuilding",
            price = 1860.00, prevClose = 1825.00, dayHigh = 1895.00, dayLow = 1810.00,
            fiftyTwoHigh = 2975.00, fiftyTwoLow = 465.00, volume = 1650000, marketCapCr = 48900.0,
            tags = listOf("Defense", "Shipbuilding", "PSU"),
            pe = 58.2, pb = 9.8, de = 0.02, roe = 18.4, roce = 24.1, divYield = 0.60,
            eps = 31.9, opMargin = 24.8, ps = 11.2, fcfCr = 920.0, promoterPct = 72.9, fiiDiiPct = 14.8
        ),
        // 56. Trent
        buildStock(
            symbol = "TRENT",
            companyName = "Trent Ltd",
            sector = "Retail & Fashion",
            price = 7320.00, prevClose = 7240.00, dayHigh = 7410.00, dayLow = 7190.00,
            fiftyTwoHigh = 7950.00, fiftyTwoLow = 1950.00, volume = 850000, marketCapCr = 260200.0,
            tags = listOf("Retail", "Tata Group", "Nifty 50"),
            pe = 142.0, pb = 42.5, de = 0.18, roe = 31.2, roce = 38.5, divYield = 0.05,
            eps = 51.5, opMargin = 16.2, ps = 18.4, fcfCr = 1200.0, promoterPct = 37.0, fiiDiiPct = 48.6
        ),
        // 57. Zomato
        buildStock(
            symbol = "ZOMATO",
            companyName = "Zomato Ltd",
            sector = "Consumer Internet",
            price = 265.80, prevClose = 261.20, dayHigh = 271.00, dayLow = 259.00,
            fiftyTwoHigh = 298.50, fiftyTwoLow = 95.00, volume = 14500000, marketCapCr = 234500.0,
            tags = listOf("Internet", "Quick Commerce", "Nifty Next 50"),
            pe = 98.4, pb = 11.2, de = 0.01, roe = 12.4, roce = 15.6, divYield = 0.0,
            eps = 2.70, opMargin = 9.8, ps = 14.8, fcfCr = 1850.0, promoterPct = 0.0, fiiDiiPct = 72.5
        ),
        // 58. Swiggy
        buildStock(
            symbol = "SWIGGY",
            companyName = "Swiggy Ltd",
            sector = "Consumer Internet",
            price = 485.50, prevClose = 478.00, dayHigh = 494.00, dayLow = 472.00,
            fiftyTwoHigh = 560.00, fiftyTwoLow = 390.00, volume = 6800000, marketCapCr = 108400.0,
            tags = listOf("Internet", "Food Delivery", "Quick Commerce"),
            pe = 82.0, pb = 8.5, de = 0.02, roe = 9.8, roce = 11.5, divYield = 0.0,
            eps = 5.92, opMargin = 7.4, ps = 8.2, fcfCr = 650.0, promoterPct = 0.0, fiiDiiPct = 68.4
        ),
        // 59. Nykaa
        buildStock(
            symbol = "NYKAA",
            companyName = "FSN E-Commerce Ventures Ltd",
            sector = "Consumer Internet & E-Commerce",
            price = 212.40, prevClose = 208.50, dayHigh = 216.00, dayLow = 206.00,
            fiftyTwoHigh = 238.00, fiftyTwoLow = 132.00, volume = 3200000, marketCapCr = 60600.0,
            tags = listOf("Beauty", "E-Commerce", "Retail"),
            pe = 115.0, pb = 28.4, de = 0.42, roe = 5.6, roce = 11.2, divYield = 0.0,
            eps = 1.84, opMargin = 6.2, ps = 8.9, fcfCr = 310.0, promoterPct = 52.2, fiiDiiPct = 34.5
        ),
        // 60. Paytm
        buildStock(
            symbol = "PAYTM",
            companyName = "One97 Communications Ltd",
            sector = "Fintech & Payments",
            price = 720.00, prevClose = 705.00, dayHigh = 735.00, dayLow = 698.00,
            fiftyTwoHigh = 998.00, fiftyTwoLow = 310.00, volume = 4800000, marketCapCr = 45800.0,
            tags = listOf("Fintech", "UPI", "Payments"),
            pe = 45.2, pb = 3.6, de = 0.01, roe = 4.8, roce = 6.2, divYield = 0.0,
            eps = 15.9, opMargin = 8.4, ps = 4.8, fcfCr = 450.0, promoterPct = 0.0, fiiDiiPct = 61.2
        ),
        // 61. PB Fintech (PolicyBazaar)
        buildStock(
            symbol = "POLICYBZR",
            companyName = "PB Fintech Ltd",
            sector = "Fintech & Insurance",
            price = 1760.00, prevClose = 1730.00, dayHigh = 1795.00, dayLow = 1715.00,
            fiftyTwoHigh = 1945.00, fiftyTwoLow = 680.00, volume = 1250000, marketCapCr = 79800.0,
            tags = listOf("Fintech", "Insurance", "Online"),
            pe = 74.0, pb = 11.5, de = 0.0, roe = 14.8, roce = 18.2, divYield = 0.0,
            eps = 23.7, opMargin = 12.8, ps = 18.2, fcfCr = 820.0, promoterPct = 0.0, fiiDiiPct = 66.8
        ),
        // 62. Jio Financial Services
        buildStock(
            symbol = "JIOFIN",
            companyName = "Jio Financial Services Ltd",
            sector = "Financial Services",
            price = 345.20, prevClose = 341.00, dayHigh = 351.00, dayLow = 338.00,
            fiftyTwoHigh = 394.00, fiftyTwoLow = 205.00, volume = 9500000, marketCapCr = 219300.0,
            tags = listOf("NBFC", "Reliance Group", "Fintech"),
            pe = 88.5, pb = 1.8, de = 0.05, roe = 2.4, roce = 3.1, divYield = 0.0,
            eps = 3.90, opMargin = 42.0, ps = 45.0, fcfCr = 1400.0, promoterPct = 47.1, fiiDiiPct = 34.2
        ),
        // 63. IRFC
        buildStock(
            symbol = "IRFC",
            companyName = "Indian Railway Finance Corporation",
            sector = "Railways & Financing",
            price = 168.40, prevClose = 165.50, dayHigh = 172.00, dayLow = 164.00,
            fiftyTwoHigh = 229.00, fiftyTwoLow = 65.00, volume = 18500000, marketCapCr = 220100.0,
            tags = listOf("Railways", "PSU", "Financing"),
            pe = 31.4, pb = 4.2, de = 8.4, roe = 14.2, roce = 9.8, divYield = 0.95,
            eps = 5.36, opMargin = 96.2, ps = 8.5, fcfCr = 3200.0, promoterPct = 86.4, fiiDiiPct = 6.8
        ),
        // 64. IRCTC
        buildStock(
            symbol = "IRCTC",
            companyName = "Indian Railway Catering & Tourism Corp",
            sector = "Railways & Tourism",
            price = 942.00, prevClose = 930.00, dayHigh = 955.00, dayLow = 922.00,
            fiftyTwoHigh = 1138.00, fiftyTwoLow = 635.00, volume = 2400000, marketCapCr = 75360.0,
            tags = listOf("Railways", "Tourism", "Monopoly"),
            pe = 62.5, pb = 22.8, de = 0.01, roe = 41.2, roce = 54.8, divYield = 0.70,
            eps = 15.0, opMargin = 34.5, ps = 17.2, fcfCr = 1120.0, promoterPct = 62.4, fiiDiiPct = 19.5
        ),
        // 65. RVNL
        buildStock(
            symbol = "RVNL",
            companyName = "Rail Vikas Nigam Ltd",
            sector = "Railways & Infrastructure",
            price = 560.50, prevClose = 548.00, dayHigh = 574.00, dayLow = 542.00,
            fiftyTwoHigh = 647.00, fiftyTwoLow = 142.00, volume = 7800000, marketCapCr = 116800.0,
            tags = listOf("Railways", "PSU", "Infrastructure"),
            pe = 69.4, pb = 14.2, de = 0.85, roe = 21.4, roce = 17.8, divYield = 0.38,
            eps = 8.07, opMargin = 6.4, ps = 4.8, fcfCr = 940.0, promoterPct = 72.8, fiiDiiPct = 9.4
        ),
        // 66. RailTel
        buildStock(
            symbol = "RAILTEL",
            companyName = "RailTel Corporation of India Ltd",
            sector = "Telecom & Railways",
            price = 452.00, prevClose = 444.00, dayHigh = 462.00, dayLow = 438.00,
            fiftyTwoHigh = 618.00, fiftyTwoLow = 198.00, volume = 2100000, marketCapCr = 14500.0,
            tags = listOf("Telecom", "PSU", "Fiber"),
            pe = 52.0, pb = 7.8, de = 0.0, roe = 15.8, roce = 21.4, divYield = 0.65,
            eps = 8.69, opMargin = 16.5, ps = 5.4, fcfCr = 320.0, promoterPct = 72.8, fiiDiiPct = 8.2
        ),
        // 67. CONCOR
        buildStock(
            symbol = "CONCOR",
            companyName = "Container Corporation of India Ltd",
            sector = "Logistics & Railways",
            price = 980.00, prevClose = 965.00, dayHigh = 998.00, dayLow = 958.00,
            fiftyTwoHigh = 1175.00, fiftyTwoLow = 680.00, volume = 1450000, marketCapCr = 59700.0,
            tags = listOf("Logistics", "Railways", "PSU"),
            pe = 44.5, pb = 5.2, de = 0.01, roe = 11.8, roce = 16.2, divYield = 1.15,
            eps = 22.0, opMargin = 22.5, ps = 6.8, fcfCr = 1250.0, promoterPct = 54.8, fiiDiiPct = 34.6
        ),
        // 68. Suzlon Energy
        buildStock(
            symbol = "SUZLON",
            companyName = "Suzlon Energy Ltd",
            sector = "Renewable & Wind Energy",
            price = 74.80, prevClose = 73.20, dayHigh = 77.00, dayLow = 72.50,
            fiftyTwoHigh = 86.00, fiftyTwoLow = 21.00, volume = 32000000, marketCapCr = 101800.0,
            tags = listOf("Renewable", "Wind", "Green Energy"),
            pe = 76.5, pb = 16.4, de = 0.02, roe = 24.5, roce = 28.2, divYield = 0.0,
            eps = 0.98, opMargin = 15.8, ps = 12.4, fcfCr = 1100.0, promoterPct = 13.3, fiiDiiPct = 42.8
        ),
        // 69. IREDA
        buildStock(
            symbol = "IREDA",
            companyName = "Indian Renewable Energy Dev Agency",
            sector = "Renewable Financing",
            price = 232.00, prevClose = 227.00, dayHigh = 238.00, dayLow = 224.00,
            fiftyTwoHigh = 310.00, fiftyTwoLow = 50.00, volume = 12000000, marketCapCr = 62300.0,
            tags = listOf("Renewable", "PSU", "Financing"),
            pe = 45.8, pb = 7.4, de = 5.2, roe = 17.5, roce = 11.2, divYield = 0.0,
            eps = 5.06, opMargin = 91.0, ps = 11.8, fcfCr = 1450.0, promoterPct = 75.0, fiiDiiPct = 8.5
        ),
        // 70. NHPC
        buildStock(
            symbol = "NHPC",
            companyName = "NHPC Ltd",
            sector = "Renewable & Hydro Power",
            price = 98.40, prevClose = 96.80, dayHigh = 101.00, dayLow = 95.50,
            fiftyTwoHigh = 118.50, fiftyTwoLow = 48.00, volume = 14500000, marketCapCr = 98800.0,
            tags = listOf("Power", "Hydro", "PSU"),
            pe = 24.5, pb = 2.4, de = 0.78, roe = 10.2, roce = 8.5, divYield = 1.95,
            eps = 4.02, opMargin = 52.0, ps = 8.2, fcfCr = 2800.0, promoterPct = 67.4, fiiDiiPct = 18.2
        ),
        // 71. SJVN
        buildStock(
            symbol = "SJVN",
            companyName = "SJVN Ltd",
            sector = "Power & Renewable",
            price = 132.50, prevClose = 130.00, dayHigh = 136.00, dayLow = 128.50,
            fiftyTwoHigh = 170.50, fiftyTwoLow = 65.00, volume = 6500000, marketCapCr = 52100.0,
            tags = listOf("Power", "Hydro", "Solar"),
            pe = 48.2, pb = 3.6, de = 1.15, roe = 7.8, roce = 8.2, divYield = 1.45,
            eps = 2.75, opMargin = 48.5, ps = 16.4, fcfCr = 890.0, promoterPct = 81.8, fiiDiiPct = 7.5
        ),
        // 72. Adani Enterprises
        buildStock(
            symbol = "ADANIENT",
            companyName = "Adani Enterprises Ltd",
            sector = "Conglomerate & Infrastructure",
            price = 3120.00, prevClose = 3080.00, dayHigh = 3165.00, dayLow = 3050.00,
            fiftyTwoHigh = 3450.00, fiftyTwoLow = 2140.00, volume = 1950000, marketCapCr = 355600.0,
            tags = listOf("Adani", "Conglomerate", "Nifty 50"),
            pe = 92.4, pb = 7.8, de = 1.25, roe = 8.5, roce = 11.4, divYield = 0.04,
            eps = 33.7, opMargin = 9.8, ps = 3.6, fcfCr = 4200.0, promoterPct = 74.7, fiiDiiPct = 16.8
        ),
        // 73. Adani Ports
        buildStock(
            symbol = "ADANIPORTS",
            companyName = "Adani Ports and SEZ Ltd",
            sector = "Ports & Infrastructure",
            price = 1465.00, prevClose = 1445.00, dayHigh = 1485.00, dayLow = 1430.00,
            fiftyTwoHigh = 1621.00, fiftyTwoLow = 755.00, volume = 2800000, marketCapCr = 316500.0,
            tags = listOf("Ports", "Adani", "Nifty 50"),
            pe = 34.8, pb = 5.4, de = 0.88, roe = 16.2, roce = 14.8, divYield = 0.42,
            eps = 42.1, opMargin = 58.2, ps = 10.8, fcfCr = 8400.0, promoterPct = 65.9, fiiDiiPct = 24.8
        ),
        // 74. Adani Green
        buildStock(
            symbol = "ADANIGREEN",
            companyName = "Adani Green Energy Ltd",
            sector = "Renewable Energy",
            price = 1890.00, prevClose = 1860.00, dayHigh = 1925.00, dayLow = 1840.00,
            fiftyTwoHigh = 2174.00, fiftyTwoLow = 880.00, volume = 1450000, marketCapCr = 299400.0,
            tags = listOf("Adani", "Solar", "Renewable"),
            pe = 165.0, pb = 28.5, de = 6.4, roe = 18.2, roce = 9.8, divYield = 0.0,
            eps = 11.4, opMargin = 68.4, ps = 28.4, fcfCr = 3200.0, promoterPct = 57.5, fiiDiiPct = 21.2
        ),
        // 75. Adani Power
        buildStock(
            symbol = "ADANIPOWER",
            companyName = "Adani Power Ltd",
            sector = "Power Generation",
            price = 675.00, prevClose = 662.00, dayHigh = 688.00, dayLow = 654.00,
            fiftyTwoHigh = 896.00, fiftyTwoLow = 320.00, volume = 5400000, marketCapCr = 260300.0,
            tags = listOf("Power", "Adani", "Thermal"),
            pe = 14.2, pb = 4.2, de = 0.72, roe = 35.8, roce = 28.4, divYield = 0.0,
            eps = 47.5, opMargin = 41.5, ps = 5.2, fcfCr = 9200.0, promoterPct = 71.8, fiiDiiPct = 17.5
        ),
        // 76. Adani Total Gas
        buildStock(
            symbol = "ATGL",
            companyName = "Adani Total Gas Ltd",
            sector = "Oil & Gas",
            price = 820.00, prevClose = 808.00, dayHigh = 835.00, dayLow = 798.00,
            fiftyTwoHigh = 1260.00, fiftyTwoLow = 530.00, volume = 980000, marketCapCr = 90200.0,
            tags = listOf("Gas", "City Gas", "Adani"),
            pe = 118.0, pb = 24.2, de = 0.38, roe = 21.4, roce = 24.8, divYield = 0.03,
            eps = 6.95, opMargin = 22.4, ps = 18.5, fcfCr = 620.0, promoterPct = 74.8, fiiDiiPct = 18.4
        ),
        // 77. Ambuja Cements
        buildStock(
            symbol = "AMBUJACEM",
            companyName = "Ambuja Cements Ltd",
            sector = "Cement & Building Materials",
            price = 645.00, prevClose = 638.00, dayHigh = 654.00, dayLow = 632.00,
            fiftyTwoHigh = 707.00, fiftyTwoLow = 405.00, volume = 3100000, marketCapCr = 158800.0,
            tags = listOf("Cement", "Adani", "Building Materials"),
            pe = 42.5, pb = 3.8, de = 0.01, roe = 9.8, roce = 12.5, divYield = 0.31,
            eps = 15.1, opMargin = 18.2, ps = 4.8, fcfCr = 2900.0, promoterPct = 70.3, fiiDiiPct = 19.8
        ),
        // 78. Bank of Baroda
        buildStock(
            symbol = "BANKBARODA",
            companyName = "Bank of Baroda",
            sector = "Banking (PSU)",
            price = 252.00, prevClose = 248.50, dayHigh = 256.00, dayLow = 246.00,
            fiftyTwoHigh = 298.00, fiftyTwoLow = 190.00, volume = 12500000, marketCapCr = 130300.0,
            tags = listOf("Banking", "PSU", "Nifty 50"),
            pe = 6.8, pb = 1.05, de = 0.0, roe = 16.5, roce = 7.4, divYield = 2.95,
            eps = 37.0, opMargin = 24.5, ps = 1.1, fcfCr = 8200.0, promoterPct = 63.9, fiiDiiPct = 24.5
        ),
        // 79. Punjab National Bank
        buildStock(
            symbol = "PNB",
            companyName = "Punjab National Bank",
            sector = "Banking (PSU)",
            price = 112.40, prevClose = 110.20, dayHigh = 114.50, dayLow = 109.00,
            fiftyTwoHigh = 143.00, fiftyTwoLow = 65.00, volume = 22000000, marketCapCr = 123800.0,
            tags = listOf("Banking", "PSU", "Nifty Next 50"),
            pe = 10.4, pb = 1.12, de = 0.0, roe = 11.2, roce = 6.8, divYield = 1.35,
            eps = 10.8, opMargin = 21.0, ps = 1.2, fcfCr = 6400.0, promoterPct = 70.1, fiiDiiPct = 16.8
        ),
        // 80. Canara Bank
        buildStock(
            symbol = "CANBK",
            companyName = "Canara Bank",
            sector = "Banking (PSU)",
            price = 108.50, prevClose = 106.80, dayHigh = 110.50, dayLow = 105.00,
            fiftyTwoHigh = 129.00, fiftyTwoLow = 63.00, volume = 14000000, marketCapCr = 98400.0,
            tags = listOf("Banking", "PSU"),
            pe = 6.4, pb = 1.02, de = 0.0, roe = 17.8, roce = 7.9, divYield = 3.10,
            eps = 16.9, opMargin = 23.5, ps = 0.85, fcfCr = 7100.0, promoterPct = 62.9, fiiDiiPct = 21.2
        ),
        // 81. Union Bank of India
        buildStock(
            symbol = "UNIONBANK",
            companyName = "Union Bank of India",
            sector = "Banking (PSU)",
            price = 124.00, prevClose = 122.00, dayHigh = 126.50, dayLow = 120.50,
            fiftyTwoHigh = 155.00, fiftyTwoLow = 82.00, volume = 9500000, marketCapCr = 94700.0,
            tags = listOf("Banking", "PSU"),
            pe = 6.1, pb = 0.95, de = 0.0, roe = 16.9, roce = 7.2, divYield = 2.85,
            eps = 20.3, opMargin = 22.8, ps = 0.92, fcfCr = 6200.0, promoterPct = 74.8, fiiDiiPct = 14.8
        ),
        // 82. IDFC First Bank
        buildStock(
            symbol = "IDFCFIRSTB",
            companyName = "IDFC First Bank Ltd",
            sector = "Banking (Private)",
            price = 78.20, prevClose = 77.00, dayHigh = 79.50, dayLow = 76.50,
            fiftyTwoHigh = 95.00, fiftyTwoLow = 68.00, volume = 16000000, marketCapCr = 58200.0,
            tags = listOf("Banking", "Private", "Retail"),
            pe = 19.8, pb = 1.65, de = 0.0, roe = 9.8, roce = 7.8, divYield = 0.0,
            eps = 3.95, opMargin = 19.2, ps = 1.8, fcfCr = 2800.0, promoterPct = 37.4, fiiDiiPct = 39.8
        ),
        // 83. Federal Bank
        buildStock(
            symbol = "FEDERALBNK",
            companyName = "The Federal Bank Ltd",
            sector = "Banking (Private)",
            price = 192.50, prevClose = 189.50, dayHigh = 195.00, dayLow = 187.00,
            fiftyTwoHigh = 207.00, fiftyTwoLow = 135.00, volume = 8500000, marketCapCr = 47100.0,
            tags = listOf("Banking", "Private"),
            pe = 11.2, pb = 1.48, de = 0.0, roe = 14.2, roce = 8.1, divYield = 0.78,
            eps = 17.2, opMargin = 22.0, ps = 1.7, fcfCr = 2400.0, promoterPct = 0.0, fiiDiiPct = 74.2
        ),
        // 84. IndusInd Bank
        buildStock(
            symbol = "INDUSINDBK",
            companyName = "IndusInd Bank Ltd",
            sector = "Banking (Private)",
            price = 1435.00, prevClose = 1418.00, dayHigh = 1455.00, dayLow = 1405.00,
            fiftyTwoHigh = 1694.00, fiftyTwoLow = 1330.00, volume = 2800000, marketCapCr = 111800.0,
            tags = listOf("Banking", "Private", "Nifty 50"),
            pe = 12.5, pb = 1.68, de = 0.0, roe = 14.8, roce = 8.6, divYield = 1.15,
            eps = 114.8, opMargin = 26.5, ps = 2.2, fcfCr = 4900.0, promoterPct = 15.2, fiiDiiPct = 68.5
        ),
        // 85. Cholamandalam Investment
        buildStock(
            symbol = "CHOLAFIN",
            companyName = "Cholamandalam Investment & Finance",
            sector = "NBFC & Financial Services",
            price = 1480.00, prevClose = 1455.00, dayHigh = 1505.00, dayLow = 1440.00,
            fiftyTwoHigh = 1618.00, fiftyTwoLow = 1040.00, volume = 1350000, marketCapCr = 124500.0,
            tags = listOf("NBFC", "Vehicle Finance", "Murugappa"),
            pe = 32.5, pb = 5.8, de = 5.8, roe = 19.8, roce = 12.4, divYield = 0.15,
            eps = 45.5, opMargin = 31.0, ps = 6.5, fcfCr = 3100.0, promoterPct = 50.4, fiiDiiPct = 38.5
        ),
        // 86. Shriram Finance
        buildStock(
            symbol = "SHRIRAMFIN",
            companyName = "Shriram Finance Ltd",
            sector = "NBFC & Financial Services",
            price = 3280.00, prevClose = 3230.00, dayHigh = 3330.00, dayLow = 3200.00,
            fiftyTwoHigh = 3650.00, fiftyTwoLow = 1780.00, volume = 1450000, marketCapCr = 123400.0,
            tags = listOf("NBFC", "Nifty 50", "Commercial Vehicles"),
            pe = 15.2, pb = 2.4, de = 3.9, roe = 17.5, roce = 12.8, divYield = 1.40,
            eps = 215.8, opMargin = 38.0, ps = 3.4, fcfCr = 4200.0, promoterPct = 25.4, fiiDiiPct = 62.8
        ),
        // 87. Muthoot Finance
        buildStock(
            symbol = "MUTHOOTFIN",
            companyName = "Muthoot Finance Ltd",
            sector = "Gold Loans & NBFC",
            price = 1920.00, prevClose = 1895.00, dayHigh = 1955.00, dayLow = 1880.00,
            fiftyTwoHigh = 2080.00, fiftyTwoLow = 1210.00, volume = 950000, marketCapCr = 77100.0,
            tags = listOf("Gold Loans", "NBFC"),
            pe = 16.8, pb = 3.2, de = 3.1, roe = 20.5, roce = 14.8, divYield = 1.30,
            eps = 114.2, opMargin = 44.0, ps = 5.6, fcfCr = 2800.0, promoterPct = 73.4, fiiDiiPct = 19.8
        ),
        // 88. HDFC Life
        buildStock(
            symbol = "HDFCLIFE",
            companyName = "HDFC Life Insurance Co Ltd",
            sector = "Insurance",
            price = 715.00, prevClose = 706.00, dayHigh = 724.00, dayLow = 700.00,
            fiftyTwoHigh = 775.00, fiftyTwoLow = 560.00, volume = 3400000, marketCapCr = 153800.0,
            tags = listOf("Insurance", "HDFC Group", "Nifty 50"),
            pe = 88.0, pb = 10.5, de = 0.05, roe = 12.8, roce = 11.5, divYield = 0.28,
            eps = 8.12, opMargin = 4.2, ps = 1.6, fcfCr = 3900.0, promoterPct = 50.4, fiiDiiPct = 39.8
        ),
        // 89. SBI Life
        buildStock(
            symbol = "SBILIFE",
            companyName = "SBI Life Insurance Co Ltd",
            sector = "Insurance",
            price = 1780.00, prevClose = 1755.00, dayHigh = 1805.00, dayLow = 1740.00,
            fiftyTwoHigh = 1935.00, fiftyTwoLow = 1340.00, volume = 1650000, marketCapCr = 178300.0,
            tags = listOf("Insurance", "SBI Group", "Nifty 50"),
            pe = 78.4, pb = 11.2, de = 0.0, roe = 15.2, roce = 14.0, divYield = 0.16,
            eps = 22.7, opMargin = 3.8, ps = 1.8, fcfCr = 4200.0, promoterPct = 55.4, fiiDiiPct = 37.2
        ),
        // 90. TVS Motor
        buildStock(
            symbol = "TVSMOTOR",
            companyName = "TVS Motor Company Ltd",
            sector = "Automotive (2-Wheeler)",
            price = 2650.00, prevClose = 2615.00, dayHigh = 2690.00, dayLow = 2595.00,
            fiftyTwoHigh = 2960.00, fiftyTwoLow = 1820.00, volume = 1100000, marketCapCr = 125900.0,
            tags = listOf("Auto", "2-Wheeler", "EV"),
            pe = 58.2, pb = 14.8, de = 1.85, roe = 29.5, roce = 22.4, divYield = 0.38,
            eps = 45.5, opMargin = 11.5, ps = 3.8, fcfCr = 1950.0, promoterPct = 50.3, fiiDiiPct = 36.8
        ),
        // 91. Ashok Leyland
        buildStock(
            symbol = "ASHOKLEY",
            companyName = "Ashok Leyland Ltd",
            sector = "Automotive (Commercial)",
            price = 224.00, prevClose = 220.50, dayHigh = 228.00, dayLow = 218.00,
            fiftyTwoHigh = 260.00, fiftyTwoLow = 160.00, volume = 8500000, marketCapCr = 65800.0,
            tags = listOf("Commercial Vehicles", "Auto", "Hinduja"),
            pe = 22.8, pb = 5.6, de = 0.92, roe = 26.2, roce = 20.5, divYield = 2.15,
            eps = 9.82, opMargin = 12.0, ps = 1.7, fcfCr = 2800.0, promoterPct = 51.5, fiiDiiPct = 32.4
        ),
        // 92. Bharat Forge
        buildStock(
            symbol = "BHARATFORG",
            companyName = "Bharat Forge Ltd",
            sector = "Auto Ancillaries & Defense",
            price = 1580.00, prevClose = 1555.00, dayHigh = 1610.00, dayLow = 1540.00,
            fiftyTwoHigh = 1825.00, fiftyTwoLow = 1050.00, volume = 1450000, marketCapCr = 73600.0,
            tags = listOf("Defense", "Forging", "Auto Components"),
            pe = 48.0, pb = 8.5, de = 1.15, roe = 18.5, roce = 15.2, divYield = 0.42,
            eps = 32.9, opMargin = 17.8, ps = 4.8, fcfCr = 1450.0, promoterPct = 45.2, fiiDiiPct = 42.5
        ),
        // 93. Samvardhana Motherson
        buildStock(
            symbol = "MOTHERSON",
            companyName = "Samvardhana Motherson Int Ltd",
            sector = "Auto Ancillaries",
            price = 198.50, prevClose = 195.00, dayHigh = 202.00, dayLow = 192.50,
            fiftyTwoHigh = 217.00, fiftyTwoLow = 88.00, volume = 14500000, marketCapCr = 139500.0,
            tags = listOf("Auto Ancillaries", "Wiring", "Global"),
            pe = 44.2, pb = 5.2, de = 0.48, roe = 13.8, roce = 15.4, divYield = 0.45,
            eps = 4.49, opMargin = 9.5, ps = 1.4, fcfCr = 3400.0, promoterPct = 60.3, fiiDiiPct = 28.5
        ),
        // 94. Exide Industries
        buildStock(
            symbol = "EXIDEIND",
            companyName = "Exide Industries Ltd",
            sector = "Batteries & EV Storage",
            price = 485.00, prevClose = 476.00, dayHigh = 494.00, dayLow = 470.00,
            fiftyTwoHigh = 620.00, fiftyTwoLow = 250.00, volume = 3200000, marketCapCr = 41200.0,
            tags = listOf("EV", "Batteries", "Energy Storage"),
            pe = 42.8, pb = 3.4, de = 0.05, roe = 8.4, roce = 11.2, divYield = 0.42,
            eps = 11.3, opMargin = 11.8, ps = 2.4, fcfCr = 850.0, promoterPct = 46.0, fiiDiiPct = 32.8
        ),
        // 95. Vedanta
        buildStock(
            symbol = "VEDL",
            companyName = "Vedanta Ltd",
            sector = "Metals & Mining",
            price = 458.00, prevClose = 451.00, dayHigh = 466.00, dayLow = 445.00,
            fiftyTwoHigh = 523.00, fiftyTwoLow = 208.00, volume = 8900000, marketCapCr = 178500.0,
            tags = listOf("Metals", "Zinc", "High Dividend"),
            pe = 14.8, pb = 3.9, de = 1.85, roe = 28.4, roce = 22.8, divYield = 7.45,
            eps = 30.9, opMargin = 26.5, ps = 1.2, fcfCr = 14500.0, promoterPct = 56.4, fiiDiiPct = 28.5
        ),
        // 96. Jindal Steel & Power
        buildStock(
            symbol = "JINDALSTEL",
            companyName = "Jindal Steel & Power Ltd",
            sector = "Metals & Mining",
            price = 985.00, prevClose = 968.00, dayHigh = 1005.00, dayLow = 955.00,
            fiftyTwoHigh = 1090.00, fiftyTwoLow = 620.00, volume = 2900000, marketCapCr = 100500.0,
            tags = listOf("Steel", "Metals"),
            pe = 16.5, pb = 2.2, de = 0.22, roe = 14.2, roce = 17.5, divYield = 0.21,
            eps = 59.7, opMargin = 20.8, ps = 1.9, fcfCr = 5400.0, promoterPct = 61.2, fiiDiiPct = 26.8
        ),
        // 97. SAIL
        buildStock(
            symbol = "SAIL",
            companyName = "Steel Authority of India Ltd",
            sector = "Metals (PSU)",
            price = 138.00, prevClose = 135.50, dayHigh = 141.00, dayLow = 134.00,
            fiftyTwoHigh = 175.00, fiftyTwoLow = 84.00, volume = 14000000, marketCapCr = 57000.0,
            tags = listOf("Steel", "PSU"),
            pe = 18.2, pb = 0.98, de = 0.58, roe = 5.8, roce = 7.5, divYield = 1.45,
            eps = 7.58, opMargin = 9.8, ps = 0.55, fcfCr = 2900.0, promoterPct = 65.0, fiiDiiPct = 17.4
        ),
        // 98. NMDC
        buildStock(
            symbol = "NMDC",
            companyName = "NMDC Ltd",
            sector = "Mining & Mineral Resources",
            price = 228.00, prevClose = 224.50, dayHigh = 232.00, dayLow = 221.00,
            fiftyTwoHigh = 286.00, fiftyTwoLow = 135.00, volume = 6800000, marketCapCr = 66800.0,
            tags = listOf("Mining", "Iron Ore", "PSU"),
            pe = 11.8, pb = 2.4, de = 0.01, roe = 22.8, roce = 29.5, divYield = 3.20,
            eps = 19.3, opMargin = 32.5, ps = 3.1, fcfCr = 4600.0, promoterPct = 60.8, fiiDiiPct = 27.2
        ),
        // 99. DLF
        buildStock(
            symbol = "DLF",
            companyName = "DLF Ltd",
            sector = "Real Estate",
            price = 865.00, prevClose = 852.00, dayHigh = 880.00, dayLow = 845.00,
            fiftyTwoHigh = 967.00, fiftyTwoLow = 490.00, volume = 3800000, marketCapCr = 214100.0,
            tags = listOf("Real Estate", "Residential", "Luxury"),
            pe = 74.0, pb = 5.2, de = 0.05, roe = 7.4, roce = 8.5, divYield = 0.58,
            eps = 11.7, opMargin = 38.0, ps = 31.0, fcfCr = 3900.0, promoterPct = 74.1, fiiDiiPct = 21.5
        ),
        // 100. Godrej Properties
        buildStock(
            symbol = "GODREJPROP",
            companyName = "Godrej Properties Ltd",
            sector = "Real Estate",
            price = 3120.00, prevClose = 3070.00, dayHigh = 3180.00, dayLow = 3030.00,
            fiftyTwoHigh = 3400.00, fiftyTwoLow = 1510.00, volume = 1100000, marketCapCr = 86700.0,
            tags = listOf("Real Estate", "Godrej Group"),
            pe = 96.0, pb = 8.4, de = 0.65, roe = 8.8, roce = 10.2, divYield = 0.0,
            eps = 32.5, opMargin = 28.5, ps = 21.0, fcfCr = 1200.0, promoterPct = 58.5, fiiDiiPct = 34.2
        ),
        // 101. Macrotech Developers (Lodha)
        buildStock(
            symbol = "LODHA",
            companyName = "Macrotech Developers Ltd",
            sector = "Real Estate",
            price = 1240.00, prevClose = 1220.00, dayHigh = 1265.00, dayLow = 1205.00,
            fiftyTwoHigh = 1600.00, fiftyTwoLow = 880.00, volume = 1450000, marketCapCr = 123500.0,
            tags = listOf("Real Estate", "Mumbai", "Residential"),
            pe = 64.5, pb = 7.8, de = 0.32, roe = 13.5, roce = 15.8, divYield = 0.18,
            eps = 19.2, opMargin = 31.5, ps = 11.2, fcfCr = 2800.0, promoterPct = 72.2, fiiDiiPct = 22.8
        ),
        // 102. Indian Hotels (Taj)
        buildStock(
            symbol = "INDHOTEL",
            companyName = "The Indian Hotels Company Ltd",
            sector = "Hospitality & Tourism",
            price = 710.00, prevClose = 698.00, dayHigh = 725.00, dayLow = 690.00,
            fiftyTwoHigh = 810.00, fiftyTwoLow = 380.00, volume = 2200000, marketCapCr = 100900.0,
            tags = listOf("Hospitality", "Taj Hotels", "Tata Group"),
            pe = 72.8, pb = 10.8, de = 0.18, roe = 16.5, roce = 18.2, divYield = 0.25,
            eps = 9.75, opMargin = 32.0, ps = 14.5, fcfCr = 1650.0, promoterPct = 38.2, fiiDiiPct = 46.5
        ),
        // 103. Persistent Systems
        buildStock(
            symbol = "PERSISTENT",
            companyName = "Persistent Systems Ltd",
            sector = "Information Technology",
            price = 5240.00, prevClose = 5160.00, dayHigh = 5320.00, dayLow = 5100.00,
            fiftyTwoHigh = 5950.00, fiftyTwoLow = 3150.00, volume = 650000, marketCapCr = 80600.0,
            tags = listOf("IT", "Cloud", "Software Services"),
            pe = 68.0, pb = 14.5, de = 0.08, roe = 24.5, roce = 30.2, divYield = 0.52,
            eps = 77.0, opMargin = 17.5, ps = 7.8, fcfCr = 1200.0, promoterPct = 31.0, fiiDiiPct = 54.8
        ),
        // 104. Coforge
        buildStock(
            symbol = "COFORGE",
            companyName = "Coforge Ltd",
            sector = "Information Technology",
            price = 6890.00, prevClose = 6790.00, dayHigh = 7010.00, dayLow = 6710.00,
            fiftyTwoHigh = 7650.00, fiftyTwoLow = 4280.00, volume = 580000, marketCapCr = 46200.0,
            tags = listOf("IT", "BFSI", "Travel Tech"),
            pe = 52.5, pb = 9.8, de = 0.35, roe = 21.2, roce = 26.5, divYield = 1.10,
            eps = 131.2, opMargin = 17.0, ps = 4.6, fcfCr = 940.0, promoterPct = 0.0, fiiDiiPct = 78.5
        ),
        // 105. Tata Elxsi
        buildStock(
            symbol = "TATAELXSI",
            companyName = "Tata Elxsi Ltd",
            sector = "IT & Engineering R&D",
            price = 7350.00, prevClose = 7260.00, dayHigh = 7460.00, dayLow = 7180.00,
            fiftyTwoHigh = 9200.00, fiftyTwoLow = 6400.00, volume = 450000, marketCapCr = 45800.0,
            tags = listOf("IT", "Design", "Automotive ER&D", "Tata Group"),
            pe = 56.4, pb = 17.8, de = 0.02, roe = 34.5, roce = 43.8, divYield = 0.95,
            eps = 130.3, opMargin = 29.5, ps = 12.8, fcfCr = 820.0, promoterPct = 43.9, fiiDiiPct = 28.5
        ),
        // 106. KPIT Technologies
        buildStock(
            symbol = "KPITTECH",
            companyName = "KPIT Technologies Ltd",
            sector = "Automotive Software",
            price = 1680.00, prevClose = 1650.00, dayHigh = 1715.00, dayLow = 1630.00,
            fiftyTwoHigh = 1928.00, fiftyTwoLow = 1080.00, volume = 1250000, marketCapCr = 46000.0,
            tags = listOf("Autonomous Driving", "EV Software", "Tech"),
            pe = 64.0, pb = 18.2, de = 0.15, roe = 32.5, roce = 38.0, divYield = 0.38,
            eps = 26.2, opMargin = 20.4, ps = 9.2, fcfCr = 750.0, promoterPct = 39.5, fiiDiiPct = 42.8
        ),
        // 107. Deepak Nitrite
        buildStock(
            symbol = "DEEPAKNTR",
            companyName = "Deepak Nitrite Ltd",
            sector = "Specialty Chemicals",
            price = 2780.00, prevClose = 2740.00, dayHigh = 2825.00, dayLow = 2710.00,
            fiftyTwoHigh = 3120.00, fiftyTwoLow = 1950.00, volume = 720000, marketCapCr = 37900.0,
            tags = listOf("Chemicals", "Phenolics"),
            pe = 44.5, pb = 7.8, de = 0.02, roe = 19.8, roce = 25.4, divYield = 0.32,
            eps = 62.5, opMargin = 16.5, ps = 4.8, fcfCr = 920.0, promoterPct = 49.1, fiiDiiPct = 31.5
        ),
        // 108. Tata Chemicals
        buildStock(
            symbol = "TATACHEM",
            companyName = "Tata Chemicals Ltd",
            sector = "Chemicals & Fertilizers",
            price = 1060.00, prevClose = 1045.00, dayHigh = 1080.00, dayLow = 1035.00,
            fiftyTwoHigh = 1349.00, fiftyTwoLow = 930.00, volume = 1450000, marketCapCr = 27000.0,
            tags = listOf("Chemicals", "Soda Ash", "Tata Group"),
            pe = 42.0, pb = 1.35, de = 0.32, roe = 4.2, roce = 6.8, divYield = 1.65,
            eps = 25.2, opMargin = 16.8, ps = 1.7, fcfCr = 840.0, promoterPct = 37.9, fiiDiiPct = 39.8
        ),
        // 109. SRF
        buildStock(
            symbol = "SRF",
            companyName = "SRF Ltd",
            sector = "Specialty Chemicals & Packaging",
            price = 2420.00, prevClose = 2390.00, dayHigh = 2460.00, dayLow = 2360.00,
            fiftyTwoHigh = 2695.00, fiftyTwoLow = 2050.00, volume = 650000, marketCapCr = 71700.0,
            tags = listOf("Fluorochemicals", "Chemicals", "Packaging"),
            pe = 52.0, pb = 6.4, de = 0.42, roe = 13.8, roce = 16.5, divYield = 0.38,
            eps = 46.5, opMargin = 20.8, ps = 5.2, fcfCr = 1350.0, promoterPct = 50.5, fiiDiiPct = 32.8
        ),
        // 110. Varun Beverages
        buildStock(
            symbol = "VBL",
            companyName = "Varun Beverages Ltd",
            sector = "Consumer Beverages",
            price = 615.00, prevClose = 605.00, dayHigh = 625.00, dayLow = 598.00,
            fiftyTwoHigh = 685.00, fiftyTwoLow = 390.00, volume = 4800000, marketCapCr = 200200.0,
            tags = listOf("FMCG", "PepsiCo Bottler", "Beverages"),
            pe = 84.0, pb = 24.2, de = 0.65, roe = 34.2, roce = 28.5, divYield = 0.22,
            eps = 7.32, opMargin = 23.8, ps = 11.5, fcfCr = 2100.0, promoterPct = 62.7, fiiDiiPct = 29.8
        ),
        // 111. Avenue Supermarts (DMart)
        buildStock(
            symbol = "DMART",
            companyName = "Avenue Supermarts Ltd",
            sector = "Retail Supermarkets",
            price = 4920.00, prevClose = 4860.00, dayHigh = 4980.00, dayLow = 4810.00,
            fiftyTwoHigh = 5480.00, fiftyTwoLow = 3620.00, volume = 850000, marketCapCr = 320100.0,
            tags = listOf("Retail", "DMart", "Supermarket"),
            pe = 112.0, pb = 17.5, de = 0.01, roe = 16.5, roce = 21.8, divYield = 0.0,
            eps = 43.9, opMargin = 8.5, ps = 6.2, fcfCr = 2400.0, promoterPct = 74.6, fiiDiiPct = 17.2
        ),
        // 112. Tata Consumer Products
        buildStock(
            symbol = "TATACONSUM",
            companyName = "Tata Consumer Products Ltd",
            sector = "FMCG & Beverages",
            price = 1180.00, prevClose = 1165.00, dayHigh = 1198.00, dayLow = 1152.00,
            fiftyTwoHigh = 1269.00, fiftyTwoLow = 810.00, volume = 1950000, marketCapCr = 112500.0,
            tags = listOf("FMCG", "Tata Group", "Tea & Salt"),
            pe = 82.0, pb = 6.2, de = 0.18, roe = 8.2, roce = 10.5, divYield = 0.68,
            eps = 14.4, opMargin = 15.2, ps = 7.2, fcfCr = 1850.0, promoterPct = 33.5, fiiDiiPct = 52.8
        ),
        // 113. Marico
        buildStock(
            symbol = "MARICO",
            companyName = "Marico Ltd",
            sector = "FMCG & Consumer Goods",
            price = 645.00, prevClose = 638.00, dayHigh = 654.00, dayLow = 632.00,
            fiftyTwoHigh = 705.00, fiftyTwoLow = 490.00, volume = 2100000, marketCapCr = 83400.0,
            tags = listOf("FMCG", "Edible Oils", "Hair Care"),
            pe = 54.0, pb = 19.8, de = 0.15, roe = 37.8, roce = 44.5, divYield = 1.45,
            eps = 11.9, opMargin = 20.5, ps = 8.4, fcfCr = 1420.0, promoterPct = 59.3, fiiDiiPct = 33.4
        ),
        // 114. Dabur India
        buildStock(
            symbol = "DABUR",
            companyName = "Dabur India Ltd",
            sector = "FMCG & Ayurvedic",
            price = 548.00, prevClose = 542.00, dayHigh = 556.00, dayLow = 538.00,
            fiftyTwoHigh = 672.00, fiftyTwoLow = 495.00, volume = 2600000, marketCapCr = 97100.0,
            tags = listOf("FMCG", "Ayurvedic", "Healthcare"),
            pe = 51.5, pb = 9.8, de = 0.12, roe = 19.8, roce = 23.5, divYield = 1.05,
            eps = 10.6, opMargin = 19.2, ps = 7.8, fcfCr = 1680.0, promoterPct = 66.2, fiiDiiPct = 24.5
        ),
        // 115. Colgate-Palmolive India
        buildStock(
            symbol = "COLPAL",
            companyName = "Colgate-Palmolive (India) Ltd",
            sector = "FMCG & Oral Care",
            price = 3450.00, prevClose = 3410.00, dayHigh = 3495.00, dayLow = 3380.00,
            fiftyTwoHigh = 3850.00, fiftyTwoLow = 1950.00, volume = 850000, marketCapCr = 93800.0,
            tags = listOf("FMCG", "Oral Care", "High ROCE"),
            pe = 68.0, pb = 46.5, de = 0.05, roe = 78.5, roce = 108.2, divYield = 1.65,
            eps = 50.7, opMargin = 33.5, ps = 16.4, fcfCr = 1350.0, promoterPct = 51.0, fiiDiiPct = 38.2
        )
    )

    val stockNewsList: List<StockNews> = listOf(
        StockNews(
            id = "N1",
            stockSymbol = "RELIANCE",
            headline = "Reliance Jio reports 18% YoY subscriber growth; 5G roll-out reaches 85% pan-India coverage",
            source = "The Economic Times",
            timeAgo = "2h ago",
            summary = "Jio Platforms crossed 490 million subscribers with monthly ARPU increasing to ₹181.7 from ₹178.8 in the previous quarter.",
            sentiment = "Bullish",
            tags = listOf("Telecom", "5G", "Earnings")
        ),
        StockNews(
            id = "N2",
            stockSymbol = "TCS",
            headline = "TCS signs $850M 7-year digital transformation deal with European logistics consortium",
            source = "Mint",
            timeAgo = "4h ago",
            summary = "The partnership involves modernizing enterprise cloud systems and automated logistics telemetry across 14 countries.",
            sentiment = "Bullish",
            tags = listOf("Deal Win", "Cloud", "Europe")
        ),
        StockNews(
            id = "N3",
            stockSymbol = "HDFCBANK",
            headline = "HDFC Bank domestic deposits expand 15.2% YoY; gross NPA ratios steady at 1.24%",
            source = "Moneycontrol",
            timeAgo = "6h ago",
            summary = "Total domestic deposits registered a 15.2% uptick to ₹23.8 lakh crore, while net interest margin stood at 3.47%.",
            sentiment = "Neutral",
            tags = listOf("Banking", "Deposits", "NPA")
        ),
        StockNews(
            id = "N4",
            stockSymbol = "INFY",
            headline = "Infosys expands enterprise generative AI suite 'Topaz' with 25 new specialized connectors",
            source = "Business Standard",
            timeAgo = "7h ago",
            summary = "The company announced 120+ active generative AI client proofs of concept with BFSI and retail sector enterprises.",
            sentiment = "Bullish",
            tags = listOf("AI", "Enterprise", "Topaz")
        ),
        StockNews(
            id = "N5",
            stockSymbol = "TATAMOTORS",
            headline = "Tata Motors EV deliveries grow 24% YoY driven by Curvv EV and Punch EV adoptions",
            source = "Autocar Professional",
            timeAgo = "11h ago",
            summary = "Commercial vehicles division reported steady volume while JLR UK order book stood resilient at 145,000 units.",
            sentiment = "Bullish",
            tags = listOf("Auto", "EV", "JLR")
        ),
        StockNews(
            id = "N6",
            stockSymbol = null,
            headline = "RBI Monetary Policy Committee maintains Repo Rate at 6.50% citing resilient macro parameters",
            source = "Financial Express",
            timeAgo = "14h ago",
            summary = "CPI inflation projection for FY27 maintained at 4.5% while real GDP growth projection remained steady at 7.2%.",
            sentiment = "Neutral",
            tags = listOf("Macro", "RBI", "Inflation")
        ),
        StockNews(
            id = "N7",
            stockSymbol = "ICICIBANK",
            headline = "ICICI Bank expands digital SME export credit platform with automated documentation flows",
            source = "Livemint",
            timeAgo = "1d ago",
            summary = "The automated portal reduces export LC turnaround times from 48 hours to under 4 hours using digital bill discounting.",
            sentiment = "Neutral",
            tags = listOf("Fintech", "SME", "Trade")
        ),
        StockNews(
            id = "N8",
            stockSymbol = "BHARTIARTL",
            headline = "Airtel Africa revenue expands 12.4% in constant currency; mobile money volume rises 31%",
            source = "Reuters India",
            timeAgo = "1d ago",
            summary = "Data consumption per customer surged to 6.2 GB per month across East and Central Africa regions.",
            sentiment = "Bullish",
            tags = listOf("Telecom", "Africa", "Airtel")
        )
    )

    val earningsSummaries: Map<String, EarningsTakeaway> = mapOf(
        "RELIANCE" to EarningsTakeaway(
            stockSymbol = "RELIANCE",
            quarter = "Q1 FY27",
            releaseDate = "19 Jul 2026",
            revenueCr = 257822.0,
            revenueYoYGrowthPct = 11.5,
            patCr = 19138.0,
            patYoYGrowthPct = 5.2,
            ebitdaMarginPct = 17.8,
            managementQuotes = listOf(
                "Consumer businesses (Retail and Jio) now contribute over 52% of total consolidated EBITDA.",
                "New Energy giga-factories in Jamnagar are progressing toward phased commercial commissioning in H2 FY27.",
                "Gross debt remained stable with net debt to EBITDA well within prudent internal target thresholds."
            ),
            factualHighlights = listOf(
                "Reliance Retail added 331 new operational stores, bringing total footprint to 18,918 locations.",
                "Oil-to-Chemicals (O2C) segment EBITDA improved 4.8% YoY driven by favorable fuel crack spreads.",
                "Capex for the quarter stood at ₹28,525 crore, funded primarily through internal operating cash flows."
            )
        ),
        "TCS" to EarningsTakeaway(
            stockSymbol = "TCS",
            quarter = "Q1 FY27",
            releaseDate = "11 Jul 2026",
            revenueCr = 62613.0,
            revenueYoYGrowthPct = 5.4,
            patCr = 12040.0,
            patYoYGrowthPct = 8.7,
            ebitdaMarginPct = 26.2,
            managementQuotes = listOf(
                "Total Contract Value (TCV) order book for the quarter stood at $8.3 billion, demonstrating steady enterprise demand.",
                "AI and Gen-AI project pipeline doubled in deal count compared to the prior sequential quarter.",
                "Attrition rate in IT services lowered to 12.1% on a trailing twelve months basis."
            ),
            factualHighlights = listOf(
                "Operating margin expanded by 40 bps YoY to 24.7%.",
                "Workforce count stood at 606,998 with net additions of 5,452 employees.",
                "Interim dividend of ₹10 per equity share declared by the Board of Directors."
            )
        ),
        "HDFCBANK" to EarningsTakeaway(
            stockSymbol = "HDFCBANK",
            quarter = "Q1 FY27",
            releaseDate = "20 Jul 2026",
            revenueCr = 82530.0,
            revenueYoYGrowthPct = 16.1,
            patCr = 16174.0,
            patYoYGrowthPct = 35.3,
            ebitdaMarginPct = 36.4,
            managementQuotes = listOf(
                "Post-merger loan-to-deposit ratio improved by 220 basis points through disciplined deposit mobilization.",
                "Retail advances grew 16.8% YoY, while commercial and rural banking advances grew 19.5% YoY.",
                "Asset quality metrics remained resilient across unsecured and mortgage portfolios."
            ),
            factualHighlights = listOf(
                "Gross NPA stood at 1.33% compared to 1.41% in the corresponding previous quarter.",
                "Net interest income rose to ₹29,837 crore from ₹23,599 crore YoY.",
                "Capital Adequacy Ratio (CAR) under Basel III guidelines stood at 19.3%."
            )
        ),
        "INFY" to EarningsTakeaway(
            stockSymbol = "INFY",
            quarter = "Q1 FY27",
            releaseDate = "18 Jul 2026",
            revenueCr = 39315.0,
            revenueYoYGrowthPct = 3.6,
            patCr = 6368.0,
            patYoYGrowthPct = 7.1,
            ebitdaMarginPct = 22.8,
            managementQuotes = listOf(
                "Large deal TCV recorded at $4.1 billion with 54% net new wins.",
                "Full-year constant currency revenue growth guidance maintained at 3.0% - 4.0%.",
                "Operating margin band maintained at 20% - 22% for the ongoing fiscal year."
            ),
            factualHighlights = listOf(
                "Free cash flow conversion for the quarter reached 115% of net profit.",
                "Digital revenues constituted 63.8% of total reported revenues.",
                "Voluntary attrition for IT services was 12.7%."
            )
        )
    )

    val optionsChainData: List<OptionsStrike> = listOf(
        OptionsStrike(24900.0, 1850000, -210000, 11.2, 345.20, 4820000, 450000, 14.8, 48.30),
        OptionsStrike(25000.0, 3120000, -180000, 11.6, 268.40, 6210000, 890000, 14.2, 72.10),
        OptionsStrike(25100.0, 4850000, 320000, 12.1, 198.50, 4150000, 210000, 13.9, 104.50),
        OptionsStrike(25200.0, 6940000, 940000, 12.8, 138.20, 2890000, -140000, 13.4, 148.00),
        OptionsStrike(25300.0, 5820000, 620000, 13.4, 89.60, 1720000, -220000, 13.1, 201.30),
        OptionsStrike(25400.0, 4210000, 410000, 14.0, 54.10, 980000, -95000, 12.8, 268.70),
        OptionsStrike(25500.0, 5320000, 840000, 14.7, 31.80, 520000, -60000, 12.5, 345.90)
    )

    val brokerAccounts: List<BrokerAccount> = listOf(
        BrokerAccount(
            brokerName = "Zerodha (Kite)",
            brokerLogoColor = 0xFF387ED1,
            accountId = "ZR8921",
            isConnected = true,
            totalHoldingsValue = 642850.0,
            totalInvestedValue = 520000.0,
            cashBalance = 42150.0,
            lastSyncTime = "Today, 15:30 IST",
            familyMemberTag = "Self (Primary)"
        ),
        BrokerAccount(
            brokerName = "Groww",
            brokerLogoColor = 0xFF00D09C,
            accountId = "GR7741",
            isConnected = true,
            totalHoldingsValue = 385400.0,
            totalInvestedValue = 340000.0,
            cashBalance = 15800.0,
            lastSyncTime = "Today, 14:15 IST",
            familyMemberTag = "Spouse (Ananya)"
        ),
        BrokerAccount(
            brokerName = "Angel One",
            brokerLogoColor = 0xFFFF5722,
            accountId = "AO4419",
            isConnected = true,
            totalHoldingsValue = 295000.0,
            totalInvestedValue = 250000.0,
            cashBalance = 24000.0,
            lastSyncTime = "Today, 12:45 IST",
            familyMemberTag = "HUF Account"
        ),
        BrokerAccount(
            brokerName = "Upstox",
            brokerLogoColor = 0xFF7A4FF3,
            accountId = "UP1209",
            isConnected = false,
            totalHoldingsValue = 0.0,
            totalInvestedValue = 0.0,
            cashBalance = 0.0,
            lastSyncTime = "Ready to Connect",
            familyMemberTag = "Child (Trust)"
        )
    )

    fun get10YearFinancials(symbol: String): List<TenYearFinancialRow> {
        val baseRev = when (symbol) {
            "RELIANCE" -> 900000.0
            "TCS" -> 240000.0
            "HDFCBANK" -> 210000.0
            "INFY" -> 153000.0
            "ITC" -> 70000.0
            else -> 85000.0
        }
        val years = (2015..2024).map { "FY$it" }
        return years.mapIndexed { idx, yr ->
            val growthFactor = 0.45 + (idx * 0.065) + ((idx % 3) * 0.02)
            val rev = baseRev * growthFactor
            val ebitda = rev * 0.22
            val pat = rev * 0.14
            val eps = (pat / 350.0)
            val margin = 20.0 + ((idx % 4) * 0.8)
            val debtEquity = when (symbol) {
                "TCS", "INFY", "ITC" -> 0.02 + ((idx % 2) * 0.01)
                "RELIANCE" -> 0.42 - ((idx % 3) * 0.03)
                else -> 0.25 + ((idx % 3) * 0.05)
            }
            val fcf = pat * 0.85
            val roe = 16.0 + ((idx % 5) * 1.5)
            TenYearFinancialRow(
                year = yr,
                revenueCr = String.format(java.util.Locale.US, "%.1f", rev).toDouble(),
                ebitdaCr = String.format(java.util.Locale.US, "%.1f", ebitda).toDouble(),
                patCr = String.format(java.util.Locale.US, "%.1f", pat).toDouble(),
                eps = String.format(java.util.Locale.US, "%.2f", eps).toDouble(),
                operatingMarginPct = String.format(java.util.Locale.US, "%.1f", margin).toDouble(),
                debtToEquity = String.format(java.util.Locale.US, "%.2f", debtEquity).toDouble(),
                freeCashFlowCr = String.format(java.util.Locale.US, "%.1f", fcf).toDouble(),
                roePct = String.format(java.util.Locale.US, "%.1f", roe).toDouble()
            )
        }.reversed() // Most recent first
    }

    fun getSeasonalReturns(symbol: String): List<SeasonalMonthReturn> {
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val seed = symbol.hashCode()
        val rng = Random(seed)
        return months.mapIndexed { mIdx, mName ->
            val bias = when (mName) {
                "Sep" -> 2.8 // Historically strong in Indian markets
                "Nov", "Dec" -> 3.1 // Year-end rally
                "Jan" -> -0.5 // Budget volatility
                "May" -> 1.4
                else -> 0.8
            }
            val yearReturns = (2015..2024).map { yr ->
                val ret = bias + (rng.nextDouble() * 12.0 - 5.5)
                yr to String.format(java.util.Locale.US, "%.1f", ret).toDouble()
            }
            SeasonalMonthReturn(mName, yearReturns)
        }
    }

    val rrgSectorData: List<RrgSectorPoint> = listOf(
        RrgSectorPoint("Nifty IT", 102.8f, 103.4f, "Leading", 0xFF10B981),
        RrgSectorPoint("Nifty Auto", 104.2f, 101.8f, "Leading", 0xFF059669),
        RrgSectorPoint("Nifty Pharma", 101.5f, 98.4f, "Weakening", 0xFFF59E0B),
        RrgSectorPoint("Nifty FMCG", 100.2f, 97.6f, "Weakening", 0xFFD97706),
        RrgSectorPoint("Nifty Metal", 96.5f, 95.8f, "Lagging", 0xFFEF4444),
        RrgSectorPoint("Nifty Media", 94.2f, 97.1f, "Lagging", 0xFFDC2626),
        RrgSectorPoint("Nifty Bank", 98.9f, 102.1f, "Improving", 0xFF3B82F6),
        RrgSectorPoint("Nifty Energy", 99.4f, 101.5f, "Improving", 0xFF2563EB),
        RrgSectorPoint("Nifty Infra", 101.1f, 100.5f, "Leading", 0xFF10B981)
    )

    val fiiDiiHistoricalFlows: List<FiiDiiFlow> = listOf(
        FiiDiiFlow("09 Sep", 1842.5, 2490.8, 3810.0, -1120.0),
        FiiDiiFlow("08 Sep", -624.1, 1950.4, 1450.0, 890.0),
        FiiDiiFlow("05 Sep", 2310.8, 1420.0, 4200.0, -560.0),
        FiiDiiFlow("04 Sep", -1150.2, 2810.5, -980.0, 1840.0),
        FiiDiiFlow("03 Sep", 940.6, 1780.2, 2150.0, 430.0)
    )

    val initialWatchlists: List<Watchlist> = listOf(
        Watchlist(
            id = "wl_core",
            name = "Nifty Core (Default)",
            stockSymbols = listOf("RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK", "BHARTIARTL", "SBIN", "ITC", "HINDUNILVR", "LT"),
            isDefault = true
        ),
        Watchlist(
            id = "wl_tech",
            name = "Tech Compounders",
            stockSymbols = listOf("TCS", "INFY", "HCLTECH", "WIPRO", "TECHM", "LTIM"),
            isDefault = false
        ),
        Watchlist(
            id = "wl_banking",
            name = "Banking & NBFC Leaders",
            stockSymbols = listOf("HDFCBANK", "ICICIBANK", "SBIN", "KOTAKBANK", "AXISBANK", "BAJFINANCE", "BAJAJFINSV"),
            isDefault = false
        ),
        Watchlist(
            id = "wl_dividends",
            name = "High Dividend Cashflow",
            stockSymbols = listOf("ITC", "COALINDIA", "POWERGRID", "NTPC", "ONGC", "VEDL"),
            isDefault = false
        )
    )

    val initialBrokerHoldings: List<BrokerHolding> = listOf(
        BrokerHolding("RELIANCE", "Reliance Industries Ltd", "Zerodha Kite", 150, 2680.00, 2985.40, "Conglomerate & Energy", "Large-Cap Equity"),
        BrokerHolding("TCS", "Tata Consultancy Services", "Zerodha Kite", 80, 3950.00, 4325.80, "Information Technology", "Large-Cap Equity"),
        BrokerHolding("HDFCBANK", "HDFC Bank Ltd", "Zerodha Kite", 220, 1540.00, 1664.20, "Banking (Private)", "Large-Cap Equity"),
        BrokerHolding("HAL", "Hindustan Aeronautics Ltd", "Groww", 65, 3820.00, 4780.00, "Defense & Aerospace", "Mid-Cap Equity"),
        BrokerHolding("BEL", "Bharat Electronics Ltd", "Groww", 500, 240.00, 302.50, "Defense & Aerospace", "Large-Cap Equity"),
        BrokerHolding("TRENT", "Trent Ltd", "Groww", 40, 5400.00, 7320.00, "Retail & Fashion", "Large-Cap Equity"),
        BrokerHolding("ZOMATO", "Zomato Ltd", "Angel One", 1200, 185.00, 265.80, "Consumer Internet", "Mid-Cap Equity"),
        BrokerHolding("IRFC", "Indian Railway Finance Corp", "Angel One", 800, 125.00, 168.40, "Railways & Financing", "PSU Equity"),
        BrokerHolding("SUZLON", "Suzlon Energy Ltd", "Upstox", 3000, 52.00, 74.80, "Renewable Energy", "Small-Cap Equity"),
        BrokerHolding("ICICIBANK", "ICICI Bank Ltd", "ICICI Direct", 180, 1080.00, 1228.60, "Banking (Private)", "Large-Cap Equity"),
        BrokerHolding("ITC", "ITC Ltd", "ICICI Direct", 600, 430.00, 508.40, "FMCG & Diversified", "Large-Cap Equity")
    )
}
