package com.example.cataniaunited.logic.lobby

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import javax.inject.Inject

class LobbyLogic @Inject constructor(
    private val playerSessionManager: PlayerSessionManager
) {

    fun toggleReady(lobbyId: String) {
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            return
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(
                MessageDTO(
                    type = MessageType.SET_READY,
                    player = playerId,
                    lobbyId = lobbyId
                )
            )
        } else {
            Log.e("GameBoardLogic", "WS not connected for placeRoad")
        }
    }

}