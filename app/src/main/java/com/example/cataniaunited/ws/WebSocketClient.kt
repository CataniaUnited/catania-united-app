package com.example.cataniaunited.ws

import com.example.cataniaunited.logic.dto.MessageDTO
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

open class WebSocketClient(private val serverUrl: String) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private lateinit var listener: WebSocketListenerImpl
    open fun connect(listener: WebSocketListener) {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(messageDTO: MessageDTO){
        webSocket?.send(messageDTO.toString())
    }

    fun close() {
        webSocket?.close(1000, "Client closed connection")
    }

    fun setListener(listener: WebSocketListenerImpl) {
        this.listener = listener
    }

}