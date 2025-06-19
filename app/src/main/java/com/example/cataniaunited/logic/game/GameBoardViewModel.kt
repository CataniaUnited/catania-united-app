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
import com.example.cataniaunited.logic.dto.TradeRequest
import com.example.cataniaunited.logic.lobby.LobbyLogic
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.logic.trade.TradeLogic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val sessionManager: PlayerSessionManager,
    private val tradeLogic: TradeLogic,
) : ViewModel() {

    val playerId get() = sessionManager.getPlayerId()
    val gameBoardState: StateFlow<GameBoardModel?> = gameDataHandler.gameBoardState

    private val _isBuildMenuOpen = MutableStateFlow(false)
    val isBuildMenuOpen: StateFlow<Boolean> = _isBuildMenuOpen

    private val _diceResult = MutableStateFlow<Pair<Int, Int>?>(null)
    val diceResult: StateFlow<Pair<Int, Int>?> = _diceResult

    val diceState: StateFlow<DiceState?> = gameDataHandler.diceState

    private val _showDicePopup = MutableStateFlow(false)
    val showDicePopup: StateFlow<Boolean> = _showDicePopup
    private val _victoryPoints = MutableStateFlow<Map<String, Int>>(emptyMap())
    val victoryPoints: StateFlow<Map<String, Int>> = _victoryPoints

    private val _playerResources = MutableStateFlow<Map<TileType, Int>>(emptyMap())
    val playerResources: StateFlow<Map<TileType, Int>> = _playerResources.asStateFlow()

    private val _players = MutableStateFlow<Map<String, PlayerInfo>>(emptyMap())
    val players: StateFlow<Map<String, PlayerInfo>> = _players.asStateFlow()

    private val _isTradeMenuOpen = MutableStateFlow(false)
    val isTradeMenuOpen: StateFlow<Boolean> = _isTradeMenuOpen.asStateFlow()

    private val _tradeOffer = MutableStateFlow<Pair<Map<TileType, Int>, Map<TileType, Int>>>(Pair(emptyMap(), emptyMap()))
    val tradeOffer: StateFlow<Pair<Map<TileType, Int>, Map<TileType, Int>>> = _tradeOffer.asStateFlow()

    init {
        Log.d("GameViewModel", "ViewModel Initialized (Hilt).")

        _players.value = gameDataHandler.playersState.value
        val resources: Map<TileType, Int>? = _players.value[playerId]?.resources
        _playerResources.value = resources ?: TileType.entries
            .filter { it != TileType.WASTE }
            .associateWith { 0 }
        _victoryPoints.value = gameDataHandler.victoryPointsState.value

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

        val currentPlayer = players.value[playerId]
        if (currentPlayer?.canRollDice != true) {
            Log.w("GameViewModel", "Player cannot roll dice now")
            isProcessingRoll = false
            return
        }

        Log.d("GameViewModel", "Initiating dice roll for lobby: $lobbyId")
        startRolling(currentPlayer.username)

        gameBoardLogic.rollDice(lobbyId)

        viewModelScope.launch {
            delay(3000) // timeout

            if (diceState.value?.isRolling == true) {
                Log.e("GameViewModel", "Dice roll timeout")
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
        val rollingPlayerUsername: String?,
        val isRolling: Boolean,
        val dice1: Int = 1,
        val dice2: Int = 1,
        val showResult: Boolean = false
    )

    fun startRolling(playerName: String?) {
        viewModelScope.launch {
            gameDataHandler.updateDiceState(
                DiceState(
                    rollingPlayerUsername = playerName,
                    isRolling = true,
                    dice1 = (1..6).random(),
                    dice2 = (1..6).random(),
                    showResult = false
                )
            )
            delay(2000) // timeout
        }
    }

    fun showResult(playerName: String?, dice1: Int, dice2: Int) {
        viewModelScope.launch {
            gameDataHandler.updateDiceState(
                  DiceState(
                    rollingPlayerUsername = playerName,
                    isRolling = false,
                    dice1 = dice1,
                    dice2 = dice2,
                    showResult = true
                )
            )
            delay(3000) // Show result for 3 seconds
            resetDiceState()
        }
    }

    fun resetDiceState() {
        viewModelScope.launch {
            gameDataHandler.updateDiceState(null)
        }
    }

    fun setTradeMenuOpen(isOpen: Boolean) {
        _isTradeMenuOpen.value = isOpen
        if (!isOpen) {
            // Reset the trade offer when the menu is closed
            _tradeOffer.value = Pair(emptyMap(), emptyMap())
        }
    }

    fun updateOfferedResource(resource: TileType, delta: Int) {
        val currentOffer = _tradeOffer.value.first.toMutableMap()
        val currentCount = currentOffer.getOrDefault(resource, 0)
        val newCount = currentCount + delta

        // Validation: Ensure count doesn't go below zero and player has the resource
        val playerHas = playerResources.value.getOrDefault(resource, 0)
        if (newCount >= 0 && newCount <= playerHas) {
            if (newCount == 0) {
                currentOffer.remove(resource)
            } else {
                currentOffer[resource] = newCount
            }
            _tradeOffer.value = Pair(currentOffer, _tradeOffer.value.second)
        }
    }

    fun updateTargetResource(resource: TileType, delta: Int) {
        val currentTarget = _tradeOffer.value.second.toMutableMap()
        val currentCount = currentTarget.getOrDefault(resource, 0)
        val newCount = currentCount + delta

        if (newCount >= 0) {
            if (newCount == 0) {
                currentTarget.remove(resource)
            } else {
                currentTarget[resource] = newCount
            }
            _tradeOffer.value = Pair(_tradeOffer.value.first, currentTarget)
        }
    }

    fun submitBankTrade(lobbyId: String) {
        val (offered, target) = _tradeOffer.value
        val tradeRequest = TradeRequest(offered, target)
        tradeLogic.sendBankTrade(lobbyId, tradeRequest)
        setTradeMenuOpen(false) // Close menu after submitting
    }
}
