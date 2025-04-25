package com.example.cataniaunited.ui.game_board.settlementPosition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.SettlementPosition

@Composable
fun SettlementComposable(
    modifier: Modifier = Modifier,
    settlementPosition: SettlementPosition,
    size: Dp,
    onSettlementClick: (SettlementPosition) -> Unit = {}
) {
    // TODO: Later, check settlementPosition.building to display different icons/colors
    val buildingColor = when (settlementPosition.building) {
        // TODO: Replace "null" string check later if backend sends actual null
        "null", null -> Color.Transparent // Nothing built yet
        // Add more cases for cities, different players, etc.
        else -> Color.Red // ERROR
    }

    val borderColor = Color.DarkGray

    Box(
        modifier = modifier
            .size(size)
            .clickable { onSettlementClick(settlementPosition) }
            .clip(CircleShape)
            .background(buildingColor)
            .border(1.dp, borderColor, CircleShape)
    )

}
