package com.example.cataniaunited.ws

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.data.model.LobbyInfo
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.exception.GameException
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.game.GameDataHandler
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnDiceResult
import com.example.cataniaunited.ws.callback.OnDiceRolling
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnLobbyUpdated
import com.example.cataniaunited.ws.callback.OnPlayerJoined
import com.example.cataniaunited.ws.callback.OnPlayerResourcesReceived
import com.example.cataniaunited.ws.callback.OnWebSocketClosed
import com.example.cataniaunited.ws.callback.OnWebSocketError
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

open class WebSocketListenerImpl @Inject constructor(
    private val onConnectionSuccess: OnConnectionSuccess,
    private val onLobbyCreated: OnLobbyCreated,
    private val onPlayerJoined: OnPlayerJoined,
    private val onLobbyUpdated: OnLobbyUpdated,
    private val onGameBoardReceived: OnGameBoardReceived,
    private val onError: OnWebSocketError,
    private val onClosed: OnWebSocketClosed,
    private val onDiceResult: OnDiceResult,
    private val onDiceRolling: OnDiceRolling,
    private val onPlayerResourcesReceived: OnPlayerResourcesReceived,
    private val gameDataHandler: GameDataHandler
) : WebSocketListener() {

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    init {
        Log.d("WebSocketListener", "GameDataHandler hashCode: ${gameDataHandler.hashCode()}")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Opened connection")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocketListener", "Raw Message Received: $text")
        try {
            val messageDTO = jsonParser.decodeFromString<MessageDTO>(text)

            Log.d("WebSocketListener", "Parsed MessageDTO:")
            Log.d("WebSocketListener", "  → Type: ${messageDTO.type}")
            Log.d("WebSocketListener", "  → LobbyId: ${messageDTO.lobbyId}")
            Log.d("WebSocketListener", "  → Player: ${messageDTO.player}")
            Log.d("WebSocketListener", "  → Players: ${messageDTO.players}")
            Log.d("WebSocketListener", "  → Message: ${messageDTO.message}")

            val rootJson = messageDTO.message
            val gameboardNode = rootJson?.get("gameboard")

            Log.d("WebSocketListener", "Extracted 'gameboard' field: $gameboardNode")

            when (messageDTO.type) {
                MessageType.CONNECTION_SUCCESSFUL -> handleConnectionSuccessful(messageDTO)
                MessageType.LOBBY_CREATED -> handleLobbyCreated(messageDTO)
                MessageType.PLAYER_JOINED -> handlePlayerJoined(messageDTO)
                MessageType.LOBBY_UPDATED -> handleLobbyUpdated(messageDTO)
                MessageType.PLAYER_RESOURCE_UPDATE -> handlePlayersUpdate(messageDTO)
                MessageType.GAME_BOARD_JSON,
                MessageType.PLACE_SETTLEMENT,
                MessageType.PLACE_ROAD,
                MessageType.GAME_STARTED,
                MessageType.NEXT_TURN,
                MessageType.UPGRADE_SETTLEMENT -> handleGameBoardJson(messageDTO)

                MessageType.DICE_RESULT -> handleDiceResult(messageDTO)
                MessageType.ROLL_DICE -> handleDiceRolling(messageDTO)
                MessageType.GAME_WON -> handleGameWon(messageDTO)
                MessageType.LOBBY_LIST -> handleLobbyList(messageDTO)

                MessageType.ERROR -> {
                    Log.e(
                        "WebSocketListener",
                        "Received ERROR message from server: ${messageDTO.message}"
                    )
                    onError.onError(GameException(messageDTO.message?.getValue("error").toString()))
                }

                MessageType.ALERT -> {
                    val alertMessage = messageDTO.message?.get("message")?.jsonPrimitive?.contentOrNull
                    val severity = messageDTO.message?.get("severity")?.jsonPrimitive?.contentOrNull

                    if (alertMessage != null) {
                        MainApplication.getInstance().applicationScope.launch {
                            gameDataHandler.showSnackbar(alertMessage, severity ?: "info")
                        }
                    }
                }


                else -> Log.w(
                    "WebSocketListener",
                    "Received unhandled message type: ${messageDTO.type}"
                )
            }
        } catch (e: Exception) {
            Log.e("WebSocketListener", "Error parsing or handling message: $text", e)
            onError.onError(e)
        }
    }

    private fun handleLobbyUpdated(messageDTO: MessageDTO) {
        val lobbyId = messageDTO.lobbyId
        val players = messageDTO.players

        if (lobbyId != null && players != null) {
            onLobbyUpdated.onLobbyUpdated(lobbyId, players)
        }
    }

    private fun handlePlayerJoined(messageDTO: MessageDTO) {
        val lobbyId = messageDTO.lobbyId
        val playerId = messageDTO.player
        val color = messageDTO.message?.get("color")?.jsonPrimitive?.contentOrNull
        val players = messageDTO.players

        if (lobbyId != null && players != null) {
            onPlayerJoined.onPlayerJoined(lobbyId, players)
            Log.i(
                "WebSocketListener",
                "Player '$playerId' joined Lobby '$lobbyId' with color $color"
            )
            // notify UI or GameDataHandler if needed
        } else {
            Log.w("WebSocketListener", "PLAYER_JOINED message missing player or lobbyId")
        }
    }

    private fun handleConnectionSuccessful(messageDTO: MessageDTO) {
        val playerId = messageDTO.message?.get("playerId")?.jsonPrimitive?.contentOrNull
        if (playerId != null) {
            Log.d("WebSocketListener", "Extracted playerId: $playerId")
            onConnectionSuccess.onConnectionSuccess(playerId) // Use callback
        } else {
            Log.e(
                "WebSocketListener",
                "CONNECTION_SUCCESSFUL message missing 'playerId': ${messageDTO.message}"
            )
            onError.onError(IllegalArgumentException("Missing playerId")) // Use callback
        }
    }

    private fun handleGameBoardJson(messageDTO: MessageDTO) {
        val lobbyId = messageDTO.lobbyId
        val players = messageDTO.players!!

        val message = messageDTO.message ?: run {
            Log.e("WebSocketListener", "Message is null")
            return
        }

        try {
            val fullMessageString = jsonParser.encodeToString(JsonObject.serializer(), message)
            message["gameboard"]?.jsonObject ?: message
            val vpMap = mutableMapOf<String, Int>()
            for ((playerId, playerInfo) in players) {
                vpMap[playerId] = playerInfo.victoryPoints
            }
            Log.d("WebSocketListener", "Parsed VP map: $vpMap")
            MainApplication.getInstance().applicationScope.launch {
                gameDataHandler.updateGameBoard(fullMessageString)
                gameDataHandler.updateVictoryPoints(vpMap)
                gameDataHandler.updatePlayers(players)
            }

            onPlayerResourcesReceived.onPlayerResourcesReceived(players)
            onGameBoardReceived.onGameBoardReceived(lobbyId ?: "", fullMessageString)

        } catch (e: Exception) {
            Log.e("WebSocketListener", "Error processing game board", e)
            onError.onError(e)
        }
    }

    private fun handleGameWon(messageDTO: MessageDTO) {
        try {
            val winnerId = messageDTO.message?.get("winner")?.jsonPrimitive?.contentOrNull
            val leaderboard = messageDTO.message?.get("leaderboard")?.jsonArray

            if (winnerId != null && leaderboard != null) {
                val players: List<PlayerInfo> = jsonParser.decodeFromString(leaderboard.toString())
                MainApplication.getInstance().applicationScope.launch {
                    MainApplication.getInstance().onGameWon(players.first(), players)
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocketListener", "Error processing GAME_WON message", e)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.i("WebSocketListener", "Closing: Code=$code, Reason=$reason")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i("WebSocketListener", "Closed: Code=$code, Reason=$reason")
        onClosed.onClosed(code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        val responseMsg = response?.message ?: "No response"
        Log.e("WebSocketListener", "Failure: ${t.message}, Response: $responseMsg", t)
        onError.onError(t) // Use callback
    }

    private fun handleLobbyCreated(messageDTO: MessageDTO) {
        val lobbyId = messageDTO.lobbyId
        val players = messageDTO.players

        if (lobbyId != null && players != null) {
            onLobbyCreated.onLobbyCreated(lobbyId, players)
            Log.i("WebSocketListener", "Lobby Created successfully with ID: $lobbyId")
        } else {
            Log.e("WebSocketListener", "LOBBY_CREATED message received without lobbyId.")
            onError.onError(IllegalArgumentException("Missing lobbyId in LOBBY_CREATED message"))
        }
    }

    internal fun handleDiceRolling(messageDTO: MessageDTO) {
        val playerName = messageDTO.message?.get("rollingUsername")?.jsonPrimitive?.contentOrNull
            ?: messageDTO.players?.get(messageDTO.player)?.username
            ?: "Player"
        val playerId = messageDTO.player ?: ""

        Log.d("WebSocketListener", "Dice rolling for: $playerName (ID: $playerId)")
        onDiceRolling.onDiceRolling(playerName)
    }

    internal fun handleDiceResult(messageDTO: MessageDTO) {
        val dice1 = messageDTO.message?.get("dice1")?.jsonPrimitive?.intOrNull ?: 0
        val dice2 = messageDTO.message?.get("dice2")?.jsonPrimitive?.intOrNull ?: 0
        val playerName = messageDTO.message?.get("rollingUsername")?.jsonPrimitive?.contentOrNull
            ?: messageDTO.players?.get(messageDTO.player)?.username
            ?: "Unknown Player"

        Log.d("WebSocketListener", "Dice result for $playerName: $dice1, $dice2")

        messageDTO.players?.let { players ->
            MainApplication.getInstance().applicationScope.launch {
                gameDataHandler.updatePlayers(players)
            }
            onPlayerResourcesReceived.onPlayerResourcesReceived(players)
        }
        onDiceResult.onDiceResult(dice1, dice2, playerName)
    }
    private fun handlePlayersUpdate(messageDTO: MessageDTO) {
        val lobbyId = messageDTO.lobbyId
        val players = messageDTO.players

        if (lobbyId != null && players != null) {
            MainApplication.getInstance().applicationScope.launch {
                gameDataHandler.updatePlayers(players)
            }
            when (messageDTO.type) {
                MessageType.PLAYER_JOINED -> onPlayerJoined.onPlayerJoined(lobbyId, players)
                MessageType.LOBBY_UPDATED -> onLobbyUpdated.onLobbyUpdated(lobbyId, players)
                MessageType.PLAYER_RESOURCE_UPDATE -> onPlayerResourcesReceived.onPlayerResourcesReceived(players)
                else -> {} // Should not happen
            }
        } else {
            Log.w("WebSocketListener", "Player update message missing lobbyId or players map")
        }
    }

    private fun handleLobbyList(messageDTO: MessageDTO){
        try {
            val lobbies = messageDTO.message?.get("lobbies")?.jsonArray

            if (lobbies != null) {
                val lobbies: List<LobbyInfo> = jsonParser.decodeFromString(lobbies.toString())
                MainApplication.getInstance().applicationScope.launch {
                    MainApplication.getInstance().onLobbyListReceived(lobbies)
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocketListener", "Error processing GAME_WON message", e)
        }
    }
}