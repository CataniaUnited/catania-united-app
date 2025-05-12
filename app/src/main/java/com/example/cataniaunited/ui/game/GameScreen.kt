package com.example.cataniaunited.ui.game

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ui.dice.DiceRollerPopup
import com.example.cataniaunited.ui.dice.ShakeDetector
import com.example.cataniaunited.ui.game_board.board.CatanBoard
import com.example.cataniaunited.ui.game_board.playerinfo.LivePlayerVictoryBar
import com.example.cataniaunited.ui.game_end.GameWinScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameScreen(
    lobbyId: String,
    navController: NavController,
    gameViewModel: GameViewModel = hiltViewModel(),
) {
    val gameBoardState by gameViewModel.gameBoardState.collectAsState()
    val isBuildMenuOpen by gameViewModel.isBuildMenuOpen.collectAsState()
    val application = LocalContext.current.applicationContext as MainApplication
    var showDicePopup by remember { mutableStateOf(false) }
    val diceResult by gameViewModel.diceResult.collectAsState()
    val playerResources by gameViewModel.playerResources.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val gameWonState by application.gameWonState.collectAsState()

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

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xff177fde),
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                }
            },
            bottomBar = {
                if (gameBoardState != null) {
                    PlayerResourcesBar(
                        modifier = Modifier.fillMaxWidth(),
                        resources = playerResources
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xff177fde))
            ) {
                LivePlayerVictoryBar(
                    viewModel = gameViewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (val board = gameBoardState) {
                        null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        else -> {
                            CatanBoard(
                                modifier = Modifier.fillMaxSize(),
                                tiles = board.tiles,
                                settlementPositions = board.settlementPositions,
                                roads = board.roads,
                                isBuildMode = isBuildMenuOpen,
                                playerId = gameViewModel.playerId,
                                onTileClicked = { tile ->
                                    Log.d("GameScreen", "Tile Clicked: ID=${tile.id}, Type=${tile.type}, Value=${tile.value}")
                                    gameViewModel.handleTileClick(tile, lobbyId)
                                },
                                onSettlementClicked = { (settlementPos, isUpgrade) ->
                                    Log.d("GameScreen", "Settlement Clicked: ID=${settlementPos.id}")
                                    gameViewModel.handleSettlementClick(
                                        settlementPos,
                                        isUpgrade,
                                        lobbyId
                                    )
                                },
                                onRoadClicked = { road ->
                                    Log.d("GameScreen", "Road Clicked: ID=${road.id}")
                                    gameViewModel.handleRoadClick(road, lobbyId)
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 32.dp, end = 16.dp)
                                    .zIndex(2f)
                            ) {
                                BuildButton(
                                    isOpen = isBuildMenuOpen,
                                    onClick = { isOpen -> gameViewModel.setBuildMenuOpen(isOpen) }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 32.dp, start = 8.dp)
                                    .zIndex(2f)
                            ) {
                                RollDiceButton {
                                    showDicePopup = true
                                    gameViewModel.rollDice(lobbyId)
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
                                gameViewModel.rollDice(lobbyId)
                            }
                        }
                    }

                    Text(
                        text = "Lobby ID: $lobbyId",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = gameWonState != null,
                enter = fadeIn() + scaleIn(initialScale = 0.9f),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    gameWonState?.let { (winner, leaderboard) ->
                        GameWinScreen(
                            winner = winner,
                            leaderboard = leaderboard,
                            onReturnToMenu = {
                                application.clearGameData()
                                application.clearLobbyData()
                                navController.navigate("starting") {
                                    popUpTo("starting") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
