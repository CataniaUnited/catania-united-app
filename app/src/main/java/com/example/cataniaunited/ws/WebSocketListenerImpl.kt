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

/**
 * One listener instance per WebSocket.
 * Translates server DTOs into app-level callbacks.
 */
class WebSocketListenerImpl(
    private val onConnectionSuccess : (playerId: String) -> Unit,
    private val onGameBoardReceived : (lobbyId: String, boardJson: String) -> Unit,
    private val onError             : (Throwable) -> Unit,
    private val onClosedCb          : (code: Int, reason: String) -> Unit,
    private val onLobbyCreated      : (lobbyId: String) -> Unit
) : WebSocketListener() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient         = true
    }


    override fun onOpen(ws: WebSocket, response: Response) {
        Log.d("WS", "Connection opened (${response.code})")
        /* server will immediately push CONNECTION_SUCCESSFUL */
    }

    override fun onMessage(ws: WebSocket, text: String) {
        Log.d("WS", "⬇ $text")
        try {
            val m = json.decodeFromString<MessageDTO>(text)

            when (m.type) {
                /* handshake echo */
                MessageType.CONNECTION_SUCCESSFUL -> {
                    m.message?.get("playerId")
                        ?.jsonPrimitive
                        ?.contentOrNull
                        ?.let(onConnectionSuccess)
                }

                /* NEW: real game start */
                MessageType.START_GAME           -> forwardStartGame(m)

                /* legacy testing path (still works) */
                MessageType.GAME_BOARD_JSON      -> forwardBoardJson(m)

                /* lobby ack */
                MessageType.LOBBY_CREATED        -> m.lobbyId?.let(onLobbyCreated)

                /* server-side error */
                MessageType.ERROR                -> {
                    Log.e("WS", "Server error: ${m.message}")
                }

                /* future types */
                else -> Log.d("WS", "Unhandled type: ${m.type}")
            }
        } catch (t: Throwable) {
            Log.e("WS", "Failed to handle WS message", t)
            onError(t)
        }
    }


    /**
     * The new START_GAME payload looks like:
     * { playerOrder:[…], board:{ … } }
     */
    private fun forwardStartGame(m: MessageDTO) {
        val lobbyId = m.lobbyId ?: return
        val obj     = m.message as? JsonObject ?: return

        // If the board is nested, extract it, otherwise use the whole object
        val boardObj = (obj["board"] ?: obj) as? JsonObject ?: return
        val raw      = json.encodeToString(JsonObject.serializer(), boardObj)

        onGameBoardReceived(lobbyId, raw)
    }

    private fun forwardBoardJson(m: MessageDTO) {
        val lobbyId = m.lobbyId ?: return
        val obj     = m.message  as? JsonObject ?: return
        val raw     = json.encodeToString(JsonObject.serializer(), obj)

        onGameBoardReceived(lobbyId, raw)
    }


    override fun onClosing(ws: WebSocket, code: Int, reason: String) {
        Log.i("WS", "Closing $code / $reason")
    }

    override fun onClosed(ws: WebSocket, code: Int, reason: String) {
        Log.i("WS", "Closed $code / $reason")
        onClosedCb(code, reason)
    }

    override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
        Log.e("WS", "Failure: ${t.message}", t)
        onError(t)
    }
}
