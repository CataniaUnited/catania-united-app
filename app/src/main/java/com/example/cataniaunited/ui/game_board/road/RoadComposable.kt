package com.example.cataniaunited.ui.game_board.road

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.cataniaunited.data.model.Road
import kotlin.math.PI

// Helper function to convert radians to degrees
fun Double.toDegrees(): Float = (this * 180.0 / PI).toFloat()

@Composable
fun RoadComposable(
    modifier: Modifier = Modifier,
    road: Road,
    length: Dp,
    thickness: Dp,
    isClickable: Boolean,
    playerId: String,
    onRoadClick: (Road) -> Unit = {}
) {
    val roadColor = when (road.color) {
        null -> Color.Transparent // Placeholder - just show border
        else -> Color(road.color.toColorInt())
    }

    val borderColor = if(isClickable || road.owner != null) Color.DarkGray else Color.Transparent

    val isOccupied = road.owner != null && road.owner != playerId
    val canBuild: Boolean = isClickable && (road.owner == null)

    Box(
        modifier = modifier
            .then(if (canBuild) Modifier.clickable { onRoadClick(road) } else Modifier)
            .width(length)
            .height(thickness)
            .graphicsLayer(
                rotationZ = road.rotationAngle.toDegrees() // Convert radians to degrees
            )
            .background(roadColor)
            .border(1.dp, borderColor)
            .alpha(if (isClickable && isOccupied) 0.3f else 1f)
    )
}