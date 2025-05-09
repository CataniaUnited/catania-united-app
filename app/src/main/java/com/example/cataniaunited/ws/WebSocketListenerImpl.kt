package com.example.cataniaunited.ws

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.data.GameDataHandler
import com.example.cataniaunited.exception.GameException
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnWebSocketClosed
import com.example.cataniaunited.ws.callback.OnWebSocketError
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

open class WebSocketListenerImpl @Inject constructor(
    private val onConnectionSuccess: OnConnectionSuccess,
    private val onLobbyCreated: OnLobbyCreated,
    private val onGameBoardReceived: OnGameBoardReceived,
    private val onError: OnWebSocketError,
    private val onClosed: OnWebSocketClosed,
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
            Log.d("WebSocketListener", "Parsed: Type=${messageDTO.type}, Lobby=${messageDTO.lobbyId}, MsgObj=${messageDTO.message}")

            when (messageDTO.type) {
                MessageType.CONNECTION_SUCCESSFUL -> handleConnectionSuccessful(messageDTO)
                MessageType.GAME_BOARD_JSON, MessageType.PLACE_SETTLEMENT, MessageType.PLACE_ROAD -> handleGameBoardJson(messageDTO)
                MessageType.LOBBY_CREATED -> handleLobbyCreated(messageDTO)
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
        val boardJsonObject: JsonObject? = messageDTO.message // The payload is the board object

        if (lobbyId != null && boardJsonObject != null) {
            try {
                // Convert the board JsonObject back to a JSON String
                val boardJsonString = jsonParser.encodeToString(JsonObject.serializer(), boardJsonObject)
                Log.d("WebSocketListener", "Extracted board JSON string for lobby: $lobbyId")
                onGameBoardReceived.onGameBoardReceived(lobbyId, boardJsonString) // Use callback
                MainApplication.getInstance().applicationScope.launch {
                    gameDataHandler.updateGameBoard(boardJsonString)
                }
            } catch (e: Exception) {
                Log.e("WebSocketListener", "Error converting board message JsonObject to String", e)
                onError.onError(e) // Use callback
            }
        } else {
            Log.e("WebSocketListener", "GAME_BOARD_JSON missing lobbyId ('${lobbyId}') or message object ('${boardJsonObject}')")
            onError.onError(IllegalArgumentException("Invalid GAME_BOARD_JSON format"))
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
        if (lobbyId != null) {
            Log.i("WebSocketListener", "Lobby Created successfully with ID: $lobbyId")
            onLobbyCreated.onLobbyCreated(lobbyId)
        } else {
            Log.e("WebSocketListener", "LOBBY_CREATED message received without lobbyId.")
            onError.onError(IllegalArgumentException("Missing lobbyId in LOBBY_CREATED message"))
        }
    }
}