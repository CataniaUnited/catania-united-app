package com.example.cataniaunited.ws

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(url: String, listener: WebSocketListener) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun close() {
        webSocket?.close(1000, "Client closed connection")
    }
}