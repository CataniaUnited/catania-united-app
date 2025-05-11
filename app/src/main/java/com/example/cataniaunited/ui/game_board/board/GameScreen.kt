// app/src/main/java/com/example/cataniaunited/ui/game_board/board/GameScreen.kt
package com.example.cataniaunited.ui.game_board.board

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.game.GameViewModel

/**
 * Shows the board that the server sent after **START_GAME**.
 *
 * * Reads the board JSON that `MainApplication` cached in `latestBoardJson`
 *   after the START_GAME payload arrived.
 * * Feeds that JSON once into [GameViewModel.initializeBoardState].
 * * Delegates all click-events back to the ViewModel.
 */
@Composable
fun GameScreen(
    lobbyId: String,
    gameViewModel: GameViewModel = hiltViewModel(),      // scoped VM
) {
    /* --- 1. UI-state from the VM ---------------------------------------- */
    val boardState       by gameViewModel.gameBoardState.collectAsState()
    val app              = LocalContext.current.applicationContext as MainApplication
    val cachedBoard      = app.latestBoardJson                 // may be null

    /* --- 2. Initialise once (if needed) -------------------------------- */
    LaunchedEffect(Unit) {
        if (gameViewModel.gameBoardState.value == null) {
            gameViewModel.initializeBoardState(cachedBoard)
        }
    }

    /* --- 3. UI ---------------------------------------------------------- */
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val board = boardState) {
            null -> CircularProgressIndicator()

            else -> CatanBoard(
                modifier            = Modifier.fillMaxSize(),
                tiles               = board.tiles,
                settlementPositions = board.settlementPositions,
                roads               = board.roads,

                onTileClicked = { tile ->
                    Log.d("GameScreen", "Tile clicked: ${tile.id}")
                    gameViewModel.handleTileClick(tile, lobbyId)
                },
                onSettlementClicked = { pos ->
                    Log.d("GameScreen", "Settlement clicked: ${pos.id}")
                    gameViewModel.handleSettlementClick(pos, lobbyId)
                },
                onRoadClicked = { road ->
                    Log.d("GameScreen", "Road clicked: ${road.id}")
                    gameViewModel.handleRoadClick(road, lobbyId)
                }
            )
        }
    }
}
