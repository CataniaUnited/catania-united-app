package com.example.cataniaunited.ws

import android.util.Log
import com.example.cataniaunited.logic.dto.MessageDTO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

open class WebSocketClient(private val serverUrl: String) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val jsonParser = Json { encodeDefaults = true }

    open fun connect(listener: WebSocketListener) {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, listener)
    }


    fun isConnected(): Boolean {
        return webSocket != null
    }


    fun sendMessage(messageDTO: MessageDTO): Boolean {
        val socket = webSocket // Capture current socket instance
        if (socket == null) {
            Log.e("WebSocketClient", "Cannot send message, WebSocket is null.")
            return false
        }
        return try {
            val jsonMessage = jsonParser.encodeToString(messageDTO) // Serialize DTO
            Log.d("WebSocketClient", "Sending: $jsonMessage")
            socket.send(jsonMessage) // Send the JSON string
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Error encoding/sending message", e)
            false
        }
    }

    fun close() {
        webSocket?.close(1000, "Client closed connection")
        webSocket = null
    }
}