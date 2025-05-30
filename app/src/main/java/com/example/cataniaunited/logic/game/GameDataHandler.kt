package com.example.cataniaunited.logic.game

import android.util.Log
import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.data.util.parseGameBoard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class GameDataHandler @Inject constructor() {
    private val _gameBoardState = MutableStateFlow<GameBoardModel?>(null)
    val gameBoardState: StateFlow<GameBoardModel?> = _gameBoardState.asStateFlow()

    private val _playersState = MutableStateFlow<Map<String, PlayerInfo>>(emptyMap())
    val playersState: StateFlow<Map<String, PlayerInfo>> = _playersState.asStateFlow()

    private val _victoryPointsState = MutableStateFlow<Map<String, Int>>(emptyMap())
    val victoryPointsState: StateFlow<Map<String, Int>> = _victoryPointsState.asStateFlow()

    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun updateGameBoard(jsonString: String) {
        Log.d("GameDataHandler", "Processing new game board JSON: $jsonString")
        try {
            val json = jsonParser.parseToJsonElement(jsonString).jsonObject

            val boardJson = json["gameboard"]?.jsonObject ?: json
            val board = parseGameBoard(boardJson.toString())

            val updatedBoard = board?.copy(
                tiles = board.tiles.toList(),
                settlementPositions = board.settlementPositions.toList(),
                roads = board.roads.toList(),
            )

            if (updatedBoard != null) {
                _gameBoardState.value = updatedBoard
                Log.i("GameDataHandler", "Game board updated successfully")
            } else {
                Log.e("GameDataHandler", "Failed to parse game board")
            }
        } catch (e: Exception) {
            Log.e("GameDataHandler", "Error parsing game board JSON", e)
        }
    }

    fun updateVictoryPoints(vpMap: Map<String, Int>) {
        _victoryPointsState.value = vpMap
        Log.d("GameDataHandler", "Updated victory points: $vpMap")
    }

    fun updatePlayers(players: Map<String, PlayerInfo>){
        val newPlayersMap = players.toMap()
        if (_playersState.value != newPlayersMap) {
            _playersState.value = newPlayersMap
            Log.d("GameDataHandler", "Updated players: $newPlayersMap (Value changed)")
        } else {
            Log.d("GameDataHandler", "PlayersState value unchanged. Not emitting new value.")
        }
    }
}