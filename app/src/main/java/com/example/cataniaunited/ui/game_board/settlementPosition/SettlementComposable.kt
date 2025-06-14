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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
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
    isClickable: Boolean,
    playerId: String,
    onSettlementClick: (Pair<SettlementPosition, Boolean>) -> Unit = {}
) {

    val building: Building? = settlementPosition.building
    val buildingColor = when (building) {
        null -> Color.Transparent // Nothing built yet
        else -> Color(building.color.toColorInt())
    }

    val icon: ImageVector? = when {
        building == null -> null
        building.type == "Settlement" -> ImageVector.vectorResource(id = R.drawable.settlement)
        else -> ImageVector.vectorResource(R.drawable.city)
    }

    val iconTint = if (buildingColor.luminance() > 0.5f) {
        Color.Black //Light background -> dark icon
    } else {
        Color.White //Dark background -> light icon
    }

    val canBuild: Boolean =
        isClickable && (building == null || (building.owner == playerId && building.type == "Settlement"))
    val isUpgrade: Boolean =
        building != null && building.owner == playerId && building.type == "Settlement"

    val actualBorderColor =
        if (isClickable || (building != null)) Color.DarkGray else Color.Transparent
    val borderWidth = 1.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(if (canBuild) Modifier.clickable {
                onSettlementClick(
                    Pair(
                        settlementPosition,
                        isUpgrade
                    )
                )
            } else Modifier)
            .size(size)
            .clip(CircleShape)
            .background(buildingColor)
            .border(borderWidth, actualBorderColor, CircleShape)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size((size.value / 1.6f).toInt().dp)
            )
        }
    }
}