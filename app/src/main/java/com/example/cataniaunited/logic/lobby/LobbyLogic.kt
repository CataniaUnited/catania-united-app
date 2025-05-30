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
            Log.e("LobbyLogic", "Error when fetching player id", ise)
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
            Log.e("LobbyLogic", "WS not connected for toggleReady")
        }
    }

    fun leaveLobby(lobbyId: String) {
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            Log.e("LobbyLogic", "Error when fetching player id", ise)
            return
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(
                MessageDTO(
                    type = MessageType.LEAVE_LOBBY,
                    player = playerId,
                    lobbyId = lobbyId
                )
            )
        } else {
            Log.e("LobbyLogic", "WS not connected for leaveLobby")
        }
    }

    fun startGame(lobbyId: String) {
        Log.d("LobbyLogic", "Starting game for lobby: $lobbyId")
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            Log.e("LobbyLogic", "Error when fetching player id", ise)
            return
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(
                MessageDTO(
                    MessageType.START_GAME,
                    playerId,
                    lobbyId,
                )
            )
        } else {
            Log.e("LobbyLogic", "WS not connected for leaveLobby")
        }

    }

}