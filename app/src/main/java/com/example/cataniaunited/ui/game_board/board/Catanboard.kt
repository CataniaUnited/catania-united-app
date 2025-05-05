package com.example.cataniaunited.ui.game_board.board

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.min
import kotlin.math.roundToInt


private const val LOGICAL_HEX_RADIUS = 10.0f
private const val LOGICAL_HEX_DIAMETER = LOGICAL_HEX_RADIUS * 2.0f

// Base layout parameters calculated once to fit the initial view
private data class BoardBaseLayoutParams(
    val minX: Float = 0.0f,
    val maxX: Float = 0.0f,
    val minY: Float = 0.0f,
    val maxY: Float = 0.0f,
    val initialScale: Float = 1.0f,
    val initialBoardOffsetX: Float = 0f,
    val initialBoardOffsetY: Float = 0f,
    val initialHexSizeDp: Dp = 60.dp,
    val initialSettlementSizeDp: Dp = 15.dp,
    val initialDrawableRoadLengthDp: Dp = 40.dp,
)

// Helper to convert Px to Dp within LaunchedEffect/Density scope
private fun Float.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }

@Composable
fun CatanBoard(
    modifier: Modifier = Modifier,
    tiles: List<Tile>,
    settlementPositions: List<SettlementPosition>,
    roads: List<Road>,
    outerMarginDp: Dp = 16.dp,
    boardBackgroundColor: Color = Color(0xff177fde),
    onTileClicked: (Tile) -> Unit = {},
    onSettlementClicked: (SettlementPosition) -> Unit = {},
    onRoadClicked: (Road) -> Unit = {}
) {
    // Check if essential data is present
    if (tiles.isEmpty() || settlementPositions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().background(boardBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text("Insufficient board data", color = Color.White)
        }
        return
    }

    // State for zoom and pan from gestures
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // State to hold the calculated base layout parameters
    var baseLayoutParams by remember { mutableStateOf<BoardBaseLayoutParams?>(null) }

    val density = LocalDensity.current

    // --- Base Layout Calculation ---
    // This effect calculates the initial fit and centering ONCE or when inputs change. Its not dependant on the gestures
    LaunchedEffect(tiles, settlementPositions, outerMarginDp, density) {
        baseLayoutParams = null
    }

    BoxWithConstraints(
        modifier = modifier
            .background(boardBackgroundColor)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    val newScale = (scale * zoom).coerceIn(0.5f, 3f) // Limit zoom levels

                    // Calculate the offset adjustment needed to keep the centroid stable
                    offset = (offset + centroid - (centroid / oldScale) * newScale) + pan

                    scale = newScale

                }
            }
    ) {
        // Recalculate base layout if it's null (first composition or input change)
        if (baseLayoutParams == null) {
            val availableWidthPx = constraints.maxWidth.toFloat()
            val availableHeightPx = constraints.maxHeight.toFloat()

            if (tiles.isNotEmpty() && settlementPositions.isNotEmpty() && availableWidthPx > 0 && availableHeightPx > 0) {
                // --- Calculate Effective Available Space (minus outer margin) ---
                val outerMarginPx = with(density) { outerMarginDp.toPx() }
                val effectiveAvailableWidthPx = (availableWidthPx - 2 * outerMarginPx).coerceAtLeast(0f)
                val effectiveAvailableHeightPx = (availableHeightPx - 2 * outerMarginPx).coerceAtLeast(0f)

                if (effectiveAvailableWidthPx > 0 && effectiveAvailableHeightPx > 0) {
                    // --- Calculate Bounds based on ALL relevant elements ---
                    val allCoordsX = tiles.map { it.coordinates[0].toFloat() } +
                            settlementPositions.map { it.coordinates[0].toFloat() }
                    val allCoordsY = tiles.map { it.coordinates[1].toFloat() } +
                            settlementPositions.map { it.coordinates[1].toFloat() }

                    val minX = allCoordsX.minOrNull() ?: 0f
                    val maxX = allCoordsX.maxOrNull() ?: 0f
                    val minY = allCoordsY.minOrNull() ?: 0f
                    val maxY = allCoordsY.maxOrNull() ?: 0f

                    val logicalWidthCenters = (maxX - minX).coerceAtLeast(0f)
                    val logicalHeightCenters = (maxY - minY).coerceAtLeast(0f)

                    if (logicalWidthCenters > 0 || logicalHeightCenters > 0) {
                        // --- Calculate Initial Scale ---
                        val totalLogicalWidth = (logicalWidthCenters + LOGICAL_HEX_DIAMETER).coerceAtLeast(0.01f)
                        val totalLogicalHeight = (logicalHeightCenters + LOGICAL_HEX_DIAMETER).coerceAtLeast(0.01f)
                        val scaleX = effectiveAvailableWidthPx / totalLogicalWidth
                        val scaleY = effectiveAvailableHeightPx / totalLogicalHeight
                        val initialFitScale = min(scaleX, scaleY).coerceAtLeast(0.01f)

                        // --- Calculate Component Sizes at Initial Scale ---
                        val initialScaledHexRadiusPx = LOGICAL_HEX_RADIUS * initialFitScale
                        val initialHexSizeDp = (LOGICAL_HEX_DIAMETER * initialFitScale).toDp(density)
                        val initialSettlementSizeDp = initialHexSizeDp * 0.25f
                        val initialSettlementDiameterPx = with(density) { initialSettlementSizeDp.toPx() }
                        val initialCenterToCenterRoadLengthPx = LOGICAL_HEX_RADIUS * initialFitScale
                        val initialDrawableRoadLengthPx = (initialCenterToCenterRoadLengthPx - initialSettlementDiameterPx).coerceAtLeast(1f)
                        val initialDrawableRoadLengthDp = initialDrawableRoadLengthPx.toDp(density)

                        // --- Calculate Initial Offset for Centering ---
                        val initialInternalPaddingNeededPx = initialScaledHexRadiusPx
                        val visualMinX = (minX * initialFitScale) - initialInternalPaddingNeededPx
                        val visualMinY = (minY * initialFitScale) - initialInternalPaddingNeededPx
                        val visualMaxX = (maxX * initialFitScale) + initialInternalPaddingNeededPx
                        val visualMaxY = (maxY * initialFitScale) + initialInternalPaddingNeededPx
                        val visualBoardWidthPx = visualMaxX - visualMinX
                        val visualBoardHeightPx = visualMaxY - visualMinY
                        val centeringOffsetX = (effectiveAvailableWidthPx - visualBoardWidthPx) / 2f
                        val centeringOffsetY = (effectiveAvailableHeightPx - visualBoardHeightPx) / 2f
                        val finalInitialBoardOffsetX = centeringOffsetX - visualMinX + outerMarginPx
                        val finalInitialBoardOffsetY = centeringOffsetY - visualMinY + outerMarginPx

                        // --- Store Base Layout Params ---
                        baseLayoutParams = BoardBaseLayoutParams(
                            minX = minX, maxX = maxX, minY = minY, maxY = maxY, // Store bounds
                            initialScale = initialFitScale,
                            initialBoardOffsetX = finalInitialBoardOffsetX,
                            initialBoardOffsetY = finalInitialBoardOffsetY,
                            initialHexSizeDp = initialHexSizeDp,
                            initialSettlementSizeDp = initialSettlementSizeDp,
                            initialDrawableRoadLengthDp = initialDrawableRoadLengthDp,
                        )
                        Log.d("CatanBoardLayout", "Base layout calculated: InitialScale=${baseLayoutParams?.initialScale}")
                    }
                }
            }
        }

        // --- Drawing ---
        // Draw elements only if base layout is calculated
        baseLayoutParams?.let { baseParams ->
            // Apply zoom/pan transformation using graphicsLayer to a Box containing all elements
            Box(
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                // Draw Tiles
                val currentHexSizePx = with(density) { baseParams.initialHexSizeDp.toPx() }
                tiles.forEach { tile ->
                    val scaledX = tile.coordinates[0].toFloat() * baseParams.initialScale
                    val scaledY = tile.coordinates[1].toFloat() * baseParams.initialScale
                    val finalX = baseParams.initialBoardOffsetX + scaledX
                    val finalY = baseParams.initialBoardOffsetY + scaledY
                    val composableOffsetX = finalX - (currentHexSizePx / 2f)
                    val composableOffsetY = finalY - (currentHexSizePx / 2f)

                    HexagonTile(
                        modifier = Modifier.offset { IntOffset(composableOffsetX.roundToInt(), composableOffsetY.roundToInt()) },
                        tile = tile,
                        size = baseParams.initialHexSizeDp, // Use initial size
                        onTileClick = onTileClicked // Pass down the handler
                    )
                }

                // Draw Settlements
                val settlementSizePx = with(density) { baseParams.initialSettlementSizeDp.toPx() }
                settlementPositions.forEach { position ->
                    val scaledX = position.coordinates[0].toFloat() * baseParams.initialScale
                    val scaledY = position.coordinates[1].toFloat() * baseParams.initialScale
                    val finalX = baseParams.initialBoardOffsetX + scaledX
                    val finalY = baseParams.initialBoardOffsetY + scaledY
                    val composableOffsetX = finalX - (settlementSizePx / 2f)
                    val composableOffsetY = finalY - (settlementSizePx / 2f)

                    SettlementComposable(
                        modifier = Modifier.offset { IntOffset(composableOffsetX.roundToInt(), composableOffsetY.roundToInt()) },
                        settlementPosition = position,
                        size = baseParams.initialSettlementSizeDp, // Use initial size
                        onSettlementClick = onSettlementClicked // Pass down the handler
                    )
                }

                // Draw Roads
                val roadLengthPx = with(density) { baseParams.initialDrawableRoadLengthDp.toPx() }
                val roadThicknessDp = baseParams.initialSettlementSizeDp * 0.4f // Relative to initial size
                val roadThicknessPx = with(density) { roadThicknessDp.toPx() }

                roads.forEach { road ->
                    val scaledX = road.coordinates[0].toFloat() * baseParams.initialScale
                    val scaledY = road.coordinates[1].toFloat() * baseParams.initialScale
                    val finalX = baseParams.initialBoardOffsetX + scaledX
                    val finalY = baseParams.initialBoardOffsetY + scaledY
                    val composableOffsetX = finalX - (roadLengthPx / 2f)
                    val composableOffsetY = finalY - (roadThicknessPx / 2f)

                    RoadComposable(
                        modifier = Modifier.offset { IntOffset(composableOffsetX.roundToInt(), composableOffsetY.roundToInt()) },
                        road = road,
                        length = baseParams.initialDrawableRoadLengthDp, // Use initial size
                        thickness = roadThicknessDp,
                        onRoadClick = onRoadClicked // Pass down the handler
                    )
                }
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Calculating layout...", color = Color.White)
            }
        }
    }
}