package com.example.cataniaunited.ui.game

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.cataniaunited.ui.game_board.playerinfo.LivePlayerVictoryBar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameScreen(
    lobbyId: String,
    gameViewModel: GameViewModel = hiltViewModel(),
) {
    val gameBoardState by gameViewModel.gameBoardState.collectAsState()
    val isBuildMenuOpen by gameViewModel.isBuildMenuOpen.collectAsState()
    val application = LocalContext.current.applicationContext as MainApplication
    var showDicePopup by remember { mutableStateOf(false) }
    val diceResult by gameViewModel.diceResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        application.gameViewModel = gameViewModel
        if (gameBoardState == null) {
            gameViewModel.initializeBoardState(application.latestBoardJson)
        }
    }

    LaunchedEffect(snackbarHostState) {
        gameViewModel.errorFlow.collectLatest { message ->
            snackbarHostState.showSnackbar(message, withDismissAction = true)
        }
    }

    ShakeDetector {
        if (!showDicePopup) {
            showDicePopup = true
            gameViewModel.rollDice(lobbyId)
        }
    }

    Scaffold(
        containerColor = Color(0xff177fde), // FULL SCREEN BLUE
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xff177fde)) // also ensure blue if scaffold fails
        ) {
            // Top player bar, fixed height
            LivePlayerVictoryBar(
                viewModel = gameViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            // Board & buttons in Box taking rest of screen
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)
            ) {
                when (val board = gameBoardState) {
                    null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    else -> {
                        // Main game board
                        CatanBoard(
                            modifier = Modifier.fillMaxSize(),
                            tiles = board.tiles,
                            settlementPositions = board.settlementPositions,
                            roads = board.roads,
                            isBuildMode = isBuildMenuOpen,
                            playerId = gameViewModel.playerId,
                            onTileClicked = { gameViewModel.handleTileClick(it, lobbyId) },
                            onSettlementClicked = { gameViewModel.handleSettlementClick(it, lobbyId) },
                            onRoadClicked = { gameViewModel.handleRoadClick(it, lobbyId) }
                        )

                        // Build button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 32.dp, end = 16.dp)
                                .zIndex(2f)
                        ) {
                            BuildButton(
                                isOpen = isBuildMenuOpen,
                                onClick = { gameViewModel.setBuildMenuOpen(it) }
                            )
                        }

                        // Dice button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 32.dp, start = 8.dp)
                                .zIndex(2f)
                        ) {
                            RollDiceButton {
                                showDicePopup = true
                            }
                        }
                    }
                }

                // Dice popup
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
    }
}
