package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.EarningsTakeaway
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun EarningsSummaryModal(
    summary: EarningsTakeaway?,
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen || summary == null) return

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x338B5CF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Earnings Call Digest",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${summary.stockSymbol} • ${summary.quarter} (${summary.releaseDate})",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Factual Only Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x1A2563EB))
                        .border(1.dp, Color(0x332563EB), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Factual Earnings Extraction only. Contains zero investment, buy, sell, or price target ratings.",
                            fontSize = 9.5.sp,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Key Financial Figures Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Revenue
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("REVENUE", fontSize = 9.sp, color = TextMuted)
                            Text(
                                "₹${String.format(Locale.US, "%,.0f", summary.revenueCr)} Cr",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "YoY: +${summary.revenueYoYGrowthPct}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GainGreen
                            )
                        }
                    }

                    // PAT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("NET PROFIT (PAT)", fontSize = 9.sp, color = TextMuted)
                            Text(
                                "₹${String.format(Locale.US, "%,.0f", summary.patCr)} Cr",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "YoY: +${summary.patYoYGrowthPct}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GainGreen
                            )
                        }
                    }

                    // EBITDA Margin
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("EBITDA MARGIN", fontSize = 9.sp, color = TextMuted)
                            Text(
                                "${summary.ebitdaMarginPct}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text("Operating Margin", fontSize = 10.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable content: Highlights & Quotes
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Operational highlights
                    Text(
                        text = "KEY FACTUAL HIGHLIGHTS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    summary.factualHighlights.forEach { highlight ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(5.dp)
                                    .clip(RoundedCornerShape(2.5.dp))
                                    .background(AccentCyan)
                            )
                            Text(
                                text = highlight,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Management Statements
                    Text(
                        text = "VERIFIED EXECUTIVE QUOTES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    summary.managementQuotes.forEach { quote ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF19202D))
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = AccentPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "\"$quote\"",
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
