package com.example.cataniaunited.ui.game_board.road

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.Road
import kotlin.math.PI

// Helper function to convert radians to degrees
fun Double.toDegrees(): Float = (this * 180.0 / PI).toFloat()

@Composable
fun RoadComposable(
    modifier: Modifier = Modifier,
    road: Road,
    length: Dp,
    thickness: Dp = 6.dp // TODO: Adjust thickness
) {
    // TODO: Later, check road.owner to display different colors
    val roadColor = when (road.owner) {
        null -> Color.Transparent // Placeholder - just show border
        else -> Color.Red // Error or unknown owner state
    }

    val borderColor = Color.DarkGray

    Box(
        modifier = modifier
            .width(length) // The length connects two settlement points
            .height(thickness)
            .graphicsLayer(
                rotationZ = road.rotationAngle.toDegrees() // Convert radians to degrees
            )
            .background(roadColor)
            .border(1.dp, borderColor) // Show border for placeholder visibility
    )
}