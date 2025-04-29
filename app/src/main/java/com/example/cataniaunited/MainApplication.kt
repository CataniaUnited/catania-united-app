package com.example.cataniaunited

import android.app.Application
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl

class MainApplication : Application() {

    companion object {
        /** Global access to the app instance */
        lateinit var instance: MainApplication
            private set
    }

    /** Your WebSocket client, initialized with the URL from BuildConfig */
    private val _webSocketClient = WebSocketClient(BuildConfig.SERVER_URL)

    /** Backing fields for playerId and lobbyId */
    private var _playerId: String? = null
    private var _lobbyId:  String? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Kick off the WS connection immediately
        _webSocketClient.connect(WebSocketListenerImpl())
    }

    /** Expose your WS client */
    fun webSocketClient(): WebSocketClient = _webSocketClient

    /** Player ID getter/setter */
    fun setPlayerId(id: String) {
        _playerId = id
    }
    fun getPlayerId(): String =
        _playerId ?: throw IllegalStateException("Player ID not initialized")

    /** Lobby ID getter/setter */
    fun setLobbyId(id: String) {
        _lobbyId = id
    }
    fun getLobbyId(): String =
        _lobbyId ?: throw IllegalStateException("Lobby ID not initialized")
}
