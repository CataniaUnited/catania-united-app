package com.example.cataniaunited.ui.game_board.board

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.Port
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.Robber
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.ui.game_board.port.PortComposable
import com.example.cataniaunited.ui.game_board.road.RoadComposable
import com.example.cataniaunited.ui.game_board.robber.RobberComposable
import com.example.cataniaunited.ui.game_board.settlementPosition.SettlementComposable
import com.example.cataniaunited.ui.game_board.tile.HexagonTile
import com.example.cataniaunited.ui.theme.catanBlue
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
    val initialPortIconSizeDp: Dp = 30.dp
)

// Helper to convert Px to Dp within LaunchedEffect/Density scope
private fun Float.toDp(density: Density): Dp = with(density) { this@toDp.toDp() }

@Composable
fun CatanBoard(
    modifier: Modifier = Modifier,
    tiles: List<Tile>,
    settlementPositions: List<SettlementPosition>,
    roads: List<Road>,
    ports: List<Port>,
    robberTile: Int?,
    outerMarginDp: Dp = 16.dp,
    boardBackgroundColor: Color = catanBlue,
    isBuildMode: Boolean,
    playerId: String,
    onTileClicked: (Tile) -> Unit = {},
    onSettlementClicked: (Pair<SettlementPosition, Boolean>) -> Unit = {},
    onRoadClicked: (Road) -> Unit = {},
    onRobberClick: (Robber) -> Unit = {}
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
    LaunchedEffect(tiles, settlementPositions, ports, outerMarginDp, density) {
        baseLayoutParams = null
    }

    BoxWithConstraints(
        modifier = modifier
            .background(boardBackgroundColor)
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    val newScale = (scale * zoom).coerceIn(0.5f, 5f) // Limit zoom levels

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
                            settlementPositions.map { it.coordinates[0].toFloat() } +
                            ports.map { it.portVisuals.portTransform.x.toFloat() } +
                            ports.flatMap { listOf(it.portVisuals.buildingSite1Position[0].toFloat(), it.portVisuals.buildingSite2Position[0].toFloat()) } // Add port-connected settlement X

                    val allCoordsY = tiles.map { it.coordinates[1].toFloat() } +
                            settlementPositions.map { it.coordinates[1].toFloat() } +
                            ports.map { it.portVisuals.portTransform.y.toFloat() } +
                            ports.flatMap { listOf(it.portVisuals.buildingSite1Position[1].toFloat(), it.portVisuals.buildingSite2Position[1].toFloat()) } // Add port-connected settlement Y

                    val minX = allCoordsX.minOrNull() ?: 0f
                    val maxX = allCoordsX.maxOrNull() ?: 0f
                    val minY = allCoordsY.minOrNull() ?: 0f
                    val maxY = allCoordsY.maxOrNull() ?: 0f

                    val logicalWidthCenters = (maxX - minX).coerceAtLeast(0f)
                    val logicalHeightCenters = (maxY - minY).coerceAtLeast(0f)

                    if (logicalWidthCenters > 0 || logicalHeightCenters > 0) {
                        // Heuristic: Estimate total logical width/height by adding a bit more padding
                        // for elements like ports that might extend beyond tile centers
                        val paddingFactor = LOGICAL_HEX_DIAMETER * 1.5f // Increased padding
                        val totalLogicalWidth = (logicalWidthCenters + paddingFactor).coerceAtLeast(0.01f)
                        val totalLogicalHeight = (logicalHeightCenters + paddingFactor).coerceAtLeast(0.01f)

                        val scaleX = effectiveAvailableWidthPx / totalLogicalWidth
                        val scaleY = effectiveAvailableHeightPx / totalLogicalHeight
                        val initialFitScale = min(scaleX, scaleY).coerceAtLeast(0.01f)

                        val initialScaledHexRadiusPx = LOGICAL_HEX_RADIUS * initialFitScale
                        val initialHexSizeDp = (LOGICAL_HEX_DIAMETER * initialFitScale).toDp(density)
                        val initialSettlementSizeDp = initialHexSizeDp * 0.25f
                        val initialSettlementDiameterPx = with(density) { initialSettlementSizeDp.toPx() }
                        val initialCenterToCenterRoadLengthPx = LOGICAL_HEX_RADIUS * initialFitScale
                        val initialDrawableRoadLengthPx = (initialCenterToCenterRoadLengthPx - initialSettlementDiameterPx).coerceAtLeast(1f)
                        val initialDrawableRoadLengthDp = initialDrawableRoadLengthPx.toDp(density)
                        val initialPortIconSizeDp = initialHexSizeDp * 0.4f // Port icon size relative to hex

                        val visualMinX = (minX * initialFitScale) - initialScaledHexRadiusPx - (with(density){initialPortIconSizeDp.toPx()}/2)
                        val visualMinY = (minY * initialFitScale) - initialScaledHexRadiusPx - (with(density){initialPortIconSizeDp.toPx()}/2)
                        val visualMaxX = (maxX * initialFitScale) + initialScaledHexRadiusPx + (with(density){initialPortIconSizeDp.toPx()}/2)
                        val visualMaxY = (maxY * initialFitScale) + initialScaledHexRadiusPx + (with(density){initialPortIconSizeDp.toPx()}/2)

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
                            initialPortIconSizeDp = initialPortIconSizeDp // <-- STORED
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
                        hasRobber = robberTile != null && robberTile == tile.id,
                        onTileClick = onTileClicked // Pass down the handler
                    )

                    if (robberTile != null && robberTile == tile.id) {
                        RobberComposable(modifier = Modifier.offset { IntOffset(composableOffsetX.roundToInt(), composableOffsetY.roundToInt()) },
                            size = baseParams.initialHexSizeDp,
                            onRobberClick = onRobberClick)
                    }
                }

                //Draw robber
                val robberSizePx = with(density) { baseParams.initialHexSizeDp.toPx() }
                robberTile.f


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
                        isClickable = isBuildMode,
                        playerId = playerId,
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
                        isClickable = isBuildMode,
                        playerId = playerId,
                        onRoadClick = onRoadClicked
                    )
                }

                // Draw Ports and Bridges
                val portIconSizePx = with(density) { baseParams.initialPortIconSizeDp.toPx() }
                val bridgeStrokeWidthPx = with(density) { 2.dp.toPx() } // Thickness of bridge lines

                Canvas(modifier = Modifier.matchParentSize()) { // Canvas for drawing bridge lines
                    ports.forEach { port ->
                        val pVisuals = port.portVisuals
                        val pTransform = pVisuals.portTransform

                        // Port Icon's center coordinates (scaled and offset)
                        val portCenterX = baseParams.initialBoardOffsetX + (pTransform.x.toFloat() * baseParams.initialScale)
                        val portCenterY = baseParams.initialBoardOffsetY + (pTransform.y.toFloat() * baseParams.initialScale)

                        // Settlement 1's center coordinates (scaled and offset)
                        val s1X = baseParams.initialBoardOffsetX + (pVisuals.buildingSite1Position[0].toFloat() * baseParams.initialScale)
                        val s1Y = baseParams.initialBoardOffsetY + (pVisuals.buildingSite1Position[1].toFloat() * baseParams.initialScale)

                        // Settlement 2's center coordinates (scaled and offset)
                        val s2X = baseParams.initialBoardOffsetX + (pVisuals.buildingSite2Position[0].toFloat() * baseParams.initialScale)
                        val s2Y = baseParams.initialBoardOffsetY + (pVisuals.buildingSite2Position[1].toFloat() * baseParams.initialScale)

                        // Draw bridge line to settlement 1
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(portCenterX, portCenterY),
                            end = Offset(s1X, s1Y),
                            strokeWidth = bridgeStrokeWidthPx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) // Optional: dashed line
                        )

                        // Draw bridge line to settlement 2
                        drawLine(
                            color = Color.DarkGray,
                            start = Offset(portCenterX, portCenterY),
                            end = Offset(s2X, s2Y),
                            strokeWidth = bridgeStrokeWidthPx,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) // Optional: dashed line
                        )
                    }
                }

                // Draw Port Icons (on top of lines)
                ports.forEach { port ->
                    val pVisuals = port.portVisuals
                    val pTransform = pVisuals.portTransform

                    val portCenterX = baseParams.initialBoardOffsetX + (pTransform.x.toFloat() * baseParams.initialScale)
                    val portCenterY = baseParams.initialBoardOffsetY + (pTransform.y.toFloat() * baseParams.initialScale)

                    val composableOffsetX = portCenterX - (portIconSizePx / 2f)
                    val composableOffsetY = portCenterY - (portIconSizePx / 2f)

                    PortComposable(
                        modifier = Modifier.offset { IntOffset(composableOffsetX.roundToInt(), composableOffsetY.roundToInt()) },
                        port = port,
                        size = baseParams.initialPortIconSizeDp
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