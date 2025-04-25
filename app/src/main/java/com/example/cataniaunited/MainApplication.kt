package com.example.cataniaunited

import android.app.Application
import android.util.Log
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

open class MainApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    internal lateinit var webSocketClient: WebSocketClient
    private var _playerId: String? = null
    private val _navigateToGameChannel = Channel<String>(Channel.BUFFERED)
    val navigateToGameFlow = _navigateToGameChannel.receiveAsFlow()

    var latestBoardJson: String? = null
        private set

    companion object {
        @Volatile // Ensure visibility across threads
        private lateinit var instance: MainApplication

        fun getInstance(): MainApplication {
            return instance
        }
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
            onGameBoardReceived = { lobbyId, boardJson ->
                Log.d("MainApplication", "Callback: onGameBoardReceived for Lobby $lobbyId.")
                latestBoardJson = boardJson
                // Use the applicationScope to launch the coroutine for sending to channel
                applicationScope.launch {
                    try {
                        _navigateToGameChannel.send(lobbyId) // Use suspend 'send'
                        Log.d("MainApplication", "Sent lobbyId $lobbyId to navigation channel.")
                    } catch (e: Exception) {
                        Log.e("MainApplication", "Error sending navigation event for lobby $lobbyId", e)
                    }
                }
            },
            onError = { error ->
                Log.e("MainApplication", "Callback: onError. Error: ${error.message}", error)
            },
            onClosed = { code, reason ->
                Log.w("MainApplication", "Callback: onClosed. Code=$code, Reason=$reason")
            }
        )

        // Connect using the configured listener
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
        if (!::webSocketClient.isInitialized) {
            throw IllegalStateException("WebSocketClient accessed before initialization in onCreate")
        }
        return webSocketClient
    }

    fun clearGameBoardData() {
        latestBoardJson = null
        Log.d("MainApplication", "Cleared latest game board JSON.")
    }
}