package com.example.cataniaunited

import android.app.Application
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.WebSocketClient

open class MainApplication : Application() {

    internal var webSocketClient: WebSocketClient = WebSocketClient(BuildConfig.SERVER_URL)
    private var _playerId: String? = null

    companion object {
        private lateinit var instance: MainApplication
        fun getInstance() = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        //Initialize web socket manager
        webSocketClient.connect(WebSocketListenerImpl())
    }

    fun getPlayerId(): String {
        return _playerId ?: throw IllegalStateException("Player Id not initialized")
    }

    fun setPlayerId(playerId: String) {
        _playerId = playerId
    }

    fun getWebSocketClient(): WebSocketClient {
        return webSocketClient;
    }
}