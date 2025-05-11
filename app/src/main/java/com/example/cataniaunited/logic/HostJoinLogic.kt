package com.example.cataniaunited.logic

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import javax.inject.Inject

class HostJoinLogic @Inject constructor(
    private val playerSessionManager: PlayerSessionManager
) {
    fun sendCreateLobby() {
        val playerId = try{
            playerSessionManager.getPlayerId()
        }catch (ise: IllegalStateException){
            return
        }

        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            val msg = MessageDTO(MessageType.CREATE_LOBBY, playerId, null, null, null)
            webSocketClient.sendMessage(msg)
            Log.i("HostJoinLogic", "CREATE_LOBBY sent")
        } else {
            Log.e("HostJoinLogic", "WebSocket not connected")
        }
    }

    fun sendJoinLobby(lobbyId: String) {
        val playerId = try{
            playerSessionManager.getPlayerId()
        }catch (ise: IllegalStateException){
            return
        }

        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            val msg = MessageDTO(MessageType.JOIN_LOBBY, playerId, lobbyId, null, null)
            webSocketClient.sendMessage(msg)
            Log.i("HostJoinLogic", "JOIN_LOBBY sent with lobbyId: $lobbyId")
        } else {
            Log.e("HostJoinLogic", "WebSocket not connected")
        }
    }

    //TODO: (Re-)move after implementation of lobby screen and start game
    private fun getGameboard(lobbyId: String){
        val playerId = try{
            playerSessionManager.getPlayerId()
        }catch (ise: IllegalStateException){
            return
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            val msg = MessageDTO(MessageType.JOIN_LOBBY, playerId, lobbyId, null, null)
            webSocketClient.sendMessage(msg)
            Log.i("HostJoinLogic", "JOIN_LOBBY sent with lobbyId: $lobbyId")
        } else {
            Log.e("HostJoinLogic", "WebSocket not connected")
        }
    }
}
