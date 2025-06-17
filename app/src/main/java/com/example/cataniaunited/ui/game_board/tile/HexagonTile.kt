package com.example.cataniaunited.ui.game_board.tile

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.data.model.TileType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun getTileImageResId(type: TileType): Int {
    return when (type) {
        TileType.WOOD -> R.drawable.wood_tile
        TileType.CLAY -> R.drawable.clay_tile
        TileType.SHEEP -> R.drawable.sheep_tile
        TileType.WHEAT -> R.drawable.wheat_tile
        TileType.ORE -> R.drawable.ore_tile
        TileType.DESERT -> R.drawable.desert_tile
    }
}

// --- Custom Shape for Clipping the Image ---
class HexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val hexagonRadius = minOf(size.width, size.height) / 2f
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val angleOffset = PI / 6 //-(PI / 2)

        // Move to the starting vertex (top center)
        val startX = centerX + hexagonRadius * cos(angleOffset).toFloat()
        val startY = centerY + hexagonRadius * sin(angleOffset).toFloat()
        path.moveTo(startX, startY)

        // Draw the 6 sides
        for (i in 1..6) {
            val angle = angleOffset + (PI / 3) * i // 60 degrees steps
            val x = centerX + hexagonRadius * cos(angle).toFloat()
            val y = centerY + hexagonRadius * sin(angle).toFloat()
            path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun HexagonTile(
    modifier: Modifier = Modifier,
    tile: Tile,
    size: Dp,
    onTileClick: (Tile) -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(size) // Apply size to the outer Box
            .clickable { onTileClick(tile) },
        contentAlignment = Alignment.Center
    ) {

        // --- Display the Image ---
        Image(
            painter = painterResource(id = getTileImageResId(tile.type)),
            contentDescription = "${tile.type} Tile", // Accessibility description
            modifier = Modifier
                .fillMaxSize() // Make image fill the Box
                .clip(HexagonShape()), // Clip the image to a hexagon shape
            contentScale = ContentScale.Crop
        )

        // Draw the number circle and text (only if not desert)
        if (tile.type != TileType.DESERT && tile.value != 0) {
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