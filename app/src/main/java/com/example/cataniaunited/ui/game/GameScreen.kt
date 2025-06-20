package com.example.cataniaunited.ui.game

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ui.dice.DiceRollerPopup
import com.example.cataniaunited.ui.dice.ShakeDetector
import com.example.cataniaunited.ui.game_board.board.CatanBoard
import com.example.cataniaunited.ui.game_board.playerinfo.LivePlayerVictoryBar
import com.example.cataniaunited.ui.game_end.GameWinScreen
import com.example.cataniaunited.ui.theme.catanBlue
import com.example.cataniaunited.ui.trade.TradeMenuPopup
import kotlin.math.roundToInt

@Composable
fun GameScreen(
    lobbyId: String,
    navController: NavController,
    gameViewModel: GameViewModel = hiltViewModel(),
) {
    val gameBoardState by gameViewModel.gameBoardState.collectAsState()
    val isBuildMenuOpen by gameViewModel.isBuildMenuOpen.collectAsState()
    val isTradeMenuOpen by gameViewModel.isTradeMenuOpen.collectAsState()
    val tradeOffer by gameViewModel.tradeOffer.collectAsState()
    val application = LocalContext.current.applicationContext as MainApplication
    val showDicePopup by gameViewModel.showDicePopup.collectAsState()
    val diceState by gameViewModel.diceState.collectAsState()
    val playerResources by gameViewModel.playerResources.collectAsState()
    val gameWonState by application.gameWonState.collectAsState()
    val players by gameViewModel.players.collectAsState()
    val player: PlayerInfo? = players[gameViewModel.playerId]

    val selectedPlayer = remember { mutableStateOf<PlayerInfo?>(null) }
    val selectedPlayerIndex = remember { mutableStateOf<Int?>(null) }
    val selectedPlayerOffsetX = remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val popupOffsetX = with(density) { selectedPlayerOffsetX.floatValue.toDp().roundToPx() }
    val popupOffsetY = with(density) { 3.dp.toPx().roundToInt() }

    LaunchedEffect(Unit) {
        application.gameViewModel = gameViewModel
        if (gameBoardState == null) {
            gameViewModel.initializeBoardState(application.latestBoardJson)
        }
    }

    if (player?.isActivePlayer == true && player.isSetupRound == false && player.canRollDice == true) {
        ShakeDetector {
            if (!showDicePopup) {
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
                LivePlayerVictoryBar(
                    selectedPlayerId = selectedPlayer.value?.id,
                    onPlayerClicked = { playerInfo, index ->
                        selectedPlayer.value =
                            if (selectedPlayer.value?.id == playerInfo.id) null else playerInfo
                        selectedPlayerIndex.value =
                            if (selectedPlayer.value?.id == playerInfo.id) index else null
                    },
                    onPlayerOffsetChanged = { offsetX ->
                        selectedPlayerOffsetX.floatValue = offsetX
                    }
                )
            },
            floatingActionButton = {
                if (player?.isActivePlayer == true) {
                    if (player.isSetupRound == false && player.canRollDice == true) {
                        FloatingActionButton(
                            onClick = { gameViewModel.rollDice(lobbyId) },
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
                            onClick = { gameViewModel.handleEndTurnClick(lobbyId) },
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
                                    Log.d("GameScreen", "Tile Clicked: ${tile.id}")
                                    gameViewModel.handleTileClick(tile, lobbyId)
                                },
                                onSettlementClicked = { (settlementPos, isUpgrade) ->
                                    Log.d("GameScreen", "Settlement Clicked: ${settlementPos.id}")
                                    gameViewModel.handleSettlementClick(
                                        settlementPos,
                                        isUpgrade,
                                        lobbyId
                                    )
                                },
                                onRoadClicked = { road ->
                                    Log.d("GameScreen", "Road Clicked: ${road.id}")
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
                                        enabled = player.canRollDice == false || player.isSetupRound == true,
                                        isOpen = isBuildMenuOpen,
                                        onClick = { isOpen -> gameViewModel.setBuildMenuOpen(isOpen) }
                                    )
                                    TradeButton(
                                        enabled = player.canRollDice == false,
                                        onClick = { gameViewModel.setTradeMenuOpen(true) }
                                    )
                                }
                            }

                            if (selectedPlayer.value != null && selectedPlayerIndex.value != null) {
                                Box(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .zIndex(10f)
                                        .offset { IntOffset(x = popupOffsetX, y = popupOffsetY) }
                                        .align(Alignment.TopStart)
                                ) {
                                    PlayerResourcePopup(
                                        resources = selectedPlayer.value!!.resources,
                                        onCheatAttempt = { tileType ->
                                            gameViewModel.onCheatAttempt(tileType, lobbyId)
                                        }
                                    )

                                }
                            }
                        }
                    }

                    if (diceState != null) {
                        DiceRollerPopup(
                            viewModel = gameViewModel,
                            onClose = { gameViewModel.resetDiceState() }
                        )
                    }

                    if (isTradeMenuOpen) {
                        TradeMenuPopup(
                            onDismiss = { gameViewModel.setTradeMenuOpen(false) },
                            tradeOffer = tradeOffer,
                            onUpdateOffer = { resource, delta ->
                                gameViewModel.updateOfferedResource(resource, delta)
                            },
                            onUpdateTarget = { resource, delta ->
                                gameViewModel.updateTargetResource(resource, delta)
                            },
                            onSubmit = { gameViewModel.submitBankTrade(lobbyId) }
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
