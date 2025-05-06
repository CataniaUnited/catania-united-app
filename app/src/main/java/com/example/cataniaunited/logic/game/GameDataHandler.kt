package com.example.cataniaunited.data

import android.util.Log
import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.util.parseGameBoard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class GameDataHandler @Inject constructor() {
    private val _gameBoardState = MutableStateFlow<GameBoardModel?>(null)
    val gameBoardState: StateFlow<GameBoardModel?> = _gameBoardState.asStateFlow()

    fun updateGameBoard(jsonString: String) {
        Log.d("GameDataHandler", "Processing new game board JSON: board=${jsonString}")
        val board = parseGameBoard(jsonString)
        val updatedBoard = board?.let {
            it.copy(
                tiles = it.tiles.toList(),
                settlementPositions = it.settlementPositions.toList(),
                roads = it.roads.toList()
            )
        }
        if (updatedBoard != null) {
            //TODO: REMOVE
            val ownedRoads = updatedBoard.roads.filter { it.owner != null }
            Log.i("GameDataHandler", "Owned roads ${ownedRoads}")
            _gameBoardState.value = null
            _gameBoardState.value = updatedBoard
            Log.i("GameDataHandler", "Game board updated successfully.")
        } else {
            Log.e("GameDataHandler", "Failed to parse game board from JSON string.")
            _gameBoardState.value = null
        }
    }
}