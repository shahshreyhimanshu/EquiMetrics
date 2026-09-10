package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RazorpayUpgradeModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onPaymentComplete: (plan: String) -> Unit
) {
    if (!isOpen) return

    var selectedPlan by remember { mutableStateOf("Annual Pro") } // "Monthly Pro" or "Annual Pro"
    var paymentMethod by remember { mutableStateOf("UPI Autopay") } // "UPI Autopay", "Cards", "Netbanking"
    var isProcessing by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
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
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Upgrade to EquiMetrics Pro",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Institutional Analytics & Uncapped Simulator",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isSuccess) {
                    // Plan Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Monthly Plan
                        val isMonthly = selectedPlan == "Monthly Pro"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isMonthly) AccentBlueSubtle else DarkSurfaceVariant)
                                .border(1.5.dp, if (isMonthly) AccentBlue else BorderSubtle, RoundedCornerShape(10.dp))
                                .clickable { selectedPlan = "Monthly Pro" }
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Monthly Pro", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹299", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("/ month", fontSize = 9.sp, color = TextMuted)
                            }
                        }

                        // Annual Plan
                        val isAnnual = selectedPlan == "Annual Pro"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAnnual) AccentBlueSubtle else DarkSurfaceVariant)
                                .border(1.5.dp, if (isAnnual) AccentBlue else BorderSubtle, RoundedCornerShape(10.dp))
                                .clickable { selectedPlan = "Annual Pro" }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Annual Pro", fontSize = 11.sp, color = TextSecondary)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GainGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("SAVE 17%", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = GainGreen)
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("₹2,999", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("/ year (billed annually)", fontSize = 9.sp, color = TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pro vs Free Comparison Matrix
                    Text(
                        text = "FREE VS PRO FEATURE COMPARISON",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("10-Yr Financials & Excel", "3 Years", "10 Years + CSV/Excel"),
                            Triple("Custom Screener", "Pre-set filters", "Custom Formula Builder"),
                            Triple("Price & Event Alerts", "Push (Delayed)", "Instant WhatsApp/Telegram"),
                            Triple("Watchlists Capacity", "1 List (Max 10)", "Unlimited Lists & Stocks"),
                            Triple("AI Earnings Summaries", "2 Transcripts/mo", "Unlimited Access"),
                            Triple("Broker Portfolio Sync", "1 Broker", "Multi-Broker / Family"),
                            Triple("Strategy Backtester", "1 Year Sample", "10-Year Full Market Sim")
                        ).forEach { (feat, freeVal, proVal) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(feat, fontSize = 9.5.sp, color = TextPrimary, modifier = Modifier.weight(1.3f))
                                Text(freeVal, fontSize = 9.sp, color = TextMuted, modifier = Modifier.weight(1f))
                                Text(proVal, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GainGreen, modifier = Modifier.weight(1.2f))
                            }
                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Razorpay Payment Method Selector
                    Text(
                        text = "SECURE RAZORPAY CHECKOUT METHOD",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("UPI Autopay", "Cards", "Netbanking").forEach { method ->
                            val isSelected = paymentMethod == method
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0x332563EB) else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) AccentBlue else BorderSubtle, RoundedCornerShape(6.dp))
                                    .clickable { paymentMethod = method }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }

                    if (paymentMethod == "UPI Autopay") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Supports Google Pay, PhonePe, Paytm & BHIM UPI Recurring Mandates",
                            fontSize = 9.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkout Button
                    Button(
                        onClick = {
                            isProcessing = true
                            scope.launch {
                                delay(1200) // Simulated Razorpay gateway roundtrip
                                isProcessing = false
                                isSuccess = true
                                delay(1000)
                                onPaymentComplete(if (selectedPlan == "Annual Pro") "Annual Pro (₹2,999/yr)" else "Monthly Pro (₹299/mo)")
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing Razorpay Checkout...", fontSize = 12.sp, color = Color.White)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "PAY ${if (selectedPlan == "Annual Pro") "₹2,999" else "₹299"} VIA RAZORPAY",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // Success View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = GainGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "EquiMetrics Pro Activated!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Your subscription has been verified via Razorpay Autopay. Enjoy uncapped analytical tools.",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
