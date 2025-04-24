package com.example.cataniaunited.ui.game_borad

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GameScreen(
    gameViewModel: GameViewModel = viewModel()
) {
    // Observe the StateFlow from the ViewModel
    val gameBoardState by gameViewModel.gameBoardState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Get the current value of the state
        when (val board = gameBoardState) {
            null -> {
                // Board data is not yet loaded or failed to load
                CircularProgressIndicator() // Show loading spinner

            }
            else -> {
                // Board data is available, pass the tiles to the CatanBoard composable
                CatanBoard(
                    modifier = Modifier.fillMaxSize(), // Let the board drawing fill the screen
                    tiles = board.tiles
                    // TODO: Pass settlements = board.settlementPositions later
                    // TODO: Pass roads = board.roads later
                )
            }
        }
    }
}