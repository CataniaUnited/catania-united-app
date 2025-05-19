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

    private val _victoryPointsState = MutableStateFlow<Map<String, Int>>(emptyMap())
    val victoryPointsState: StateFlow<Map<String, Int>> = _victoryPointsState.asStateFlow()

    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun updateGameBoard(jsonString: String) {
        Log.d("GameDataHandler", "Processing new game board JSON: $jsonString")
        try {
            val json = jsonParser.parseToJsonElement(jsonString).jsonObject

            val boardJson = json["gameboard"]?.jsonObject ?: json
            val board = parseGameBoard(boardJson.toString())

            // Extract players from root level
            val players = json["players"]?.jsonObject?.let { playersJson ->
                playersJson.mapNotNull { (id, node) ->
                    try {
                        PlayerInfo(
                            playerId = id,
                            username = node.jsonObject["username"]?.jsonPrimitive?.contentOrNull ?: "",
                            colorHex = node.jsonObject["color"]?.jsonPrimitive?.contentOrNull ?: "#8C4E27",
                            victoryPoints = node.jsonObject["victoryPoints"]?.jsonPrimitive?.intOrNull ?: 0
                        )

                    } catch (e: Exception) {
                        Log.e("GameDataHandler", "Error parsing player $id", e)
                        null
                    }
                }
            } ?: _gameBoardState.value?.players

            val updatedBoard = board?.copy(
                tiles = board.tiles.toList(),
                settlementPositions = board.settlementPositions.toList(),
                roads = board.roads.toList(),
                players = players
            )

            if (updatedBoard != null) {
                _gameBoardState.value = updatedBoard
                Log.i("GameDataHandler", "Game board updated successfully with players: ${players?.size}")
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
}