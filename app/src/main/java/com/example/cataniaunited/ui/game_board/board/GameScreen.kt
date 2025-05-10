package com.example.cataniaunited.ui.game_board.board

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ui.components.DevelopmentCardPopup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.example.cataniaunited.ui.components.DevelopmentCardRowPopup
import androidx.compose.ui.unit.dp






@Composable
fun GameScreen(
    lobbyId: String,
    gameViewModel: GameViewModel = hiltViewModel(),
) {
    val gameBoardState by gameViewModel.gameBoardState.collectAsState()
    val application = LocalContext.current.applicationContext as MainApplication // Get app instance
    var drawnCardType by remember { mutableStateOf<String?>(null) }
    var showCardPopup by remember { mutableStateOf(false) }


    // Trigger initial load when the screen enters composition if state is null
    LaunchedEffect(Unit) { // Run once when GameScreen enters composition
        if (gameViewModel.gameBoardState.value == null) {
            gameViewModel.initializeBoardState(application.latestBoardJson)
        }
    }

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
                        gameViewModel.handleTileClick(tile, lobbyId)
                    },
                    onSettlementClicked = { settlementPos ->
                        Log.d("GameScreen", "Settlement Clicked: ID=${settlementPos.id}")
                        gameViewModel.handleSettlementClick(settlementPos, lobbyId)
                    },
                    onRoadClicked = { road ->
                        Log.d("GameScreen", "Road Clicked: ID=${road.id}")
                        gameViewModel.handleRoadClick(road, lobbyId)
                    }
                )


                //Deck icon to show development cards
                Button(
                    onClick = { showCardPopup = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("🂠") // card deck icon
                }
            }
        }

        // Popup showing  development cards
        if (showCardPopup) {
            DevelopmentCardRowPopup(
                cards = gameViewModel.myDevelopmentCards,
                onDismiss = { showCardPopup = false }
            )
        }
    }

    // Existing card draw button
    if (drawnCardType != null) {
        DevelopmentCardPopup(
            cardType = drawnCardType!!,
            onDismiss = { drawnCardType = null }
        )
    }

    Button(onClick = {
        drawnCardType = "KNIGHT"
        gameViewModel.handleBuyDevCardClick(lobbyId)
    }) {
        Text("Buy Dev Card")
    }
}