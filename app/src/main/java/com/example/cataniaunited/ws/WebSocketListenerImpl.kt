package com.example.cataniaunited.ws

import android.util.Log
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

open class WebSocketListenerImpl(
    private val onConnectionSuccess: (playerId: String) -> Unit,
    private val onGameBoardReceived: (lobbyId: String, boardJson: String) -> Unit,
    private val onError: (error: Throwable) -> Unit,
    private val onClosed: (code: Int, reason: String) -> Unit,
    private val onLobbyCreated: (lobbyId: String) -> Unit,
) : WebSocketListener() {

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

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
                MessageType.GAME_BOARD_JSON -> handleGameBoardJson(messageDTO)
                MessageType.LOBBY_CREATED -> handleLobbyCreated(messageDTO)
                // TODO: Other Messages

                MessageType.ERROR -> {
                    Log.e("WebSocketListener", "Received ERROR message from server: ${messageDTO.message}")
                }
                else -> Log.w("WebSocketListener", "Received unhandled message type: ${messageDTO.type}")
            }
        } catch (e: Exception) {
            Log.e("WebSocketListener", "Error parsing or handling message: $text", e)
            onError(e)
        }
    }

    private fun handleConnectionSuccessful(messageDTO: MessageDTO) {
        val playerId = messageDTO.message?.get("playerId")?.jsonPrimitive?.contentOrNull
        if (playerId != null) {
            Log.d("WebSocketListener", "Extracted playerId: $playerId")
            onConnectionSuccess(playerId) // Use callback
        } else {
            Log.e("WebSocketListener", "CONNECTION_SUCCESSFUL message missing 'playerId': ${messageDTO.message}")
            onError(IllegalArgumentException("Missing playerId")) // Use callback
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
                onGameBoardReceived(lobbyId, boardJsonString) // Use callback
            } catch (e: Exception) {
                Log.e("WebSocketListener", "Error converting board message JsonObject to String", e)
                onError(e) // Use callback
            }
        } else {
            Log.e("WebSocketListener", "GAME_BOARD_JSON missing lobbyId ('${lobbyId}') or message object ('${boardJsonObject}')")
            onError(IllegalArgumentException("Invalid GAME_BOARD_JSON format"))
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.i("WebSocketListener", "Closing: Code=$code, Reason=$reason")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i("WebSocketListener", "Closed: Code=$code, Reason=$reason")
        onClosed(code, reason) // Use callback
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        val responseMsg = response?.message ?: "No response"
        Log.e("WebSocketListener", "Failure: ${t.message}, Response: $responseMsg", t)
        onError(t) // Use callback
    }

    private fun handleLobbyCreated(messageDTO: MessageDTO) {
        val lobbyId = messageDTO.lobbyId
        if (lobbyId != null) {
            Log.i("WebSocketListener", "Lobby Created successfully with ID: $lobbyId")
            onLobbyCreated(lobbyId)
        } else {
            Log.e("WebSocketListener", "LOBBY_CREATED message received without lobbyId.")
            onError(IllegalArgumentException("Missing lobbyId in LOBBY_CREATED message"))
        }
    }
}