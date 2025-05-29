package com.example.cataniaunited.logic.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.ws.provider.WebSocketErrorProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _playerResources = MutableStateFlow<Map<TileType, Int>>(emptyMap())
    val playerResources: StateFlow<Map<TileType, Int>> = _playerResources.asStateFlow()

    private val _players = MutableStateFlow<Map<String, PlayerInfo>>(emptyMap())
    val players: StateFlow<Map<String, PlayerInfo>> = _players.asStateFlow()

    init {
        Log.d("GameViewModel", "ViewModel Initialized (Hilt).")

        val initialResources = TileType.entries
            .filter { it != TileType.WASTE }
            .associateWith { 0 }
        _playerResources.value = initialResources

        viewModelScope.launch {
            errorProvider.errorFlow.collect { errorMessage ->
                Log.e("GameBoardViewModel", "Error Message received")
                _errorChannel.send(errorMessage)
            }
            gameDataHandler.victoryPointsState.collect {
                _victoryPoints.value = it
            }

            gameDataHandler.playersState.collect {
                _players.value = it
            }
        }
    }

    fun initializeBoardState(initialJson: String?) {
        if (gameBoardState.value == null) {
            Log.i("GameViewModel", "Initializing board state.")
            if (initialJson != null) {
                loadGameBoardFromJson(initialJson)
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

    fun updatePlayerResources(newResources: Map<TileType, Int>) {
        Log.d("GameViewModel", "Updating player resources: $newResources")
        _playerResources.value = newResources
    }

    fun handleTileClick(tile: Tile, lobbyId: String) {
        Log.d("GameViewModel", "handleTileClick: Tile ID=${tile.id}")
        // TODO: Implement logic for tile click (e.g., move robber phase)
    }

    fun handleSettlementClick(settlementPosition: SettlementPosition, isUpgrade: Boolean, lobbyId: String) {
        Log.d(
            "GameViewModel",
            "handleSettlementClick: SettlementPosition ID=${settlementPosition.id}"
        )
        if(isUpgrade){
            gameBoardLogic.upgradeSettlement(settlementPosition.id, lobbyId)
        }else{
            gameBoardLogic.placeSettlement(settlementPosition.id, lobbyId)
        }

    }

    fun handleRoadClick(road: Road, lobbyId: String) {
        Log.d("GameViewModel", "handleRoadClick: Road ID=${road.id}")

        val pid = playerId
        gameBoardLogic.setActivePlayer(pid, lobbyId)

        gameBoardLogic.placeRoad(road.id, lobbyId)
    }

    fun setBuildMenuOpen(isOpen: Boolean) {
        Log.d("GameViewModel", "handleBuildMenuClick: isOpen=$isOpen")
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
