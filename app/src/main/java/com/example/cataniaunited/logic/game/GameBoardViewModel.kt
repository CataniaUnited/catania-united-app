package com.example.cataniaunited.logic.game

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.util.parseGameBoard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameBoardLogic: GameBoardLogic
    // Remove 'application: Application'
) : ViewModel() {

    private val _gameBoardState = MutableStateFlow<GameBoardModel?>(null)
    val gameBoardState: StateFlow<GameBoardModel?> = _gameBoardState.asStateFlow()

    init {
        Log.d("GameViewModel", "ViewModel Initialized (Hilt).")
        // Don't load initial board automatically here
    }

    // New function to be called externally (e.g., from the Composable's LaunchedEffect)
    fun initializeBoardState(initialJson: String?) {
        if (_gameBoardState.value == null) { // Only load if not already loaded
            Log.i("GameViewModel", "Initializing board state.")
            if (initialJson != null) {
                loadGameBoardFromJson(initialJson)
                // Maybe clear application state here if needed via injected dependency?
            } else {
                Log.e("GameViewModel", "Initial board JSON was null during initialization!")
                _gameBoardState.value = null
            }
        }
    }



    fun loadGameBoardFromJson(jsonString: String) {
        viewModelScope.launch {
            val board = parseGameBoard(jsonString)
            if (board != null) {
                _gameBoardState.value = board
                Log.i("GameViewModel", "Game board loaded/updated successfully from JSON.")
            } else {
                Log.e("GameViewModel", "Failed to parse game board from JSON string.")
                _gameBoardState.value = null
            }
        }
    }

    fun updateGameBoard(newBoard: GameBoardModel?) {
        viewModelScope.launch {
            _gameBoardState.value = newBoard
            if (newBoard != null) {
                Log.d("GameViewModel", "Game board updated directly with object.")
            } else {
                Log.w("GameViewModel", "Received null game board object for update.")
            }
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
        Log.d("GameViewModel", "handleSettlementClick: SettlementPosition ID=${settlementPosition.id}")
        // TODO: Implement logic for placing/upgrading settlement DON'T FORGET UPGRADE XD
        // 1) Check game state (setup or not? your turn?)
        // 2) Check resources
        // 3) Validate placement rules (distance, road connection)
        // 4) Get lobbyId and PlayerId
        // 5) Call gameBoardLogic.placeSettlement(settlementPosition.id, lobbyId)
    }

    fun handleRoadClick(road: Road, lobbyId: String) {
        Log.d("GameViewModel", "handleRoadClick: Road ID=${road.id}")
        // TODO: Implement logic for placing road
        // 1) Check game state (setup or not? your turn?)
        // 2) Check resources
        // 3) Validate placement rules (road connection, empty)
        // 4) Get lobbyId and PlayerId
        // 5) Call gameBoardLogic.placeRoad(road.id, lobbyId)
    }
}

