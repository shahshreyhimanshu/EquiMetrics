package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketDepthItem
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun MarketDepthTable(
    depthItems: List<MarketDepthItem>,
    modifier: Modifier = Modifier,
    feedSourceName: String = "Angel One SmartAPI",
    onOpenFeedSettings: (() -> Unit)? = null
) {
    val totalBidQty = depthItems.sumOf { it.bidQty }
    val totalAskQty = depthItems.sumOf { it.askQty }
    val totalQty = (totalBidQty + totalAskQty).coerceAtLeast(1)
    val bidRatioPct = (totalBidQty.toFloat() / totalQty) * 100f
    val askRatioPct = (totalAskQty.toFloat() / totalQty) * 100f
    val maxQty = maxOf(depthItems.maxOfOrNull { it.bidQty } ?: 1, depthItems.maxOfOrNull { it.askQty } ?: 1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "5-LEVEL MARKET DEPTH (L2)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Real-time Order Book • $feedSourceName",
                    fontSize = 10.sp,
                    color = AccentCyan
                )
            }

            if (onOpenFeedSettings != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                        .clickable { onOpenFeedSettings() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Gateway Settings",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            } else {
                Text(
                    text = "Live Gateway Active",
                    fontSize = 10.sp,
                    color = GainGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ratio Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .weight(bidRatioPct.coerceAtLeast(1f))
                    .fillMaxHeight()
                    .background(GainGreen)
            )
            Box(
                modifier = Modifier
                    .weight(askRatioPct.coerceAtLeast(1f))
                    .fillMaxHeight()
                    .background(LossRed)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Bid: ${totalBidQty} (${String.format(Locale.US, "%.1f", bidRatioPct)}%)",
                fontSize = 10.sp,
                color = GainGreen,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Ask: ${totalAskQty} (${String.format(Locale.US, "%.1f", askRatioPct)}%)",
                fontSize = 10.sp,
                color = LossRed,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Orders", fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(1f))
            Text("Qty", fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(1f))
            Text("Bid Price", fontSize = 9.sp, color = GainGreen, modifier = Modifier.weight(1.2f))
            Text("Ask Price", fontSize = 9.sp, color = LossRed, modifier = Modifier.weight(1.2f))
            Text("Qty", fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(1f))
            Text("Orders", fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(1f))
        }

        // Table Rows
        depthItems.forEach { item ->
            val bidBarPct = (item.bidQty.toFloat() / maxQty).coerceIn(0f, 1f)
            val askBarPct = (item.askQty.toFloat() / maxQty).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
            ) {
                // Background depth fill
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(bidBarPct)
                                .align(Alignment.CenterEnd)
                                .background(GainGreen.copy(alpha = 0.12f))
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(askBarPct)
                                .align(Alignment.CenterStart)
                                .background(LossRed.copy(alpha = 0.12f))
                        )
                    }
                }

                // Row text values
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${item.bidOrders}", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    Text("${item.bidQty}", fontSize = 10.sp, color = TextPrimary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    Text(String.format(Locale.US, "%.2f", item.bidPrice), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = GainGreen, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                    Text(String.format(Locale.US, "%.2f", item.askPrice), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = LossRed, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                    Text("${item.askQty}", fontSize = 10.sp, color = TextPrimary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    Text("${item.askOrders}", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
