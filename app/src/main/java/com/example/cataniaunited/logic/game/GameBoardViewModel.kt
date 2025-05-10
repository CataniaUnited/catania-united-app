package com.example.cataniaunited.logic.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.ws.provider.WebSocketErrorProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameBoardLogic: GameBoardLogic,
    private val gameDataHandler: GameDataHandler,
    private val sessionManager: PlayerSessionManager,
    private val errorProvider: WebSocketErrorProvider
) : ViewModel() {

    val playerId get() = sessionManager.getPlayerId()
    val gameBoardState: StateFlow<GameBoardModel?> = gameDataHandler.gameBoardState

    private val _errorChannel = Channel<String>(Channel.BUFFERED)
    val errorFlow = _errorChannel.receiveAsFlow()

    private val _isBuildMenuOpen = MutableStateFlow(false)
    val isBuildMenuOpen: StateFlow<Boolean> = _isBuildMenuOpen

    private val _diceResult = MutableStateFlow<Pair<Int, Int>?>(null)
    val diceResult: StateFlow<Pair<Int, Int>?> = _diceResult

    private val _victoryPoints = MutableStateFlow<Map<String, Int>>(emptyMap())
    val victoryPoints: StateFlow<Map<String, Int>> = _victoryPoints

    init {
        Log.d("GameViewModel", "ViewModel Initialized (Hilt).")
        // Don't load initial board automatically here

        viewModelScope.launch {
            errorProvider.errorFlow.collect { errorMessage ->
                Log.e("GameBoardViewModel", "Error Message received")
                _errorChannel.send(errorMessage)
            }
        }

        viewModelScope.launch {
            gameDataHandler.victoryPointsState.collect {
                _victoryPoints.value = it
            }
        }
    }

    // New function to be called externally (e.g., from the Composable's LaunchedEffect)
    fun initializeBoardState(initialJson: String?) {
        if (gameBoardState.value == null) { // Only load if not already loaded
            Log.i("GameViewModel", "Initializing board state.")
            if (initialJson != null) {
                loadGameBoardFromJson(initialJson)
                // Maybe clear application state here if needed via injected dependency?
            } else {
                Log.e("GameViewModel", "Initial board JSON was null during initialization!")
            }
        }
    }

    fun loadGameBoardFromJson(jsonString: String) {
        viewModelScope.launch {
            gameDataHandler.updateGameBoard(jsonString)
        }
    }

    // --- Placeholder Click Handlers ---

    fun handleTileClick(tile: Tile, lobbyId: String) {
        Log.d("GameViewModel", "handleTileClick: Tile ID=${tile.id}")
        // TODO: Implement logic for tile click (e.g., move robber phase)
        // 1) Check game state (is it robber phase?)
        // 2) Validate if the tile is a valid target
        // 3) call gameBoardLogic....
    }

    fun handleSettlementClick(settlementPosition: SettlementPosition, lobbyId: String) {
        Log.d(
            "GameViewModel",
            "handleSettlementClick: SettlementPosition ID=${settlementPosition.id}"
        )
        // TODO: Implement logic for placing/upgrading settlement DON'T FORGET UPGRADE XD
        // 1) Check game state (setup or not? your turn?)
        // 2) Check resources
        // 3) Validate placement rules (distance, road connection)
        // 4) Get lobbyId and PlayerId
        // 5) Call gameBoardLogic.placeSettlement(settlementPosition.id, lobbyId)
        gameBoardLogic.placeSettlement(settlementPosition.id, lobbyId)
    }

    fun handleRoadClick(road: Road, lobbyId: String) {
        Log.d("GameViewModel", "handleRoadClick: Road ID=${road.id}")
        // TODO: Implement logic for placing road
        // 1) Check game state (setup or not? your turn?)
        // 2) Check resources
        // 3) Validate placement rules (road connection, empty)
        // 4) Get lobbyId and PlayerId
        // 5) Call gameBoardLogic.placeRoad(road.id, lobbyId)
        gameBoardLogic.placeRoad(road.id, lobbyId)
    }

    fun setBuildMenuOpen(isOpen: Boolean) {
        Log.d("GameViewModel", "handleBuildMenuClick: isOpen=${isOpen}")
        _isBuildMenuOpen.value = isOpen
    }

    var isProcessingRoll = false
    fun rollDice(lobbyId: String) {
        if (isProcessingRoll) return

        isProcessingRoll = true
        Log.d("GameViewModel", "Initiating dice roll for lobby: $lobbyId")
        gameBoardLogic.rollDice(lobbyId)

        viewModelScope.launch {
            isProcessingRoll = false
        }
    }

    fun updateDiceResult(dice1: Int?, dice2: Int?) {
        viewModelScope.launch {
            if (dice1 != null && dice2 != null) {
                _diceResult.value = Pair(dice1, dice2)
            } else {
                _diceResult.value = null
            }
        }
    }
}

