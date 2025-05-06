package com.example.cataniaunited

import android.app.Application
import android.util.Log
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnWebSocketClosed
import com.example.cataniaunited.ws.callback.OnWebSocketError
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
open class MainApplication : Application(),
    OnConnectionSuccess,
    OnLobbyCreated,
    OnGameBoardReceived,
    OnWebSocketError,
    OnWebSocketClosed {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Inject
    lateinit var webSocketListener: WebSocketListenerImpl

    internal lateinit var webSocketClient: WebSocketClient
    private var _playerId: String? = null

    val _navigateToGameChannel = Channel<String>(Channel.BUFFERED)
    val navigateToGameFlow = _navigateToGameChannel.receiveAsFlow()

    var latestBoardJson: String? = null

    private val _currentLobbyIdFlow = MutableStateFlow<String?>(null) // Private Mutable StateFlow
    val currentLobbyIdFlow: StateFlow<String?> =
        _currentLobbyIdFlow.asStateFlow() // Public Immutable StateFlow

    var currentLobbyId: String?
        get() = _currentLobbyIdFlow.value // Getter reads from flow
        set(value) { // Setter updates the flow
            _currentLobbyIdFlow.value = value
        }

    companion object {
        @Volatile // Ensure visibility across threads
        private lateinit var instance: MainApplication
        fun getInstance(): MainApplication = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.i("MainApplication", "onCreate: Application instance initialized.")
        webSocketClient = WebSocketClient(BuildConfig.SERVER_URL)
        Log.i("MainApplication", "onCreate: Initializing WebSocket connection.")
        webSocketClient.connect(webSocketListener)
    }

    fun getPlayerId(): String {
        return _playerId ?: throw IllegalStateException("Player Id not initialized")
    }

    fun setPlayerId(playerId: String) {
        _playerId = playerId
        Log.i("MainApplication", "Player ID set to: $playerId")
    }

    fun getWebSocketClient(): WebSocketClient {
        // Ensure webSocketClient is initialized before returning
        check(::webSocketClient.isInitialized) { "WebSocketClient accessed before initialization in onCreate" }
        return webSocketClient
    }

    fun clearGameData() {
        latestBoardJson = null
        Log.d("MainApplication", "Cleared game board JSON.")
    }

    fun clearLobbyData() {
        currentLobbyId = null
        latestBoardJson = null
        Log.d("MainApplication", "Cleared lobby data.")
    }

    override fun onConnectionSuccess(playerId: String) {
        Log.d("MainApplication", "Callback: onConnectionSuccess. Player ID: $playerId")
        setPlayerId(playerId)
    }

    override fun onLobbyCreated(lobbyId: String) {
        Log.i("MainApplication", "Callback: onLobbyCreated. Lobby ID: $lobbyId")
        currentLobbyId = lobbyId
    }

    override fun onGameBoardReceived(lobbyId: String, boardJson: String) {
        Log.d("MainApplication", "Callback: onGameBoardReceived for Lobby $lobbyId.")
        if (latestBoardJson == null && lobbyId == _currentLobbyIdFlow.value) {
            applicationScope.launch {
                _navigateToGameChannel.send(lobbyId)
            }
        } else {
            Log.w("MainApplication", "Received board for wrong lobby.")
        }
        latestBoardJson = boardJson
    }

    override fun onError(error: Throwable) {
        Log.e("MainApplication", "Callback: onError. Error: ${error.message}", error)
    }

    override fun onClosed(code: Int, reason: String) {
        Log.w("MainApplication", "Callback: onClosed. Code=$code, Reason=$reason")
        clearGameData()
    }


}