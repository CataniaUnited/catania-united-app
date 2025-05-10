package com.example.cataniaunited.ui.game

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ui.dice.DiceRollerPopup
import com.example.cataniaunited.ui.dice.ShakeDetector
import com.example.cataniaunited.ui.game_board.board.CatanBoard
import kotlinx.coroutines.flow.collectLatest


@Composable
fun GameScreen(
    lobbyId: String,
    gameViewModel: GameViewModel = hiltViewModel(),
) {
    val gameBoardState by gameViewModel.gameBoardState.collectAsState()
    val isBuildMenuOpen by gameViewModel.isBuildMenuOpen.collectAsState()
    val application = LocalContext.current.applicationContext as MainApplication // Get app instance
    var showDicePopup by remember { mutableStateOf(false) }
    val diceResult by gameViewModel.diceResult.collectAsState()
    val playerResources by gameViewModel.playerResources.collectAsState()

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarHostState) {
        gameViewModel.errorFlow.collectLatest { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                withDismissAction = true
            )
        }
    }

    // Trigger initial load when the screen enters composition if state is null
    LaunchedEffect(Unit) { // Run once when GameScreen enters composition
        application.gameViewModel = gameViewModel
        if (gameViewModel.gameBoardState.value == null) {
            gameViewModel.initializeBoardState(application.latestBoardJson)
        }
    }

    ShakeDetector(onShake = {
        if (!showDicePopup) {
            showDicePopup = true
            gameViewModel.rollDice(lobbyId)
        }
    })

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data: SnackbarData ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
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
                        isBuildMode = isBuildMenuOpen,
                        playerId = gameViewModel.playerId,

                        // Add click handlers
                        onTileClicked = { tile ->
                            Log.d(
                                "GameScreen",
                                "Tile Clicked: ID=${tile.id}, Type=${tile.type}, Value=${tile.value}"
                            )
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
                            .align(Alignment.TopEnd)
                            .offset(y = 32.dp)
                            .zIndex(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        BuildButton(
                            isOpen = isBuildMenuOpen,
                            onClick = { isOpen -> gameViewModel.setBuildMenuOpen(isOpen) }
                        )
                    }

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


        // Player Resources Bar at the bottom
        if (gameBoardState != null) {
            PlayerResourcesBar(
                resources = playerResources
            )
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
}