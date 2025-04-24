package com.example.cataniaunited.ui.game_borad

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.Tile
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt


private const val LOGICAL_HEX_RADIUS = 10.0f
private val LOGICAL_HEX_WIDTH = (LOGICAL_HEX_RADIUS * sqrt(3.0)).toFloat() // Approx 17.32
private val LOGICAL_HEX_HEIGHT = LOGICAL_HEX_RADIUS * 2.0f            // Approx 20.0

// Helper data class for calculated bounds and scale
private data class BoardLayoutParams(
    val minX: Float = 0.0f,
    val maxX: Float = 0.0f,
    val minY: Float = 0.0f,
    val maxY: Float = 0.0f,
    val scale: Float = 1.0f,
    val boardOffsetX: Float = 0f, // Offset to center the entire board
    val boardOffsetY: Float = 0f,
    val hexSizeDp: Dp = 60.dp     // The calculated size for the HexagonTile Box
)

// Helper to convert Px to Dp within LaunchedEffect
private fun Float.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }

@Composable
fun CatanBoard(modifier: Modifier = Modifier, tiles: List<Tile>) {
    if (tiles.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No board data")
        }
        return
    }

    var layoutParams by remember { mutableStateOf<BoardLayoutParams?>(null) }

    BoxWithConstraints(modifier = modifier.background(Color.Cyan.copy(alpha = 0.1f))) {
        val availableWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val availableHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val density = LocalDensity.current

        LaunchedEffect(tiles, maxWidth, maxHeight, density) {
            if (tiles.isNotEmpty() && availableWidthPx > 0 && availableHeightPx > 0) {
                // Use Float for calculations
                val coordsX = tiles.map { it.coordinates[0].toFloat() }
                val coordsY = tiles.map { it.coordinates[1].toFloat() }
                val minX = coordsX.minOrNull() ?: 0f
                val maxX = coordsX.maxOrNull() ?: 0f
                val minY = coordsY.minOrNull() ?: 0f
                val maxY = coordsY.maxOrNull() ?: 0f

                // Logical extent based on tile centers
                val logicalWidthCenters = (maxX - minX).coerceAtLeast(0f)
                val logicalHeightCenters = (maxY - minY).coerceAtLeast(0f)

                // Add half a hex width/height to each side for the full logical extent
                val totalLogicalWidth = logicalWidthCenters + LOGICAL_HEX_WIDTH
                val totalLogicalHeight = logicalHeightCenters + LOGICAL_HEX_HEIGHT

                if (totalLogicalWidth <= 0 || totalLogicalHeight <= 0) {
                    Log.w("CatanBoard", "Calculated logical dimensions are zero or negative.")
                    layoutParams = null
                    return@LaunchedEffect
                }

                // --- Scaling ---
                // Calculate scale to fit the *total logical extent* into available pixels
                val scaleX = availableWidthPx / totalLogicalWidth
                val scaleY = availableHeightPx / totalLogicalHeight
                val scale = min(scaleX, scaleY).coerceAtLeast(0.01f) // Ensure positive scale

                // --- Calculate Hex Size ---
                // The radius of the hex in pixels after scaling
                val scaledRadiusPx = LOGICAL_HEX_RADIUS * scale
                // A HexagonTile's Box needs to encompass the hex.
                val hexBoxSizePx = 2 * scaledRadiusPx
                val hexSizeDp = hexBoxSizePx.toDp(density) // Convert pixel size to Dp

                // --- Centering Offset ---
                // Calculate the total board size in pixels *after scaling*
                val totalScaledWidthPx = totalLogicalWidth * scale
                val totalScaledHeightPx = totalLogicalHeight * scale

                // Calculate the offset needed to center this scaled board
                val boardOffsetX = (availableWidthPx - totalScaledWidthPx) / 2f
                val boardOffsetY = (availableHeightPx - totalScaledHeightPx) / 2f

                layoutParams = BoardLayoutParams(
                    minX = minX, maxX = maxX, minY = minY, maxY = maxY,
                    scale = scale,
                    boardOffsetX = boardOffsetX, boardOffsetY = boardOffsetY,
                    hexSizeDp = hexSizeDp
                )
                Log.d("CatanBoardLayout", "Layout calculated: $layoutParams")

            } else {
                layoutParams = null
                if (tiles.isEmpty()) Log.w("CatanBoard", "Tile list is empty.")
                if (availableWidthPx <= 0) Log.w("CatanBoard", "Available width is zero.")
                if (availableHeightPx <= 0) Log.w("CatanBoard", "Available height is zero.")
            }
        }

        // Draw the tiles
        layoutParams?.let { params ->
            val currentHexSizePx = with(density) { params.hexSizeDp.toPx() }
            val currentRadiusPx = currentHexSizePx / 2f // Radius derived from the Box size

            // Verify calculated radius matches expected scaled radius
            val expectedRadiusPx = LOGICAL_HEX_RADIUS * params.scale
            if (kotlin.math.abs(currentRadiusPx - expectedRadiusPx) > 0.1f) { // Allow small tolerance
                Log.w("CatanBoardDraw", "Mismatch! currentRadiusPx=$currentRadiusPx, expectedRadiusPx=$expectedRadiusPx")
            }

            tiles.forEach { tile ->
                // --- Calculate Position ---
                // 1. Get logical coordinate relative to the minimum coord (origin)
                val relativeX = tile.coordinates[0].toFloat() - params.minX
                val relativeY = tile.coordinates[1].toFloat() - params.minY // Assuming Y increases downwards on screen relative to logical Y

                // 2. Scale the relative logical position
                val scaledX = relativeX * params.scale
                val scaledY = relativeY * params.scale

                // 3. Add the centering offset of the *entire board*
                // 4. Add offset for half a hex width/height because minX/minY is a *center*
                val finalX = params.boardOffsetX + (LOGICAL_HEX_WIDTH / 2f * params.scale) + scaledX
                val finalY = params.boardOffsetY + (LOGICAL_HEX_HEIGHT / 2f * params.scale) + scaledY

                // 5. Calculate offset for the Composable (top-left corner)
                //    The 'finalX/Y' calculated above is the desired *center* position.
                //    Subtract half the Box size to get the top-left.
                val composableOffsetX = finalX - (currentHexSizePx / 2f)
                val composableOffsetY = finalY - (currentHexSizePx / 2f)

                HexagonTile(
                    modifier = Modifier
                        .offset {
                            IntOffset(composableOffsetX.roundToInt(), composableOffsetY.roundToInt())
                        },
                    tile = tile,
                    size = params.hexSizeDp // Use the calculated Dp size for the Box
                )
            }
        } ?: run {
            // Show loading/calculating state
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Calculating layout...")
            }
        }
    }
}