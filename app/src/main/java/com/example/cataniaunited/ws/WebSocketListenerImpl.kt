package com.example.cataniaunited.ws

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

open class WebSocketListenerImpl : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Opened connection")
    }

    /**
     * Callback endpoint which retrieves messages from the server
     */
    override fun onMessage(webSocket: WebSocket, message: String) {
        val messageDTO: MessageDTO = MessageDTO.fromJson(message);
        Log.d("WebSocket", "Received message: $messageDTO")

        if(MessageType.CONNECTION_SUCCESSFUL == messageDTO.type){
            setPlayerId(messageDTO)
        }
    }

    fun setPlayerId(messageDTO: MessageDTO) {
        val playerId: String = messageDTO.message?.get("playerId")?.jsonPrimitive?.content!!
        Log.d("WebSocket", "Setting player id: playerId=$playerId")
        MainApplication.getInstance().setPlayerId(playerId)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "Closing connection: Code=$code, Reason=$reason")
        webSocket.close(1000, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "Connection threw exception", t)
    }
}