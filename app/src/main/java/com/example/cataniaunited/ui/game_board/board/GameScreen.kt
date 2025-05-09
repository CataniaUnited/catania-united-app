package com.example.cataniaunited.ui.game_board.board

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.logic.game.PlayerInfo
import com.example.cataniaunited.ui.dice.DiceRollerPopup
import com.example.cataniaunited.ui.dice.ShakeDetector
import com.example.cataniaunited.ui.game.RollDiceButton
import com.example.cataniaunited.ui.game_board.playerinfo.PlayerVictoryBar
import com.example.cataniaunited.ui.theme.catanBlue


@Composable
fun GameScreen(
    lobbyId: String,
    gameViewModel: GameViewModel = hiltViewModel(),
) {
    val gameBoardState by gameViewModel.gameBoardState.collectAsState()
    val application = LocalContext.current.applicationContext as MainApplication // Get app instance
    var showDicePopup by remember { mutableStateOf(false) }
    val diceResult by gameViewModel.diceResult.collectAsState()

    // Trigger initial load when the screen enters composition if state is null
    LaunchedEffect(Unit) { // Run once when GameScreen enters composition
        if (gameViewModel.gameBoardState.value == null) {
            gameViewModel.initializeBoardState(application.latestBoardJson)
        }
    }

    LaunchedEffect(gameViewModel) {
        application.gameViewModel = gameViewModel
    }

    ShakeDetector(onShake = {
        if (!showDicePopup) {
            showDicePopup = true
            gameViewModel.rollDice(lobbyId)
        }
    })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(catanBlue),
        contentAlignment = Alignment.Center
    ) {
        when (val board = gameBoardState) {
            null -> {
                CircularProgressIndicator()
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ){
                    PlayerVictoryBar(
                        players = listOf(
                            PlayerInfo("1", "Mia", "#FF0000", 4),
                            PlayerInfo("2", "Nassir", "#0000FF", 10),
                            PlayerInfo("3", "Jean", "#D4AF37", 9),
                            PlayerInfo("4", "Candamir", "#800080", 0)
                        ),

                        currentPlayerId = application.getPlayerId(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(2f)
                            .padding(top = 12.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        CatanBoard(
                            modifier = Modifier.fillMaxSize(),
                            tiles = board.tiles,
                            settlementPositions = board.settlementPositions,
                            roads = board.roads,
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

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(y = 32.dp)
                                .zIndex(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            RollDiceButton {
                                showDicePopup = true
                            }
                        }
                    }
                }
            }
        }
    }
    if (showDicePopup) {
        DiceRollerPopup(
            onDiceRolled = { gameViewModel.rollDice(lobbyId) },
            onClose = {
                showDicePopup = false
                gameViewModel.updateDiceResult(null, null)
            },
            dice1Result = diceResult?.first,
            dice2Result = diceResult?.second
        )
    }
}


