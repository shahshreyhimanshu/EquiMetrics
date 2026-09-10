package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Stock
import com.example.service.GeminiAiService
import com.example.ui.theme.*
import com.example.viewmodel.MarketViewModel
import kotlinx.coroutines.launch

@Composable
fun AiSuiteScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(0) } // 0: Summarizer, 1: Educator
    var selectedStockSymbol by remember { mutableStateOf("RELIANCE") }
    val currentStock = uiState.allStocks.find { it.symbol == selectedStockSymbol } ?: uiState.allStocks.firstOrNull()

    // Summarizer state
    var summaryText by remember { mutableStateOf("") }
    var isSummarizerLoading by remember { mutableStateOf(false) }

    // Educator state
    var educatorQuery by remember { mutableStateOf("What is the difference between ROCE and ROE?") }
    var educatorResponse by remember { mutableStateOf("") }
    var isEducatorLoading by remember { mutableStateOf(false) }

    // Auto-generate initial summary when screen opens or stock changes
    LaunchedEffect(selectedStockSymbol, uiState.isPro) {
        currentStock?.let { stock ->
            isSummarizerLoading = true
            summaryText = GeminiAiService.summarizeStock(stock, uiState.isPro)
            isSummarizerLoading = false
        }
    }

    // Auto-generate initial educator response
    LaunchedEffect(uiState.isPro) {
        if (educatorResponse.isEmpty()) {
            isEducatorLoading = true
            educatorResponse = GeminiAiService.educateConcept(educatorQuery, uiState.isPro)
            isEducatorLoading = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Plan Status & Pro Upgrade Gating Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (!uiState.isPro) viewModel.openUpgradeModal() else viewModel.toggleProTier()
                    },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isPro) Color(0xFF0F2618) else Color(0xFF1E2536)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (uiState.isPro) GainGreen.copy(alpha = 0.5f) else AccentBlue.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPro) Icons.Default.AutoAwesome else Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (uiState.isPro) GainGreen else AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (uiState.isPro) "AI SUITE: PRO ADVANCED" else "AI SUITE: FREE BASIC",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (uiState.isPro) GainGreen.copy(alpha = 0.25f) else AccentBlue.copy(alpha = 0.25f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (uiState.isPro) "UNLIMITED ADVANCED" else "BASIC MODE",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.isPro) GainGreen else AccentBlue
                                    )
                                }
                            }
                            Text(
                                text = if (uiState.isPro)
                                    "Institutional Moat • Forensic Risk Matrix • Derivation Masterclasses"
                                else
                                    "Basic 3-point summaries & definitions • Tap to unlock Advanced Pro",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Text(
                        text = if (uiState.isPro) "Active" else "Get Pro",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isPro) GainGreen else AccentBlue
                    )
                }
            }
        }

        item {
            // Mode Switcher Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = DarkSurfaceVariant,
                contentColor = AccentBlue,
                indicator = {},
                divider = {},
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    modifier = Modifier
                        .background(if (activeTab == 0) AccentBlue else Color.Transparent)
                        .padding(vertical = 10.dp)
                        .testTag("ai_summarizer_tab")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = if (activeTab == 0) Color.White else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "AI Stock Summarizer",
                            fontSize = 12.sp,
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 0) Color.White else TextMuted
                        )
                    }
                }

                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    modifier = Modifier
                        .background(if (activeTab == 1) AccentBlue else Color.Transparent)
                        .padding(vertical = 10.dp)
                        .testTag("ai_educator_tab")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = if (activeTab == 1) Color.White else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "AI Market Educator",
                            fontSize = 12.sp,
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == 1) Color.White else TextMuted
                        )
                    }
                }
            }
        }

        // ==========================================
        // TAB 0: AI STOCK SUMMARIZER
        // ==========================================
        if (activeTab == 0) {
            item {
                Text(
                    text = "Select Any Listed Equity (${uiState.allStocks.size} stocks live)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Horizontal Stock Selector with live prices
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.allStocks) { stock ->
                        val isSelected = stock.symbol == selectedStockSymbol
                        Card(
                            modifier = Modifier
                                .clickable { selectedStockSymbol = stock.symbol },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AccentBlueSubtle else DarkSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) AccentBlue else BorderSubtle
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(
                                    text = stock.symbol,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AccentCyan else TextPrimary
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "₹${String.format(java.util.Locale.US, "%,.2f", stock.currentPrice)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    val isGain = stock.change >= 0
                                    Text(
                                        text = "${if (isGain) "+" else ""}${String.format(java.util.Locale.US, "%.2f", stock.changePct)}%",
                                        fontSize = 9.sp,
                                        color = if (isGain) GainGreen else LossRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary Content Card
            item {
                currentStock?.let { stock ->
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
                                Column {
                                    Text(
                                        text = stock.companyName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${stock.sector} • P/E: ${stock.ratios.peRatio} • ROCE: ${stock.ratios.rocePercentage}%",
                                        fontSize = 10.5.sp,
                                        color = TextSecondary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isSummarizerLoading = true
                                            summaryText = GeminiAiService.summarizeStock(stock, uiState.isPro)
                                            isSummarizerLoading = false
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Summary",
                                        tint = AccentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            if (isSummarizerLoading) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = AccentCyan,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Synthesizing live metrics with Gemini AI...",
                                        fontSize = 11.5.sp,
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                Text(
                                    text = summaryText,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = TextPrimary
                                )
                            }

                            // Pro Callout Banner if user is Free
                            if (!uiState.isPro) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openUpgradeModal() },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF182236)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = AccentAmber,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Unlock Advanced Institutional AI",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Gain 4-quadrant moat metrics, earnings call sentiment, & forensic risk matrices for all 115 stocks.",
                                                fontSize = 10.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        Button(
                                            onClick = { viewModel.openUpgradeModal() },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("₹299", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 1: AI MARKET EDUCATOR
        // ==========================================
        if (activeTab == 1) {
            item {
                Text(
                    text = "Curated Indian Market Topics",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))

                val presetTopics = listOf(
                    "What is ROCE vs ROE?",
                    "Options Greeks: Delta & Theta",
                    "Put-Call Ratio (PCR) & Max Pain",
                    "How to spot Balance Sheet Red Flags?",
                    "FII vs DII Flow Mechanics",
                    "P/E vs P/B in Indian Equities"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetTopics) { topic ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable {
                                    educatorQuery = topic
                                    coroutineScope.launch {
                                        isEducatorLoading = true
                                        educatorResponse = GeminiAiService.educateConcept(topic, uiState.isPro)
                                        isEducatorLoading = false
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = topic,
                                fontSize = 10.5.sp,
                                color = AccentCyan
                            )
                        }
                    }
                }
            }

            item {
                // Interactive Question Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = educatorQuery,
                        onValueChange = { educatorQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_educator_input"),
                        placeholder = {
                            Text("Ask any market concept (e.g. gamma squeeze, promoter pledge)...", fontSize = 11.sp, color = TextMuted)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = BorderSubtle,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (educatorQuery.isNotBlank()) {
                                    coroutineScope.launch {
                                        isEducatorLoading = true
                                        educatorResponse = GeminiAiService.educateConcept(educatorQuery, uiState.isPro)
                                        isEducatorLoading = false
                                    }
                                }
                            }
                        )
                    )

                    Button(
                        onClick = {
                            if (educatorQuery.isNotBlank()) {
                                coroutineScope.launch {
                                    isEducatorLoading = true
                                    educatorResponse = GeminiAiService.educateConcept(educatorQuery, uiState.isPro)
                                    isEducatorLoading = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Ask",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Educator Result Card
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (uiState.isPro) GainGreen else AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (uiState.isPro) "Institutional Masterclass" else "Basic Concept Capsule",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (uiState.isPro) GainGreen.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (uiState.isPro) "PRO ADVANCED" else "FREE BASIC",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isPro) GainGreen else TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        if (isEducatorLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = AccentCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Formulating educational analysis...",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                        } else {
                            Text(
                                text = educatorResponse,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = TextPrimary
                            )
                        }

                        if (!uiState.isPro) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openUpgradeModal() },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF141C2B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Want Mathematical Derivations & Desk Case Studies?",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentCyan
                                        )
                                        Text(
                                            text = "Pro plan upgrades AI Educator to Institutional Masterclasses.",
                                            fontSize = 9.5.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = AccentCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Statutory Regulatory Disclaimer
            Text(
                text = "SEBI Compliance: AI Summarizer & Educator are strictly educational and analytical utilities. They do not issue buy, sell, or hold advice or price forecasts.",
                fontSize = 9.sp,
                color = TextMuted,
                lineHeight = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
