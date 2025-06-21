package com.example.cataniaunited.ui.game

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoubleArrow
import com.example.cataniaunited.ui.theme.catanClay
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ui.components.DevelopmentCardPopup
import com.example.cataniaunited.ui.components.DevelopmentCardRowPopup
import com.example.cataniaunited.ui.dice.DiceRollerPopup
import com.example.cataniaunited.ui.dice.ShakeDetector
import com.example.cataniaunited.ui.game_board.board.CatanBoard
import com.example.cataniaunited.ui.game_board.playerinfo.LivePlayerVictoryBar
import com.example.cataniaunited.ui.game_end.GameWinScreen
import com.example.cataniaunited.ui.theme.catanBlue

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
    val gameWonState by application.gameWonState.collectAsState()
    val players by gameViewModel.players.collectAsState()
    val buildingCosts by gameViewModel.buildingCosts.collectAsState()
    val player: PlayerInfo? = players[gameViewModel.playerId]
    val drawnCardType by gameViewModel.drawnCardType.collectAsState()
    var showCardPopup by remember { mutableStateOf(false) }
    val buildingCostsText = buildString {
        buildingCosts?.let {
            appendLine("Settlement: " + it.settlement.entries.joinToString { "${it.value} ${it.key}" })
            appendLine("City: " + it.city.entries.joinToString { "${it.value} ${it.key}" })
            appendLine("Road: " + it.road.entries.joinToString { "${it.value} ${it.key}" })
            appendLine("Development Card: " + it.developmentCard.entries.joinToString { "${it.value} ${it.key}" })
        } ?: append("Loading building costs...")
    }

    LaunchedEffect(Unit) {
        application.gameViewModel = gameViewModel
        if (gameBoardState == null) {
            gameViewModel.initializeBoardState(application.latestBoardJson)
        }
        gameViewModel.requestBuildingCosts(lobbyId)
    }

    if (drawnCardType != null) {
        DevelopmentCardPopup(
            cardType = drawnCardType!!,
            onDismiss = { gameViewModel.clearDrawnCard() }
        )
    }

    if (showCardPopup) {
        DevelopmentCardRowPopup(
            cards = gameViewModel.myDevelopmentCards,
            onDismiss = { showCardPopup = false }
        )
    }

    if(player?.isActivePlayer == true && player.canRollDice == true){
        ShakeDetector {
            if (!showDicePopup) {
                showDicePopup = true
                gameViewModel.rollDice(lobbyId)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xff177fde),
            bottomBar = {
                if (gameBoardState != null) {
                    PlayerResourcesBar(
                        modifier = Modifier.fillMaxWidth(),
                        resources = playerResources
                    )
                }
            },
            topBar = {
                LivePlayerVictoryBar()
            },
            floatingActionButton = {
                if (player?.isActivePlayer == true) {
                    if (player.canRollDice == true) {
                        //Roll dice action
                        FloatingActionButton(
                            onClick = {
                                showDicePopup = true
                                gameViewModel.rollDice(lobbyId)
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Image(
                                painter = painterResource(R.drawable.dice_6),
                                contentDescription = "Roll dice",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        FloatingActionButton(
                            onClick = {
                                gameViewModel.handleEndTurnClick(lobbyId)
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DoubleArrow,
                                contentDescription = "End turn"
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(catanBlue)
            ) {
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
                                ports = board.ports,
                                isBuildMode = isBuildMenuOpen,
                                playerId = gameViewModel.playerId,
                                onTileClicked = { tile ->
                                    Log.d(
                                        "GameScreen",
                                        "Tile Clicked: ID=${tile.id}, Type=${tile.type}, Value=${tile.value}"
                                    )
                                    gameViewModel.handleTileClick(tile, lobbyId)
                                },
                                onSettlementClicked = { (settlementPos, isUpgrade) ->
                                    Log.d(
                                        "GameScreen",
                                        "Settlement Clicked: ID=${settlementPos.id}"
                                    )
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

                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 32.dp, end = 16.dp)
                                    .zIndex(2f)
                            ) {
                                if (player?.isActivePlayer == true) {
                                    BuildButton(
                                        enabled = player.canRollDice == false,
                                        isOpen = isBuildMenuOpen,
                                        onClick = { isOpen -> gameViewModel.setBuildMenuOpen(isOpen) }
                                    )
                                    Button(onClick = {
                                        gameViewModel.handleBuyDevCardClick(lobbyId)
                                    }) {
                                        Text("Buy Dev Card")
                                    }
                                }

                            }
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .background(catanClay, shape = RoundedCornerShape(12.dp)) // Brown
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = buildingCostsText,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    if (showDicePopup) {
                        DiceRollerPopup(
                            onClose = {
                                showDicePopup = false
                                gameViewModel.updateDiceResult(null, null)
                            },
                            dice1Result = diceResult?.first,
                            dice2Result = diceResult?.second
                        )
                    }

                    Text(
                        text = "Lobby ID: $lobbyId",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    )
                }
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

