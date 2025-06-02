package com.example.cataniaunited

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnDiceResult
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnLobbyUpdated
import com.example.cataniaunited.ws.callback.OnPlayerJoined
import com.example.cataniaunited.ws.callback.OnPlayerResourcesReceived
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
        _gameWonState.value = null
        this.players.clear()
        this.clearGameData()
        Log.d("MainApplication", "Cleared lobby data.")
    }

    override fun onConnectionSuccess(playerId: String) {
        Log.d("MainApplication", "Callback: onConnectionSuccess. Player ID: $playerId")
        setPlayerId(playerId)
    }

    override fun onLobbyCreated(
        lobbyId: String,
        players: Map<String, PlayerInfo>
    ) {
        Log.i(
            "MainApplication",
            "Callback: onLobbyCreated. Lobby ID: $lobbyId with players: $players"
        )
        if (lobbyId == _currentLobbyIdFlow.value || _currentLobbyIdFlow.value == null) {
            applicationScope.launch {
                _navigateToLobbyChannel.send(lobbyId)
                Log.d("MainApplication", "Navigating to lobby: $lobbyId")
            }
            this.players.clear()
            this.players.addAll(players.values)

        } else {
            Log.w("MainApplication", "Received lobby creation for wrong lobby.")
        }

    }

    override fun onPlayerJoined(
        lobbyId: String,
        players: Map<String, PlayerInfo>
    ) {
        Log.d("MainApplication", "Callback: onPlayerJoined. Players $players")
        updateLobby(lobbyId, players)
    }

    override fun onLobbyUpdated(lobbyId: String, players: Map<String, PlayerInfo>) {
        Log.d("MainApplication", "Callback: onLobbyUpdated. Players $players")
        updateLobby(lobbyId, players)
    }

    private fun updateLobby(lobbyId: String, players: Map<String, PlayerInfo>){
        if(!players.contains(_playerId)){
            Log.d("MainApplication", "Player not part of Lobby $lobbyId, dropping update")
            return
        }

        Log.d("MainApplication", "Lobby update received: lobby = $lobbyId, players = $players")
        if (currentLobbyId == null) {
            currentLobbyId = lobbyId;
        }
        this.players.clear()
        this.players.addAll(players.values)
    }

    override fun onGameBoardReceived(lobbyId: String, boardJson: String) {
        Log.d(
            "MainApplication",
            "Callback: onGameBoardReceived for Lobby $lobbyId. Current lobby id: $currentLobbyId"
        )
        if (latestBoardJson == null && lobbyId == currentLobbyId) {
            applicationScope.launch {
                _navigateToGameChannel.send(lobbyId)
            }
        } else {
            Log.d("MainApplication", "Already in game channel, abort navigation and update boardJson")
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
        clearLobbyData()
    }

    override fun onPlayerResourcesReceived(players: Map<String, PlayerInfo>) {
        Log.d("MainApplication", "Callback: onPlayerResourcesReceived. Players: $players")
        applicationScope.launch {
            gameViewModel?.let {
                val resources: Map<TileType, Int>? = players[_playerId]?.resources;
                if (resources != null) {
                    it.updatePlayerResources(resources)
                    Log.d(
                        "MainApplication",
                        "Successfully called updatePlayerResources on ViewModel."
                    )
                } else {
                    Log.w("MainApplication", "Resources were null — skipping update.")
                }

            } ?: Log.w("MainApplication", "gameViewModel was null — skipping update.")
        }
    }

}