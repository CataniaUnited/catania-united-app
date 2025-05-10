package com.example.cataniaunited.logic

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType

object HostJoinLogic {
    fun sendCreateLobby() {
        val playerId = try {
            MainApplication.getInstance().getPlayerId()
        } catch (e: Exception) {
            Log.e("HostJoinLogic", "PlayerID Error", e)
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
        val playerId = try {
            MainApplication.getInstance().getPlayerId()
        } catch (e: Exception) {
            Log.e("HostJoinLogic", "PlayerID Error", e)
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
