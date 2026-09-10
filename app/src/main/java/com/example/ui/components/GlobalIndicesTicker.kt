package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketIndex
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun GlobalIndicesTicker(
    indices: List<MarketIndex>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F131C))
            .horizontalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Market Status Pulse
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x2600C805))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(GainGreen)
            )
            Text(
                text = "LIVE NSE/BSE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = GainGreen
            )
        }

        indices.forEach { index ->
            IndexPill(index = index)
        }
    }
}

@Composable
fun IndexPill(
    index: MarketIndex,
    modifier: Modifier = Modifier
) {
    val isGain = index.change >= 0
    val color = if (isGain) GainGreen else LossRed

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = index.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Text(
            text = String.format(Locale.US, "%,.2f", index.currentValue),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = TextPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(if (isGain) Color(0x2200C805) else Color(0x22FF3B30))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isGain) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(9.dp)
            )
            Text(
                text = String.format(Locale.US, "%+.2f%%", index.changePct),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
