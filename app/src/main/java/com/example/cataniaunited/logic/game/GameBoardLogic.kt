package com.example.cataniaunited.logic.game

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GameBoardLogic {


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


    fun requestNewGameBoard(playerCount: Int, lobbyId: String) { // Keep lobbyId parameter for now
        val playerId = try {
            MainApplication.getInstance().getPlayerId()
        } catch (e: IllegalStateException) {
            Log.e("GameBoardLogic", "Player ID not initialized when requesting new game board.", e)
            return // Cannot proceed without player ID
        }

        // The payload only contains playerCount based on backend need described earlier
        val messagePayload = buildJsonObject {
            put("playerCount", playerCount)
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()

        if (webSocketClient.isConnected()) {
            val messageToSend = MessageDTO(
                type = MessageType.CREATE_GAME_BOARD,
                player = playerId,
                lobbyId = lobbyId,
                players = null,
                message = messagePayload
            )
            webSocketClient.sendMessage(messageToSend)
            Log.i("GameBoardLogic", "Sent CREATE_GAME_BOARD request for $playerCount players in lobby $lobbyId.")
        } else {
            Log.e("GameBoardLogic", "WebSocket not connected when trying to create game board.")
        }
    }
}