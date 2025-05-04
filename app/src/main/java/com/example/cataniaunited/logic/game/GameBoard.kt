package com.example.cataniaunited.logic.game

import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.util.Log

class GameBoard {

    fun placeSettlement(settlementPositionId: Int, lobbyId: String) {
        val playerId = MainApplication.getInstance().getPlayerId()
        val message = buildJsonObject {
            put("settlementPositionId", settlementPositionId)
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        webSocketClient.sendMessage(MessageDTO(MessageType.PLACE_SETTLEMENT, playerId, lobbyId, null, message))
    }

    fun placeRoad(roadId: Int, lobbyId: String) {
        val playerId = MainApplication.getInstance().getPlayerId()
        val message = buildJsonObject {
            put("roadId", roadId)
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        webSocketClient.sendMessage(MessageDTO(MessageType.PLACE_ROAD, playerId, lobbyId, null, message))
    }

    fun rollDice(lobbyId: String) {
        try {
            Log.d("GameBoard", "Rolling dice for lobby: $lobbyId")
            val playerId = MainApplication.getInstance().getPlayerId()
            val message = buildJsonObject {
                put("action", "rollDice")
            }
            val webSocketClient = MainApplication.getInstance().getWebSocketClient()
            webSocketClient.sendMessage(
                MessageDTO(
                    MessageType.ROLL_DICE,
                    playerId,
                    lobbyId,
                    null,
                    message
                )
            )
        } catch (e: Exception) {
            Log.e("GameBoard", "Error rolling dice", e)
        }
    }
}