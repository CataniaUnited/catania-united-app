package com.example.cataniaunited.ws

import com.example.cataniaunited.logic.dto.MessageDTO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketClient(private val serverUrl: String) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    // 1️⃣ The backing flow
    private val _messageFlow = MutableSharedFlow<MessageDTO>(replay = 0)
    /** Collect this to see every incoming MessageDTO. */
    val messageFlow = _messageFlow.asSharedFlow()

    fun connect(listener: WebSocketListener) {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun sendMessage(messageDTO: MessageDTO) {
        webSocket?.send(messageDTO.toString())
    }

    fun close() {
        webSocket?.close(1000, "Client closed connection")
    }

    /** Called by your listener implementation to push messages into the flow. */
    internal suspend fun dispatchIncoming(dto: MessageDTO) {
        _messageFlow.emit(dto)
    }
}
