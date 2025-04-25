package com.example.cataniaunited.ui.game_board.board

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
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.ui.game_board.road.RoadComposable
import com.example.cataniaunited.ui.game_board.settlementPosition.SettlementComposable
import com.example.cataniaunited.ui.game_board.tile.HexagonTile
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt


private const val LOGICAL_HEX_RADIUS = 10.0f
private val LOGICAL_HEX_WIDTH = (LOGICAL_HEX_RADIUS * sqrt(3.0)).toFloat() // Approx 17.32
private val LOGICAL_HEX_HEIGHT = LOGICAL_HEX_RADIUS * 2.0f            // Approx 20.0

// Helper data class for calculated bounds and scale
private data class BoardLayoutParams(
    val scale: Float = 1.0f,
    val boardOffsetX: Float = 0f,
    val boardOffsetY: Float = 0f,
    val hexSizeDp: Dp = 60.dp,
    val settlementSizeDp: Dp = 15.dp,
    val drawableRoadLengthDp: Dp = 40.dp
)

// Helper to convert Px to Dp within LaunchedEffect/Density scope
private fun Float.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }

@Composable
fun CatanBoard(
    modifier: Modifier = Modifier,
    tiles: List<Tile>,
    settlementPositions: List<SettlementPosition>,
    roads: List<Road>,
    padding: Dp = 16.dp,
    boardBackgroundColor: Color = Color(0xff177fde)
) {
    if (tiles.isEmpty()) {
        // Apply background to the empty state box
        Box(modifier = modifier.fillMaxSize().background(boardBackgroundColor), contentAlignment = Alignment.Center) {
            Text("No board data", color = Color.White)
        }
        return
    }

    var layoutParams by remember { mutableStateOf<BoardLayoutParams?>(null) }

    // Apply the background color to the main BoxWithConstraints
    BoxWithConstraints(modifier = modifier.background(boardBackgroundColor)) {
        val density = LocalDensity.current
        val availableWidthPx = with(density) { maxWidth.toPx() }
        val availableHeightPx = with(density) { maxHeight.toPx() }
        val paddingPx = with(density) { padding.toPx() }

        // Calculate effective available space *after* accounting for padding
        val effectiveAvailableWidthPx = max(0f, availableWidthPx - 2 * paddingPx)
        val effectiveAvailableHeightPx = max(0f, availableHeightPx - 2 * paddingPx)


        LaunchedEffect(tiles, maxWidth, maxHeight, density, padding) { // Add padding as a key
            // Ensure we have tiles and *effective* space to draw in
            if (tiles.isNotEmpty() && effectiveAvailableWidthPx > 0 && effectiveAvailableHeightPx > 0) {

                // --- Basic Coordinates & Bounds (Tile Centers Only) ---
                val coordsX = tiles.map { it.coordinates[0].toFloat() }
                val coordsY = tiles.map { it.coordinates[1].toFloat() }
                val minX = coordsX.minOrNull() ?: 0f
                val maxX = coordsX.maxOrNull() ?: 0f
                val minY = coordsY.minOrNull() ?: 0f
                val maxY = coordsY.maxOrNull() ?: 0f

                val logicalWidthCenters = (maxX - minX).coerceAtLeast(0f)
                val logicalHeightCenters = (maxY - minY).coerceAtLeast(0f)

                val logicalWidthForScale = if (logicalWidthCenters == 0f && logicalHeightCenters == 0f) 1f else logicalWidthCenters
                val logicalHeightForScale = if (logicalWidthCenters == 0f && logicalHeightCenters == 0f) 1f else logicalHeightCenters

                if (logicalWidthForScale <= 0 && logicalHeightForScale <= 0) {
                    Log.w("CatanBoard", "Both logical dimensions are zero or negative.")
                    layoutParams = null
                    return@LaunchedEffect
                }

                // --- Scaling ---
                val scaleX = if (logicalWidthForScale > 0) effectiveAvailableWidthPx / logicalWidthForScale else Float.MAX_VALUE
                val scaleY = if (logicalHeightForScale > 0) effectiveAvailableHeightPx / logicalHeightForScale else Float.MAX_VALUE
                var scale = min(scaleX, scaleY).coerceAtLeast(0.01f)

                // --- Calculate Sizes ---
                fun calculateSizes(currentScale: Float): Triple<Dp, Dp, Dp> {
                    val scaledHexRadiusPx = LOGICAL_HEX_RADIUS * currentScale
                    val hexSizeDp = (2 * scaledHexRadiusPx).toDp(density)
                    val settlementSizeDp = hexSizeDp * 0.25f
                    val settlementDiameterPx = with(density) { settlementSizeDp.toPx() }
                    val centerToCenterRoadLengthPx = LOGICAL_HEX_RADIUS * currentScale
                    val drawableRoadLengthPx = (centerToCenterRoadLengthPx - settlementDiameterPx).coerceAtLeast(1f)
                    val drawableRoadLengthDp = drawableRoadLengthPx.toDp(density)
                    return Triple(hexSizeDp, settlementSizeDp, drawableRoadLengthDp)
                }

                var (hexSizeDp, settlementSizeDp, drawableRoadLengthDp) = calculateSizes(scale)

                // --- Element Padding ---
                val scaledHexWidthPx = LOGICAL_HEX_WIDTH * scale
                val scaledHexHeightPx = LOGICAL_HEX_HEIGHT * scale
                val elementPaddingNeededX = scaledHexWidthPx / 2f
                val elementPaddingNeededY = scaledHexHeightPx / 2f

                // --- Re-evaluate Scale ---
                val requiredWidthWithElementPadding = (logicalWidthCenters * scale) + (elementPaddingNeededX * 2)
                val requiredHeightWithElementPadding = (logicalHeightCenters * scale) + (elementPaddingNeededY * 2)

                if (requiredWidthWithElementPadding > effectiveAvailableWidthPx || requiredHeightWithElementPadding > effectiveAvailableHeightPx) {
                    val scaleCorrectionX = if (requiredWidthWithElementPadding > 0) effectiveAvailableWidthPx / requiredWidthWithElementPadding else 1f
                    val scaleCorrectionY = if (requiredHeightWithElementPadding > 0) effectiveAvailableHeightPx / requiredHeightWithElementPadding else 1f
                    val scaleCorrection = min(scaleCorrectionX, scaleCorrectionY)
                    scale *= scaleCorrection

                    val (correctedHexSizeDp, correctedSettlementSizeDp, correctedDrawableRoadLengthDp) = calculateSizes(scale)
                    hexSizeDp = correctedHexSizeDp
                    settlementSizeDp = correctedSettlementSizeDp
                    drawableRoadLengthDp = correctedDrawableRoadLengthDp
                }

                // --- Visual Bounds & Offset ---
                val finalElementPaddingNeededX = (LOGICAL_HEX_WIDTH * scale) / 2f // Use corrected scale
                val finalElementPaddingNeededY = (LOGICAL_HEX_HEIGHT * scale) / 2f // Use corrected scale

                val visualMinX = (minX * scale) - finalElementPaddingNeededX
                val visualMaxX = (maxX * scale) + finalElementPaddingNeededX
                val visualMinY = (minY * scale) - finalElementPaddingNeededY
                val visualMaxY = (maxY * scale) + finalElementPaddingNeededY

                val visualBoardWidthPx = visualMaxX - visualMinX
                val visualBoardHeightPx = visualMaxY - visualMinY

                val centeringOffsetX = (effectiveAvailableWidthPx - visualBoardWidthPx) / 2f
                val centeringOffsetY = (effectiveAvailableHeightPx - visualBoardHeightPx) / 2f

                val finalBoardOffsetX = paddingPx + centeringOffsetX - visualMinX
                val finalBoardOffsetY = paddingPx + centeringOffsetY - visualMinY

                // --- Update Layout Params ---
                layoutParams = BoardLayoutParams(
                    scale = scale,
                    boardOffsetX = finalBoardOffsetX,
                    boardOffsetY = finalBoardOffsetY,
                    hexSizeDp = hexSizeDp,
                    settlementSizeDp = settlementSizeDp,
                    drawableRoadLengthDp = drawableRoadLengthDp
                )

            } else { // End of calculation if error
                layoutParams = null
                if (tiles.isEmpty()) Log.w("CatanBoard", "Tile list is empty.")
                if (effectiveAvailableWidthPx <= 0) Log.w("CatanBoard", "Effective available width is zero or negative (check padding).")
                if (effectiveAvailableHeightPx <= 0) Log.w("CatanBoard", "Effective available height is zero or negative (check padding).")
            }
        }



        // Draw the elements if layout is calculated
        layoutParams?.let { params ->
            val density = LocalDensity.current

            // --- Draw Tiles ---
            val currentHexSizePx = with(density) { params.hexSizeDp.toPx() }
            tiles.forEach { tile ->
                val scaledX = tile.coordinates[0].toFloat() * params.scale
                val scaledY = tile.coordinates[1].toFloat() * params.scale
                val finalX = params.boardOffsetX + scaledX
                val finalY = params.boardOffsetY + scaledY
                val composableOffsetX = finalX - (currentHexSizePx / 2f)
                val composableOffsetY = finalY - (currentHexSizePx / 2f)

                HexagonTile(
                    modifier = Modifier.offset { IntOffset(composableOffsetX.roundToInt(), composableOffsetY.roundToInt()) },
                    tile = tile,
                    size = params.hexSizeDp
                )
            }

            // --- Draw Settlements ---
            val settlementSizePx = with(density) { params.settlementSizeDp.toPx() }
            settlementPositions.forEach { position ->
                val scaledX = position.coordinates[0].toFloat() * params.scale
                val scaledY = position.coordinates[1].toFloat() * params.scale
                val finalX = params.boardOffsetX + scaledX
                val finalY = params.boardOffsetY + scaledY
                val composableOffsetX = finalX - (settlementSizePx / 2f)
                val composableOffsetY = finalY - (settlementSizePx / 2f)

                SettlementComposable(
                    modifier = Modifier.offset { IntOffset(composableOffsetX.roundToInt(), composableOffsetY.roundToInt()) },
                    settlementPosition = position,
                    size = params.settlementSizeDp
                )
            }

            // --- Draw Roads ---
            val roadLengthPx = with(density) { params.drawableRoadLengthDp.toPx() }
            val roadThicknessDp = params.settlementSizeDp * 0.4f
            val roadThicknessPx = with(density) { roadThicknessDp.toPx() }

            roads.forEach { road ->
                val scaledX = road.coordinates[0].toFloat() * params.scale
                val scaledY = road.coordinates[1].toFloat() * params.scale
                val finalX = params.boardOffsetX + scaledX
                val finalY = params.boardOffsetY + scaledY
                val composableOffsetX = finalX - (roadLengthPx / 2f)
                val composableOffsetY = finalY - (roadThicknessPx / 2f)

                RoadComposable(
                    modifier = Modifier.offset { IntOffset(composableOffsetX.roundToInt(), composableOffsetY.roundToInt()) },
                    road = road,
                    length = params.drawableRoadLengthDp,
                    thickness = roadThicknessDp
                )
            }
        } ?: run {
            // Apply background to the loading state box
            Box(Modifier.fillMaxSize().background(boardBackgroundColor), contentAlignment = Alignment.Center) {
                Text("Calculating layout...", color = Color.White) // Make text visible
            }
        }
    }
}