package com.example.cataniaunited.ws

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Receives raw text frames from OkHttp WebSocket,
 * parses them into MessageDTOs, handles the CONNECTION_SUCCESSFUL
 * and LOBBY_CREATED messages to stash IDs into MainApplication,
 * then re-emits every DTO into the shared flow for your UI to collect.
 */
class WebSocketListenerImpl : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "▶ Connection opened")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocket", "◀ Raw payload: $text")

        // 1) Parse JSON → DTO
        val msg: MessageDTO = MessageDTO.fromJson(text)
        Log.d("WebSocket", "◀ Parsed DTO: $msg")

        // 2a) On first connect, server sends CONNECTION_SUCCESSFUL with { "playerId": "…" }
        if (msg.type == MessageType.CONNECTION_SUCCESSFUL) {
            (msg.message as? JsonObject)
                ?.get("playerId")
                ?.jsonPrimitive
                ?.content
                ?.let { id: String ->
                    Log.d("WebSocket", "… setting playerId = $id")
                    MainApplication.instance.setPlayerId(id)
                }
        }

        // 2b) When lobby is created or joined, server sends LOBBY_CREATED with { "lobbyId": "…" }
        if (msg.type == MessageType.LOBBY_CREATED) {
            (msg.message as? JsonObject)
                ?.get("lobbyId")
                ?.jsonPrimitive
                ?.content
                ?.let { code: String ->
                    Log.d("WebSocket", "… setting lobbyId = $code")
                    MainApplication.instance.setLobbyId(code)
                }
        }

        // 3) Finally hand off every DTO into your shared flow
        GlobalScope.launch(Dispatchers.IO) {
            MainApplication.instance
                .webSocketClient()      // your client getter
                .dispatchIncoming(msg)  // your method to emit into the Flow/SharedFlow
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "⌛ Closing (code=$code, reason=$reason)")
        webSocket.close(1000, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "❌ Connection failure", t)
    }
}
