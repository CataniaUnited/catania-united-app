package com.example.cataniaunited.logic.game

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class GameBoardLogic @Inject constructor() {


    fun placeSettlement(settlementPositionId: Int, lobbyId: String) {
        val playerId = try { MainApplication.getInstance().getPlayerId() } catch (e: Exception) { Log.e("GameBoardLogic", "PlayerID Error", e); return }
        val message = buildJsonObject { put("settlementPositionId", settlementPositionId) }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(MessageDTO(MessageType.PLACE_SETTLEMENT, playerId, lobbyId, null, message))
        } else { Log.e("GameBoardLogic", "WS not connected for placeSettlement") }
    }

    fun placeRoad(roadId: Int, lobbyId: String) {
        val playerId = try { MainApplication.getInstance().getPlayerId() } catch (e: Exception) { Log.e("GameBoardLogic", "PlayerID Error", e); return }
        val message = buildJsonObject { put("roadId", roadId) }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(MessageDTO(MessageType.PLACE_ROAD, playerId, lobbyId, null, message))
        } else { Log.e("GameBoardLogic", "WS not connected for placeRoad") }
    }

    fun requestCreateLobby() {
        val playerId = try {
            MainApplication.getInstance().getPlayerId()
        } catch (e: IllegalStateException) {
            Log.e("GameBoardLogic", "Player ID not initialized when requesting lobby creation.", e)
            return
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            val messageToSend = MessageDTO( MessageType.CREATE_LOBBY, playerId, null, null, null)
            webSocketClient.sendMessage(messageToSend)
            Log.i("GameBoardLogic", "Sent CREATE_LOBBY request.")
        } else {
            Log.e("GameBoardLogic", "WebSocket not connected when trying to create lobby.")
        }
    }

    fun requestBoardForLobby(lobbyId: String, playerCount: Int = 4) {
        val playerId = try {
            MainApplication.getInstance().getPlayerId()
        } catch (e: IllegalStateException) {
            Log.e("GameBoardLogic", "Player ID not initialized when requesting new game board.", e)
            return
        }
        val messagePayload = buildJsonObject { put("playerCount", playerCount) }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            val messageToSend = MessageDTO( MessageType.CREATE_GAME_BOARD, playerId, lobbyId, null, messagePayload )
            webSocketClient.sendMessage(messageToSend)
            Log.i("GameBoardLogic", "Sent CREATE_GAME_BOARD request for $playerCount players in lobby $lobbyId.")
        } else {
            Log.e("GameBoardLogic", "WebSocket not connected when trying to create game board.")
        }
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