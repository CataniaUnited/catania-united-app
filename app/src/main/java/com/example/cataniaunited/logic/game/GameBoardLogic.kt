package com.example.cataniaunited.logic.game

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class GameBoardLogic @Inject constructor(
    private val playerSessionManager: PlayerSessionManager
) {

    fun placeSettlement(settlementPositionId: Int, lobbyId: String) {
        sendSettlementMessage(MessageType.PLACE_SETTLEMENT, settlementPositionId, lobbyId)
    }

    fun upgradeSettlement(settlementPositionId: Int, lobbyId: String) {
        sendSettlementMessage(MessageType.UPGRADE_SETTLEMENT, settlementPositionId, lobbyId)
    }

    private fun sendSettlementMessage(
        messageType: MessageType,
        settlementPositionId: Int,
        lobbyId: String
    ) {
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            return
        }
        val message = buildJsonObject { put("settlementPositionId", settlementPositionId) }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(MessageDTO(messageType, playerId, lobbyId, null, message))
        } else {
            Log.e("GameBoardLogic", "WS not connected for upgradeSettlement")
        }
    }

    fun placeRoad(roadId: Int, lobbyId: String) {
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            return
        }
        val message = buildJsonObject { put("roadId", roadId) }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(
                MessageDTO(
                    MessageType.PLACE_ROAD,
                    playerId,
                    lobbyId,
                    null,
                    message
                )
            )
        } else {
            Log.e("GameBoardLogic", "WS not connected for placeRoad")
        }
    }

    fun requestCreateLobby() {
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            return
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            val messageToSend = MessageDTO(MessageType.CREATE_LOBBY, playerId, null, null, null)
            webSocketClient.sendMessage(messageToSend)
            Log.i("GameBoardLogic", "Sent CREATE_LOBBY request.")

        } else {
            Log.e("GameBoardLogic", "WebSocket not connected when trying to create lobby.")
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

    fun setActivePlayer(playerId: String, lobbyId: String) {
        val message = MessageDTO(
            type = MessageType.SET_ACTIVE_PLAYER,
            player = playerId,
            lobbyId = lobbyId,
            message = null
        )
        MainApplication.getInstance().getWebSocketClient().sendMessage(message)
    }


}