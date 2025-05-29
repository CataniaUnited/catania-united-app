package com.example.cataniaunited.logic.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cataniaunited.data.model.GameBoardModel
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

    private val _diceState = MutableStateFlow<DiceState?>(null)
    val diceState: StateFlow<DiceState?> = _diceState

    private val _showDicePopup = MutableStateFlow(false)
    val showDicePopup: StateFlow<Boolean> = _showDicePopup
    private val _victoryPoints = MutableStateFlow<Map<String, Int>>(emptyMap())
    val victoryPoints: StateFlow<Map<String, Int>> = _victoryPoints

    private val _playerResources = MutableStateFlow<Map<TileType, Int>>(emptyMap())
    val playerResources: StateFlow<Map<TileType, Int>> = _playerResources.asStateFlow()

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
        }

        viewModelScope.launch {
            gameDataHandler.victoryPointsState.collect {
                _victoryPoints.value = it
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

        startRolling(playerId)

        gameBoardLogic.rollDice(lobbyId)

        viewModelScope.launch {
            if (diceState.value?.isRolling == true) {
                resetDiceState()
            }
            isProcessingRoll = false
        }
    }

    fun updateDiceResult(dice1: Int?, dice2: Int?) {
        viewModelScope.launch {
            _diceResult.value = if (dice1 != null && dice2 != null) dice1 to dice2 else null
        }
    }

    data class DiceState(
        val rollingPlayer: String?,
        val isRolling: Boolean,
        val dice1: Int = 1,
        val dice2: Int = 1,
        val showResult: Boolean = false
    )

    fun startRolling(playerName: String?) {
        Log.d("GameViewModel", "Starting dice roll for player: $playerName")

        val currentState = _diceState.value

        if (currentState == null || (!currentState.isRolling && !currentState.showResult)) {
            _diceState.value = DiceState(
                rollingPlayer = playerName,
                isRolling = true,
                showResult = false
            )
            _showDicePopup.value = true
            Log.d("GameViewModel", "Started rolling for player: $playerName")
        } else {
            Log.d("GameViewModel", "Cannot start rolling - current state: isRolling=${currentState.isRolling}, showResult=${currentState.showResult}")
        }
    }

    fun showResult(playerName: String?, dice1: Int, dice2: Int) {
        Log.d("GameViewModel", "Showing dice result for $playerName: $dice1, $dice2")
        val currentState = _diceState.value

        if (currentState?.isRolling == true) {
            _diceState.value = DiceState(
                rollingPlayer = currentState.rollingPlayer ?: playerName,
                isRolling = false,
                dice1 = dice1,
                dice2 = dice2,
                showResult = true
            )
            updateDiceResult(dice1, dice2)
            Log.d("GameViewModel", "Updated dice state to show result")
        } else if (currentState == null) {
            Log.w("GameViewModel", "Received dice result without rolling state, creating result state")
            _diceState.value = DiceState(
                rollingPlayer = playerName,
                isRolling = false,
                dice1 = dice1,
                dice2 = dice2,
                showResult = true
            )
            _showDicePopup.value = true
            updateDiceResult(dice1, dice2)
        } else {
            Log.w("GameViewModel", "Received dice result but current state is not rolling: $currentState")
        }
    }

    fun resetDiceState() {
        Log.d("GameViewModel", "Resetting dice state")
        _diceState.value = null
        _showDicePopup.value = false
        updateDiceResult(null, null)
    }

    fun closeDicePopup() {
        Log.d("GameViewModel", "Closing dice popup")
        _showDicePopup.value = false
        viewModelScope.launch {
            resetDiceState()
        }
    }
}
