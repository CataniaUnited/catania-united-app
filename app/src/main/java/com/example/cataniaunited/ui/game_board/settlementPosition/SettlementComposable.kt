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
import androidx.compose.ui.draw.alpha
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
    onSettlementClick: (SettlementPosition) -> Unit = {}
) {

    val building: Building? = settlementPosition.building
    val buildingColor = when (building) {
        null -> Color.Transparent // Nothing built yet
        else -> Color(building.color.toColorInt())
    }
    val borderColor = if(isClickable || (building != null)) Color.DarkGray else Color.Transparent

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

    val isOccupied = building != null && building.owner != playerId
    val canBuild: Boolean = isClickable && (building == null || (building.owner == playerId && building.type == "Settlement"))

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(if (canBuild) Modifier.clickable { onSettlementClick(settlementPosition)  } else Modifier)
            .size(size)
            .clip(CircleShape)
            .background(buildingColor)
            .border(1.dp, borderColor, CircleShape)
            .alpha(if (isClickable && isOccupied) 0.3f else 1f)
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
