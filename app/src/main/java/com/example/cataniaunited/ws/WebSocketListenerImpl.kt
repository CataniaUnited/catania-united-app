package com.example.cataniaunited.ws

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.logic.game.GameDataHandler
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.exception.GameException
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnDiceResult
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
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
    private val onGameBoardReceived: OnGameBoardReceived,
    private val onError: OnWebSocketError,
    private val onClosed: OnWebSocketClosed,
    private val onDiceResult: OnDiceResult,
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
            Log.d("WebSocketListener", "  → Message: ${messageDTO.message}")


            val rootJson = messageDTO.message
            val gameboardNode = rootJson?.get("gameboard")
            val playersNode = rootJson?.get("players")

            Log.d("WebSocketListener", "Extracted 'gameboard' field: $gameboardNode")
            Log.d("WebSocketListener", "Extracted 'players' field: $playersNode")


            when (messageDTO.type) {
                MessageType.CONNECTION_SUCCESSFUL -> handleConnectionSuccessful(messageDTO)
                MessageType.GAME_BOARD_JSON,
                MessageType.PLACE_SETTLEMENT,
                MessageType.PLACE_ROAD,
                MessageType.UPGRADE_SETTLEMENT -> handleGameBoardJson(messageDTO)
                MessageType.LOBBY_CREATED -> handleLobbyCreated(messageDTO)
                MessageType.DICE_RESULT -> handleDiceResult(messageDTO)
                MessageType.PLAYER_JOINED -> handlePlayerJoined(messageDTO)
                MessageType.GAME_WON -> handleGameWon(messageDTO)
                MessageType.PLAYER_RESOURCES -> handlePlayerResources(messageDTO)
                // TODO: Other Messages

                MessageType.ERROR -> {
                    Log.e("WebSocketListener", "Received ERROR message from server: ${messageDTO.message}")
                    onError.onError(GameException(messageDTO.message?.getValue("error").toString()))
                }
                else -> Log.w("WebSocketListener", "Received unhandled message type: ${messageDTO.type}")
            }
        } catch (e: Exception) {
            Log.e("WebSocketListener", "Error parsing or handling message: $text", e)
            onError.onError(e)
        }
    }

    private fun handlePlayerJoined(messageDTO: MessageDTO) {
        val playerId = messageDTO.player
        val lobbyId = messageDTO.lobbyId
        val color = messageDTO.message?.get("color")?.jsonPrimitive?.contentOrNull

        if (playerId != null && lobbyId != null) {
            onPlayerJoined.onPlayerJoined(lobbyId, playerId, color)
            Log.i(
                "WebSocketListener",
                "Player '$playerId' joined Lobby '$lobbyId' with color $color"
            )
            // notify UI or GameDataHandler if needed
        } else {
            Log.w("WebSocketListener", "PLAYER_JOINED message missing player or lobbyId")
        }
    }

    private fun handlePlayerResources(messageDTO: MessageDTO) {
        val resourcesJson = messageDTO.message
        Log.d("WebSocketListener", "RAW PLAYER_RESOURCES JSON: $resourcesJson")
        if (resourcesJson != null) {
            try {
                val typedResources = mutableMapOf<TileType, Int>()
                Log.d("DebugEnum", "TileType from handlePlayerResources: ${TileType.WOOD::class.java.name}")
                Log.d("DebugEnum", "Incoming JSON keys: ${resourcesJson.keys}")
                TileType.entries.forEach { tileType ->
                    if (tileType != TileType.WASTE) {
                        val count = resourcesJson[tileType.name.uppercase()]?.jsonPrimitive?.intOrNull ?: 0
                        typedResources[tileType] = count
                    }
                }
                Log.d("WebSocketListener", "Parsed Player Resources: $typedResources")
                onPlayerResourcesReceived.onPlayerResourcesReceived(typedResources)
            } catch (e: Exception) {
                Log.e("WebSocketListener", "Error parsing player resources from: $resourcesJson", e)
                onError.onError(IllegalArgumentException("Invalid PLAYER_RESOURCES format", e))
            }
        } else {
            Log.e("WebSocketListener", "PLAYER_RESOURCES message missing resource object")
            onError.onError(IllegalArgumentException("PLAYER_RESOURCES message missing resource object"))

        }
    }

    private fun handleConnectionSuccessful(messageDTO: MessageDTO) {
        val playerId = messageDTO.message?.get("playerId")?.jsonPrimitive?.contentOrNull
        if (playerId != null) {
            Log.d("WebSocketListener", "Extracted playerId: $playerId")
            onConnectionSuccess.onConnectionSuccess(playerId) // Use callback
        } else {
            Log.e("WebSocketListener", "CONNECTION_SUCCESSFUL message missing 'playerId': ${messageDTO.message}")
            onError.onError(IllegalArgumentException("Missing playerId")) // Use callback
        }
    }

    private fun handleGameBoardJson(messageDTO: MessageDTO) {
        val lobbyId = messageDTO.lobbyId

        val message = messageDTO.message ?: run {
            Log.e("WebSocketListener", "Message is null")
            return
        }

        try {
            val fullMessageString = jsonParser.encodeToString(JsonObject.serializer(), message)

            message["gameboard"]?.jsonObject ?: message
            val playersNode = message["players"]?.jsonObject

            MainApplication.getInstance().applicationScope.launch {
                gameDataHandler.updateGameBoard(fullMessageString)
            }

            playersNode?.let { playersJson ->
                val vpMap = mutableMapOf<String, Int>()
                for ((playerId, playerNode) in playersJson) {
                    val vp = playerNode.jsonObject["victoryPoints"]?.jsonPrimitive?.intOrNull ?: 0
                    vpMap[playerId] = vp
                }
                Log.d("WebSocketListener", "Parsed VP map: $vpMap")
                MainApplication.getInstance().applicationScope.launch {
                    gameDataHandler.updateVictoryPoints(vpMap)
                }
            }

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
                val players = leaderboard.mapNotNull { entry ->
                    val obj = entry.jsonObject
                    PlayerInfo(
                        playerId = "",
                        username = obj["username"]?.jsonPrimitive?.contentOrNull ?: "",
                        colorHex = "#8C4E27",
                        victoryPoints = obj["vp"]?.jsonPrimitive?.intOrNull ?: 0
                    )
                }

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
        val playerId = messageDTO.player
        val color = messageDTO.message?.get("color")?.jsonPrimitive?.contentOrNull

        if (lobbyId != null) {
            Log.i("WebSocketListener", "Lobby Created successfully with ID: $lobbyId")
            onLobbyCreated.onLobbyCreated(lobbyId, playerId, color)
        } else {
            Log.e("WebSocketListener", "LOBBY_CREATED message received without lobbyId.")
            onError.onError(IllegalArgumentException("Missing lobbyId in LOBBY_CREATED message"))
        }
    }

    internal fun handleDiceResult(messageDTO: MessageDTO) {
        val dice1 = messageDTO.message?.get("dice1")?.jsonPrimitive?.content?.toInt() ?: 0
        val dice2 = messageDTO.message?.get("dice2")?.jsonPrimitive?.content?.toInt() ?: 0

        Log.d("WebSocketListener", "Processing new dice result: $dice1, $dice2")
        onDiceResult.onDiceResult(dice1, dice2)
    }
}