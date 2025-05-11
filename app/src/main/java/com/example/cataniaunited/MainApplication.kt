// app/src/main/java/com/example/cataniaunited/MainApplication.kt
package com.example.cataniaunited

import android.app.Application
import android.util.Log
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

/**
 * Application-wide singleton that
 *  • owns the [WebSocketClient]
 *  • exposes reactive state (playerId, current lobby, latest board) to the UI
 *  • relays one-off navigation events through a [Channel]
 */
@HiltAndroidApp
class MainApplication : Application() {

    /* -------------------------------------------------- */
    /*  Coroutine scope                                   */
    /* -------------------------------------------------- */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /* -------------------------------------------------- */
    /*  WebSocket                                         */
    /* -------------------------------------------------- */
    lateinit var webSocketClient: WebSocketClient
        private set

    /* -------------------------------------------------- */
    /*  Reactive state                                    */
    /* -------------------------------------------------- */
    private val _playerId          = MutableStateFlow<String?>(null)
    private val _currentLobbyId    = MutableStateFlow<String?>(null)
    private val _latestBoardJson   = MutableStateFlow<String?>(null)

    val playerIdFlow:          StateFlow<String?> = _playerId.asStateFlow()
    val currentLobbyIdFlow:    StateFlow<String?> = _currentLobbyId.asStateFlow()
    val latestBoardJsonFlow:   StateFlow<String?> = _latestBoardJson.asStateFlow()

    /* one-shot navigation channel (GameScreen) */
    private val _navToGameChannel = Channel<String>(Channel.BUFFERED)
    val   navigateToGameFlow      = _navToGameChannel.receiveAsFlow()

    val latestBoardJson: String?
        get() = latestBoardJsonFlow.value          // read-only accessor

    /* -------------------------------------------------- */
    /*  Singleton accessor                                */
    /* -------------------------------------------------- */
    companion object {
        @Volatile private lateinit var inst: MainApplication
        fun getInstance(): MainApplication = inst
    }

    /* -------------------------------------------------- */
    /*  Lifecycle                                         */
    /* -------------------------------------------------- */
    override fun onCreate() {
        super.onCreate()
        inst = this

        Log.i("MainApplication", "Booting application")

        /* ---  build WS listener  --- */
        val listener = WebSocketListenerImpl(
            onConnectionSuccess = { id ->
                Log.i("MainApplication", "WS: connected – playerId=$id")
                _playerId.value = id
            },

            onLobbyCreated = { lobbyId ->
                Log.i("MainApplication", "WS: lobby created – id=$lobbyId")
                _currentLobbyId.value = lobbyId
            },

            onGameBoardReceived = { lobbyId, boardJson ->
                Log.i("MainApplication", "WS: board received for $lobbyId")
                _latestBoardJson.value = boardJson

                /* forward navigation if we’re still in that lobby */
                if (lobbyId == _currentLobbyId.value) {
                    appScope.launch { _navToGameChannel.send(lobbyId) }
                } else {
                    Log.w(
                        "MainApplication",
                        "Board for $lobbyId ignored (current lobby is ${_currentLobbyId.value})"
                    )
                }
            },

            onError = { err ->
                Log.e("MainApplication", "WS error: ${err.message}", err)
            },

            onClosedCb = { code, reason ->
                Log.w("MainApplication", "WS closed $code / $reason – clearing state")
                _playerId.value        = null
                _currentLobbyId.value  = null
                _latestBoardJson.value = null
            }
        )

        /* ---  connect  --- */
        webSocketClient = WebSocketClient(BuildConfig.SERVER_URL)
        webSocketClient.connect(listener)
    }

    /* -------------------------------------------------- */
    /*  Public helpers                                    */
    /* -------------------------------------------------- */

    /** throws if not yet known */
    fun getPlayerId(): String =
        _playerId.value ?: error("Player ID not initialised yet")

    fun clearLobbyData() {
        _currentLobbyId.value  = null
        _latestBoardJson.value = null
    }

    fun clearBoard() {
        _latestBoardJson.value = null
    }
}
