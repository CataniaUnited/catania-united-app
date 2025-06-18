package com.example.cataniaunited.ui.trade

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.ui.theme.catanClayDark
import com.example.cataniaunited.ui.theme.catanGold

@Composable
fun TradeTypeButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    // A simple Box provides the shape, background, and click behavior.
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp) // Smaller, square button size
            .clip(RoundedCornerShape(20.dp))
            .background(catanGold.copy(alpha = if (enabled) 1f else 0.6f))
            .border(2.dp, catanClayDark, RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = if (enabled) Color.Black else Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
    }
}