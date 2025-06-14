package com.example.cataniaunited.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanRessourceBar

@Composable
fun CentralCard(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .width(430.dp)
            .height(400.dp)
            .border(
                width = 9.dp,
                color = catanRessourceBar,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                catanClay.copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}