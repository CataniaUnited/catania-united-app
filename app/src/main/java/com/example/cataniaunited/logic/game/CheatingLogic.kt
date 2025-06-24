package com.example.cataniaunited.logic.game

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.data.model.TileType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class CheatingLogic @Inject constructor(
    private val playerSessionManager: PlayerSessionManager
) {
    fun sendCheatAttempt(tileType: TileType, lobbyId: String) {
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            Log.e("CheatingLogic", "Error when fetching player id", ise)
            return
        }
        val wsClient = MainApplication.getInstance().getWebSocketClient()
        if (wsClient.isConnected()) {
            val messageDTO = MessageDTO(
                type = MessageType.CHEAT_ATTEMPT,
                player = playerId,
                lobbyId = lobbyId,
                message = buildJsonObject {
                    put("resource", tileType.name)
                }
            )
            wsClient.sendMessage(messageDTO)
        } else {
            Log.e("CheatingLogic", "WS not connected for sendCheatAttempt")
        }
    }

    fun sendReportPlayer(reportedId: String, lobbyId: String) {
        val reporterId = try {
            playerSessionManager.getPlayerId()
        } catch (e: IllegalStateException) {
            Log.e("CheatingLogic", "Failed to get player ID for report", e)
            return
        }

        val wsClient = MainApplication.getInstance().getWebSocketClient()
        if (wsClient.isConnected()) {
            val message = MessageDTO(
                type = MessageType.REPORT_PLAYER,
                player = reporterId,
                lobbyId = lobbyId,
                message = buildJsonObject {
                    put("reportedId", reportedId)
                }
            )
            wsClient.sendMessage(message)
        } else {
            Log.e("CheatingLogic", "WebSocket not connected (sendReportPlayer)")
        }
    }
}