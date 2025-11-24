package com.example.cataniaunited.logic.lobby

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.dto.UsernameRequest
import com.example.cataniaunited.logic.player.PlayerSessionManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

class LobbyLogic @Inject constructor(
    private val playerSessionManager: PlayerSessionManager
) {
    
    val playerIdErrorMessage: String = "Error when fetching player id"

    fun toggleReady(lobbyId: String) {
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            Log.e("LobbyLogic", playerIdErrorMessage, ise)
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
            Log.e("LobbyLogic", playerIdErrorMessage, ise)
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
            Log.e("LobbyLogic", playerIdErrorMessage, ise)
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

    fun endTurn(lobbyId: String){
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            Log.e("LobbyLogic", playerIdErrorMessage, ise)
            return
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(
                MessageDTO(
                    type = MessageType.END_TURN,
                    player = playerId,
                    lobbyId = lobbyId
                )
            )
        } else {
            Log.e("LobbyLogic", "WS not connected for endTurn")
        }
    }

    fun setUsername(lobbyId: String, username: String){
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            Log.e("LobbyLogic", playerIdErrorMessage, ise)
            return
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            val usernameRequest = UsernameRequest(username = username)
            val payload = Json.encodeToJsonElement(usernameRequest).jsonObject
            webSocketClient.sendMessage(
                MessageDTO(
                    type = MessageType.SET_USERNAME,
                    player = playerId,
                    lobbyId = lobbyId,
                    message = payload
                )
            )
        } else {
            Log.e("LobbyLogic", "WS not connected for setUsername")
        }
    }

    fun getLobbies(){
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            webSocketClient.sendMessage(MessageDTO(type = MessageType.GET_LOBBIES))
        } else {
            Log.e("LobbyLogic", "WS not connected for getLobbies")
        }
    }

}