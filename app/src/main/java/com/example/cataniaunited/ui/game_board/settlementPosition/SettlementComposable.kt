package com.example.cataniaunited.ui.game_board.settlementPosition

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.Building
import com.example.cataniaunited.data.model.SettlementPosition

@Composable
fun SettlementComposable(
    modifier: Modifier = Modifier,
    settlementPosition: SettlementPosition,
    size: Dp,
    onSettlementClick: (SettlementPosition) -> Unit = {}
) {

    val building: Building? = settlementPosition.building;
    val buildingColor = when (building) {
        null -> Color.Transparent // Nothing built yet
        else -> Color(building.color.toColorInt())
    }
    val borderColor = Color.DarkGray

    val icon: Painter? = when {
        building == null -> null
        building.type == "Settlement" -> painterResource(id = R.drawable.home_group)
        else -> painterResource(id = R.drawable.city)
    }

    val iconTint = if (buildingColor.luminance() > 0.5f) {
        Color.Black //Light background -> dark icon
    } else {
        Color.White //Dark background -> light icon
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clickable { onSettlementClick(settlementPosition) }
            .clip(CircleShape)
            .background(buildingColor)
            .border(1.dp, borderColor, CircleShape)
    ) {

        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(size / 1.6f)
            )
        }
    }

}
