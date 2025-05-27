package com.example.cataniaunited

import android.app.Application
import android.util.Log
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.data.model.TileType
import androidx.compose.runtime.mutableStateListOf
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnDiceResult
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnLobbyUpdated
import com.example.cataniaunited.ws.callback.OnPlayerResourcesReceived
import com.example.cataniaunited.ws.callback.OnPlayerJoined
import com.example.cataniaunited.ws.callback.OnWebSocketClosed
import com.example.cataniaunited.ws.callback.OnWebSocketError
import com.example.cataniaunited.ws.provider.WebSocketErrorProvider
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
    OnPlayerJoined,
    OnLobbyUpdated,
    OnGameBoardReceived,
    OnWebSocketError,
    OnWebSocketClosed,
    OnDiceResult,
    OnPlayerResourcesReceived,
    WebSocketErrorProvider {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Inject
    lateinit var webSocketListener: WebSocketListenerImpl

    internal lateinit var webSocketClient: WebSocketClient
    private var _playerId: String? = null
    val players = mutableStateListOf<PlayerInfo>()
    val _navigateToLobbyChannel = Channel<String>(Channel.BUFFERED)
    val navigateToLobbyFlow = _navigateToLobbyChannel.receiveAsFlow()
    val _navigateToGameChannel = Channel<String>(Channel.BUFFERED)
    val navigateToGameFlow = _navigateToGameChannel.receiveAsFlow()

    private val _errorChannel = Channel<String>(Channel.BUFFERED)

    //override errorFlow of WebSocketErrorProvider
    override val errorFlow = _errorChannel.receiveAsFlow()

    var latestBoardJson: String? = null

    private val _currentLobbyIdFlow = MutableStateFlow<String?>(null) // Private Mutable StateFlow
    val currentLobbyIdFlow: StateFlow<String?> =
        _currentLobbyIdFlow.asStateFlow() // Public Immutable StateFlow

    var gameViewModel: GameViewModel? = null
    var currentLobbyId: String?
        get() = _currentLobbyIdFlow.value // Getter reads from flow
        set(value) { // Setter updates the flow
            _currentLobbyIdFlow.value = value
        }

    private val _gameWonState = MutableStateFlow<Pair<PlayerInfo, List<PlayerInfo>>?>(null)
    val gameWonState: StateFlow<Pair<PlayerInfo, List<PlayerInfo>>?> = _gameWonState.asStateFlow()


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

    override fun onLobbyCreated(
        lobbyId: String,
        playerId: String,
        username: String?,
        color: String?
    ) {
        Log.i("MainApplication", "Callback: onLobbyCreated. Lobby ID: $lobbyId with playerId: $playerId, username: $username, color: $color")
        if(lobbyId == _currentLobbyIdFlow.value || _currentLobbyIdFlow.value == null){
            applicationScope.launch {
                _navigateToLobbyChannel.send(lobbyId)
                Log.d("MainApplication", "Navigating to lobby: $lobbyId")
            }
            players.add(PlayerInfo(
                playerId = playerId,
                username = "username",
                colorHex = color!!,
                isHost = true,
                isReady = false
            ))

        } else {
            Log.w("MainApplication", "Received lobby creation for wrong lobby.")
        }

    }

    override fun onPlayerJoined(
        lobbyId: String,
        playerId: String,
        username: String?,
        color: String?
    ) {
        Log.d("MainApplication", "Callback: onPlayerJoined. Player ID: $playerId with username: $username, color: $color")
        players.add(PlayerInfo(
            playerId = playerId,
            colorHex = color!!,
            username = "NewPlayer",
            isHost = false,
            isReady = false))
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

    fun onGameWon(winner: PlayerInfo, leaderboard: List<PlayerInfo>) {
        applicationScope.launch {
            _gameWonState.value = winner to leaderboard
        }
    }

    override fun onDiceResult(dice1: Int, dice2: Int) {
        Log.d("MainApplication", "Callback: onDiceResult. Dice1: $dice1, Dice2: $dice2")
        applicationScope.launch {
            gameViewModel?.updateDiceResult(dice1, dice2)
        }
    }

    override fun onError(error: Throwable) {
        Log.e("MainApplication", "Callback: onError. Error: ${error.message}", error)
        applicationScope.launch {
            _errorChannel.send(error.message ?: "Unknown error occurred")
        }
    }

    override fun onClosed(code: Int, reason: String) {
        Log.w("MainApplication", "Callback: onClosed. Code=$code, Reason=$reason")
        clearGameData()
    }

    override fun onPlayerResourcesReceived(resources: Map<TileType, Int>) {
        Log.d("MainApplication", "Callback: onPlayerResourcesReceived. Resources: $resources")
        applicationScope.launch {
            gameViewModel?.let {
                it.updatePlayerResources(resources)
                Log.d("MainApplication", "Successfully called updatePlayerResources on ViewModel.")
            } ?: Log.w("MainApplication", "gameViewModel was null — skipping update.")
        }
    }

    override fun onLobbyUpdated(
        lobbyId: String,
        playerId: String,
        username: String?,
        color: String?
    ) {
        TODO("Not yet implemented")
    }


}