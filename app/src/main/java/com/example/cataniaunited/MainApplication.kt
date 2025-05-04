package com.example.cataniaunited

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.cataniaunited.logic.lobby.LobbyColorManager
import com.example.cataniaunited.ui.lobby.LobbyPlayer
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl
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

@HiltAndroidApp
open class MainApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    internal lateinit var webSocketClient: WebSocketClient
    private var _playerId: String? = null
    val playersInLobby = mutableStateListOf<LobbyPlayer>()
    val _navigateToGameChannel = Channel<String>(Channel.BUFFERED)
    val navigateToGameFlow = _navigateToGameChannel.receiveAsFlow()

    var latestBoardJson: String? = null


    private val _currentLobbyIdFlow = MutableStateFlow<String?>(null) // Private Mutable StateFlow
    val currentLobbyIdFlow: StateFlow<String?> = _currentLobbyIdFlow.asStateFlow() // Public Immutable StateFlow

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

        val listener = WebSocketListenerImpl(
            onConnectionSuccess = { playerId ->
                Log.d("MainApplication", "Callback: onConnectionSuccess. Player ID: $playerId")
                setPlayerId(playerId)
            },
            onLobbyCreated = { lobbyId ->
                Log.i("MainApplication", "Callback: onLobbyCreated. Lobby ID: $lobbyId")
                currentLobbyId = lobbyId
                addHostToLobby(lobbyId)
                Log.d("MainApplication", ">>> _currentLobbyIdFlow value is now: ${_currentLobbyIdFlow.value}")
            },
            onGameBoardReceived = { lobbyId, boardJson ->
                Log.d("MainApplication", "Callback: onGameBoardReceived for Lobby $lobbyId.")
                latestBoardJson = boardJson
                if (lobbyId == _currentLobbyIdFlow.value) {
                    applicationScope.launch {
                        try {
                            _navigateToGameChannel.send(lobbyId)
                            Log.d("MainApplication", "Sent lobbyId $lobbyId to navigation channel.")
                        } catch (e: Exception) {
                            Log.e("MainApplication", "Error sending navigation event for lobby $lobbyId", e)
                        }
                    }
                } else {
                    Log.w("MainApplication", "Received board for lobby $lobbyId, but current lobby flow value is ${_currentLobbyIdFlow.value}. Ignoring navigation.")
                }
            },
            onError = { error ->
                Log.e("MainApplication", "Callback: onError. Error: ${error.message}", error)
            },
            onClosed = { code, reason ->
                Log.w("MainApplication", "Callback: onClosed. Code=$code, Reason=$reason")
                // Reset state on disconnect
                _playerId = null
                currentLobbyId = null
                latestBoardJson = null
            }
        )
        webSocketClient.connect(listener)
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

    fun addHostToLobby(lobbyId: String) {
        val hostUsername = "HostPlayer"
        val hostColor = LobbyColorManager.assignColor(lobbyId, hostUsername)

        if (hostColor != null) {
            playersInLobby.clear()
            playersInLobby.add(LobbyPlayer(username = hostUsername, color = hostColor))
            Log.i("LobbySetup", "Host $hostUsername was added in lobby $lobbyId with color $hostColor")
        }
        else {
            Log.e("LobbySetup", "ERROR: No more colors available for host $hostUsername in lobby $lobbyId")
        }

    }
}