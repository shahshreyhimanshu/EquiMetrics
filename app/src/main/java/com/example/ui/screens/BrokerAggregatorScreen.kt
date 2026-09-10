package com.example.ui.screens

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
import com.example.model.BrokerHolding
import com.example.ui.theme.*
import com.example.viewmodel.MarketViewModel
import java.util.Locale

@Composable
fun BrokerAggregatorScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPro = uiState.isPro

    var brokerList by remember {
        mutableStateOf(
            listOf(
                BrokerConnection("Zerodha Kite", "RBI Account Aggregator (AA)", true, 786250.0, "Fetched 10m ago", "3 Direct Equities"),
                BrokerConnection("Groww", "RBI Account Aggregator (AA)", true, 584900.0, "Fetched 1h ago", "3 Growth Equities"),
                BrokerConnection("Angel One", "RBI Account Aggregator (AA)", false, 453800.0, "Ready to Fetch Holdings", "2 High-Beta Equities"),
                BrokerConnection("Upstox", "RBI Account Aggregator (AA)", false, 224400.0, "Ready to Fetch Holdings", "1 Small-Cap Energy"),
                BrokerConnection("ICICI Direct", "RBI Account Aggregator (AA)", false, 526100.0, "Ready to Fetch Holdings", "2 Core Holdings")
            )
        )
    }

    val connectedBrokers = brokerList.filter { it.isConnected }
    val connectedNames = connectedBrokers.map { it.name }.toSet()

    // Filter holdings belonging to connected brokers
    val activeHoldings = remember(connectedNames) {
        SampleDataProvider.initialBrokerHoldings.filter { it.brokerName in connectedNames }
    }

    val totalInvested = activeHoldings.sumOf { it.totalInvested }
    val totalCurrentValue = activeHoldings.sumOf { it.totalCurrentValue }
    val totalPnl = totalCurrentValue - totalInvested
    val totalPnlPct = if (totalInvested > 0) (totalPnl / totalInvested) * 100 else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Consolidated Portfolio Readings Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "PORTFOLIO TRACKER (RBI AA)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isPro) GainGreen.copy(alpha = 0.2f) else AccentBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isPro) "PRO: UNLIMITED BROKERS" else "FREE: 1 BROKER",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPro) GainGreen else AccentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Total Fetched Holdings Value (Readings Only)", fontSize = 10.sp, color = TextMuted)
                    Text(
                        "₹${String.format(Locale.US, "%,.2f", totalCurrentValue)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Invested", fontSize = 9.5.sp, color = TextMuted)
                            Text(
                                "₹${String.format(Locale.US, "%,.2f", totalInvested)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Unrealized P&L", fontSize = 9.5.sp, color = TextMuted)
                            Text(
                                "${if (totalPnl >= 0) "+" else ""}₹${String.format(Locale.US, "%,.2f", totalPnl)} (${String.format(Locale.US, "%.2f", totalPnlPct)}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPnl >= 0) GainGreen else LossRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Strict Read-Only Zero-Execution Notice
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = GainGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Read-Only Holdings Tracking. Fetches verified holdings from linked accounts via RBI Account Aggregator. Zero trading or execution affordances.",
                                fontSize = 9.5.sp,
                                color = TextSecondary,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Capital Allocation Readings
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Text("Asset Allocation Readings", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text("${activeHoldings.size} Holdings Tracked", fontSize = 9.5.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Allocation bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Box(modifier = Modifier.weight(0.70f).fillMaxHeight().background(AccentBlue))
                        Box(modifier = Modifier.weight(0.18f).fillMaxHeight().background(AccentPurple))
                        Box(modifier = Modifier.weight(0.12f).fillMaxHeight().background(GainGreen))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AllocationLegendItem("Large-Cap Equity", "70%", AccentBlue)
                        AllocationLegendItem("Mid/Small Cap", "18%", AccentPurple)
                        AllocationLegendItem("PSU & Utilities", "12%", GainGreen)
                    }
                }
            }
        }

        // Fetched Holdings Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FETCHED HOLDINGS (${activeHoldings.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Text(
                    text = "Read-Only Feed",
                    fontSize = 9.sp,
                    color = AccentCyan
                )
            }
        }

        // Holdings List Items (Only Readings)
        items(activeHoldings) { holding ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = holding.symbol,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = holding.brokerName,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AccentCyan
                                    )
                                }
                            }
                            Text(
                                text = holding.companyName,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${String.format(Locale.US, "%,.2f", holding.totalCurrentValue)}",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${if (holding.unrealizedPnl >= 0) "+" else ""}₹${String.format(Locale.US, "%,.2f", holding.unrealizedPnl)} (${String.format(Locale.US, "%.2f", holding.unrealizedPnlPct)}%)",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (holding.unrealizedPnl >= 0) GainGreen else LossRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = BorderSubtle, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Qty: ${holding.quantity} • Avg Buy: ₹${holding.averageBuyPrice} • LTP: ₹${holding.currentPrice}",
                            fontSize = 9.5.sp,
                            color = TextMuted
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = holding.assetClass,
                                fontSize = 8.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Linked Broker Consents (Account Aggregator)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACCOUNT AGGREGATOR CONSENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                if (!isPro && connectedBrokers.size >= 1) {
                    Text(
                        text = "Free Limit: 1 Broker (Upgrade for Multi-Broker)",
                        fontSize = 9.5.sp,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        items(brokerList) { broker ->
            val canConnectMore = isPro || connectedBrokers.size < 1 || broker.isConnected

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = broker.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (broker.isConnected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Connected",
                                    tint = GainGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = "${broker.protocol} • ${broker.syncStatus}",
                            fontSize = 9.5.sp,
                            color = TextMuted
                        )
                        Text(
                            text = broker.allocationNote,
                            fontSize = 9.sp,
                            color = AccentCyan
                        )
                    }

                    Button(
                        onClick = {
                            if (!broker.isConnected && !isPro && connectedBrokers.size >= 1) {
                                viewModel.openUpgradeModal()
                            } else {
                                brokerList = brokerList.map {
                                    if (it.name == broker.name) {
                                        it.copy(
                                            isConnected = !it.isConnected,
                                            syncStatus = if (!it.isConnected) "Fetched just now" else "Ready to Fetch Holdings"
                                        )
                                    } else it
                                }
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                broker.isConnected -> DarkSurfaceVariant
                                !canConnectMore -> Color(0xFF1E293B)
                                else -> AccentBlue
                            }
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (!broker.isConnected && !canConnectMore) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                            }
                            Text(
                                text = if (broker.isConnected) "Stop Sync" else if (!canConnectMore) "Upgrade Pro" else "Fetch Holdings",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (broker.isConnected) TextSecondary else Color.White
                            )
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

@Composable
private fun AllocationLegendItem(label: String, percentage: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(7.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text("$label $percentage", fontSize = 9.sp, color = TextSecondary)
    }
}

data class BrokerConnection(
    val name: String,
    val protocol: String,
    val isConnected: Boolean,
    val portfolioValue: Double,
    val syncStatus: String,
    val allocationNote: String
)
