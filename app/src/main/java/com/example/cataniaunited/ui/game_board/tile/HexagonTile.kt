package com.example.cataniaunited.ui.game_board.tile

//draw a single hexagon tile with its color and number.

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.data.model.TileType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Define colors for each resource type
fun getTileColor(type: TileType): Color {
    return when (type) {
        TileType.WOOD -> Color(0xFF006400) // Dark Green
        TileType.CLAY -> Color(0xFFB87333) // Orange/Brown (Brick)
        TileType.SHEEP -> Color(0xFF90EE90) // Light Green
        TileType.WHEAT -> Color(0xFFF4A460) // Yellow/Gold
        TileType.ORE -> Color(0xFF808080) // Gray
        TileType.WASTE -> Color(0xFFD2B48C) // Tan (Desert)
    }
}

// Create a Hexagon Path
fun createHexagonPath(size: Size): Path {
    val path = Path()
    val hexagonSize = minOf(size.width, size.height) / 2f // Radius based on smallest dimension
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val angleOffset = PI / 6

    // Start at the top-right vertex
    val startX = centerX + hexagonSize * cos(angleOffset).toFloat()
    val startY = centerY + hexagonSize * sin(angleOffset).toFloat()
    path.moveTo(startX, startY)

    // Draw the 6 sides
    for (i in 1..6) {
        val angle = angleOffset + (PI / 3) * i // 60 degrees steps
        val x = centerX + hexagonSize * cos(angle).toFloat()
        val y = centerY + hexagonSize * sin(angle).toFloat()
        path.lineTo(x, y)
    }
    path.close()
    return path
}

@Composable
fun HexagonTile(
    modifier: Modifier = Modifier,
    tile: Tile,
    size: Dp = 60.dp // TODO: calculate this based on screen size/zoom
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val hexagonPath = createHexagonPath(this.size)
            // Fill the hexagon
            drawPath(
                path = hexagonPath,
                color = getTileColor(tile.type)
            )
        }

        // Draw the number circle and text (only if not desert/waste)
        if (tile.type != TileType.WASTE && tile.value != 0) {
            // Consider making the circle slightly larger if text feels cramped
            val circleSizeFraction = 0.45f
            val fontSizeFraction = 0.22f

            Box(
                modifier = Modifier
                    .size(size * circleSizeFraction)
                    .graphicsLayer {
                        clip = true
                        shape = CircleShape
                    }
                    .background(Color(0xFFFFFDD0)),
                contentAlignment = Alignment.Center
            ) {
                val numberColor = if (tile.value == 6 || tile.value == 8) Color.Red else Color.Black


                val calculatedFontSize = (size.value * fontSizeFraction).sp

                Text(
                    text = tile.value.toString(),
                    color = numberColor,
                    fontSize = calculatedFontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHexagonTile() {
    Row {
        HexagonTile(tile = Tile(1, TileType.WOOD, 11, listOf(0.0, 0.0)))
        HexagonTile(tile = Tile(2, TileType.ORE, 8, listOf(0.0, 0.0)))
        HexagonTile(tile = Tile(3, TileType.WASTE, 0, listOf(0.0, 0.0)))
    }
}