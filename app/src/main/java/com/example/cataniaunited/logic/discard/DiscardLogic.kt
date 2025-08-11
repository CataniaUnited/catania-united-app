package com.example.cataniaunited.logic.discard

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.DiscardRequest
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

class DiscardLogic  @Inject constructor (
    private val playerSessionManager: PlayerSessionManager
){
    fun sendDiscardResources(lobbyId: String, discardRequest: DiscardRequest) {
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            Log.e("DiscardLogic", "Cannot send discard, player ID not available", ise)
            return
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            val payload = Json.encodeToJsonElement(discardRequest).jsonObject

            val message = MessageDTO(
                type = MessageType.DISCARD_RESOURCES,
                player = playerId,
                lobbyId = lobbyId,
                message = payload
            )
            webSocketClient.sendMessage(message)
            Log.i("DiscardLogic", "DISCARD_RESOURCES sent: $message")
        } else {
            Log.e("DiscardLogic", "WebSocket not connected, can not send discard.")
        }
    }
}