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

    fun placeRobber(lobbyId: String, robberTileId: Int){
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            return
        }
        val message = buildJsonObject { put("robberTileId", robberTileId) }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(
                MessageDTO(
                    MessageType.PLACE_ROBBER,
                    playerId,
                    lobbyId,
                    null,
                    message
                )
            )
        } else {
            Log.e("GameBoardLogic", "WS not connected for placeRobber")
        }
    }

    fun rollDice(lobbyId: String) {
        try {
            val playerId = playerSessionManager.getPlayerId()
            val playerName = MainApplication.getInstance().players
                .firstOrNull { it.id == playerId }?.username ?: playerId
            val message = buildJsonObject {
                put("action", "rollDice")
                put("player", playerId)
                put("playerName", playerName)
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