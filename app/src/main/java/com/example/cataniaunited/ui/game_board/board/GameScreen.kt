package com.example.cataniaunited.ui.game_board.board

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cataniaunited.logic.game.GameViewModel

@Composable
fun GameScreen(
    gameViewModel: GameViewModel = viewModel(),
) {
    val gameBoardState by gameViewModel.gameBoardState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val board = gameBoardState) {
            null -> {
                CircularProgressIndicator()
            }
            else -> {
                CatanBoard(
                    modifier = Modifier.fillMaxSize(),
                    tiles = board.tiles,
                    settlementPositions = board.settlementPositions,
                    roads = board.roads,

                    // Add click handlers
                    onTileClicked = { tile ->
                        Log.d("GameScreen", "Tile Clicked: ID=${tile.id}, Type=${tile.type}, Value=${tile.value}")
                        gameViewModel.handleTileClick(tile)
                    },
                    onSettlementClicked = { settlementPos ->
                        Log.d("GameScreen", "Settlement Clicked: ID=${settlementPos.id}")
                        gameViewModel.handleSettlementClick(settlementPos)
                    },
                    onRoadClicked = { road ->
                        Log.d("GameScreen", "Road Clicked: ID=${road.id}")
                        gameViewModel.handleRoadClick(road)
                    }
                )
            }
        }
    }
}