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
import com.example.cataniaunited.logic.lobby.LobbyLogic
import com.example.cataniaunited.logic.player.PlayerSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameBoardLogic: GameBoardLogic,
    private val lobbyLogic: LobbyLogic,
    private val gameDataHandler: GameDataHandler,
    private val sessionManager: PlayerSessionManager
) : ViewModel() {

    val playerId get() = sessionManager.getPlayerId()
    val gameBoardState: StateFlow<GameBoardModel?> = gameDataHandler.gameBoardState

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

    private val _robberTile = MutableStateFlow<List<Int>?>(null)
    val robberTile: StateFlow<List<Int>?> = _robberTile.asStateFlow()

    init {
        Log.d("GameViewModel", "ViewModel Initialized (Hilt).")

        _players.value = gameDataHandler.playersState.value
        val resources: Map<TileType, Int>? = _players.value[playerId]?.resources
        _playerResources.value = resources ?: TileType.entries
            .filter { it != TileType.DESERT }
            .associateWith { 0 }
        _victoryPoints.value = gameDataHandler.victoryPointsState.value
        _robberTile.value = gameDataHandler.robberTileState.value

        Log.d("GameViewModel", "Players initialized: ${players.value}")
        Log.d("GameViewModel", "Resources initialized: ${playerResources.value}")
        Log.d("GameViewModel", "Victory points initialized: ${victoryPoints.value}")

        viewModelScope.launch {
            gameDataHandler.victoryPointsState.collect {
                Log.d("GameViewModel_Collect", "Updating victory points value: $it")
                _victoryPoints.value = it
            }
        }

        viewModelScope.launch {
            gameDataHandler.playersState.collect {
                Log.d("GameViewModel_Collect", "RECEIVED playersState in collect: $it")
                _players.value = it

                val playerInfo: PlayerInfo? = it[playerId];
                if(playerInfo != null && !playerInfo.isActivePlayer){
                    //Close build menu when player is not active player
                    setBuildMenuOpen(false)
                }
            }
            Log.d("GameViewModel_Collect", "playersState collect FINISHED in ViewModelScope.")
        }

        viewModelScope.launch {
            gameDataHandler.robberTileState.collect {
                Log.d("GameViewModel_Collect", "RECEIVED robberTileState in collect: $it")
                _robberTile.value = it
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
        gameBoardLogic.placeRobber(tile.id, lobbyId)
    }

    fun handleSettlementClick(
        settlementPosition: SettlementPosition,
        isUpgrade: Boolean,
        lobbyId: String
    ) {
        Log.d(
            "GameViewModel",
            "handleSettlementClick: SettlementPosition ID=${settlementPosition.id}"
        )
        if (isUpgrade) {
            gameBoardLogic.upgradeSettlement(settlementPosition.id, lobbyId)
        } else {
            gameBoardLogic.placeSettlement(settlementPosition.id, lobbyId)
        }

    }

    fun handleRoadClick(road: Road, lobbyId: String) {
        Log.d("GameViewModel", "handleRoadClick: Road ID=${road.id}")
        gameBoardLogic.placeRoad(road.id, lobbyId)
    }

    fun handleEndTurnClick(lobbyId: String){
        Log.d("GameViewModel", "handleEndTurnClick: Player ended turn")
        lobbyLogic.endTurn(lobbyId)
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
