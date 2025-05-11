package com.example.cataniaunited.logic.game

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import javax.inject.Inject

/**
 * Thin client-side helper that only knows how to fire WebSocket
 * commands.  All game logic lives on the server.
 */
class GameBoardLogic @Inject constructor() {

    private val tag = "GameBoardLogic"

    /* ─────────────────────────────────────────────────────────────── */
    /*  Lobby creation                                                 */
    /* ─────────────────────────────────────────────────────────────── */

    fun requestCreateLobby() {
        val playerId = runCatching { MainApplication.getInstance().getPlayerId() }
            .getOrElse {
                Log.e(tag, "Player-ID not available (CREATE_LOBBY)", it); return
            }

        val ws = MainApplication.getInstance().webSocketClient

        if (!ws.isConnected()) {
            Log.e(tag, "WS not connected – cannot CREATE_LOBBY"); return
        }

        ws.sendMessage(
            MessageDTO(
                type    = MessageType.CREATE_LOBBY,
                player  = playerId,
                lobbyId = null
            )
        )
        Log.i(tag, "CREATE_LOBBY sent by $playerId")
    }

    /* ─────────────────────────────────────────────────────────────── */
    /*  Start Game (host button)                                       */
    /* ─────────────────────────────────────────────────────────────── */

    /**
     * 1️⃣  auto-joins `(playerCount-1)` dummy players so the lobby has enough seats
     * 2️⃣  sends **START_GAME** – the server now does the heavy work
     */
    fun requestStartGame(lobbyId: String, playerCount: Int = 4) {
        val playerId = runCatching { MainApplication.getInstance().getPlayerId() }
            .getOrElse {
                Log.e(tag, "Player-ID not available (START_GAME)", it); return
            }

        val ws = MainApplication.getInstance().webSocketClient

        if (!ws.isConnected()) {
            Log.e(tag, "WS not connected – cannot START_GAME"); return
        }

        /* 1 . Auto-join dummy players so lobby has [playerCount] seats */
        repeat(playerCount - 1) {
            ws.sendMessage(
                MessageDTO(
                    type    = MessageType.JOIN_LOBBY,
                    player  = UUID.randomUUID().toString(),
                    lobbyId = lobbyId
                )
            )
        }

        /* 2 . Fire the real START_GAME */
        ws.sendMessage(
            MessageDTO(
                type    = MessageType.START_GAME,
                player  = playerId,
                lobbyId = lobbyId
            )
        )
        Log.i(tag, "START_GAME sent (total=$playerCount) for lobby $lobbyId")
    }

    /* ─────────────────────────────────────────────────────────────── */
    /*  In-game actions                                                */
    /* ─────────────────────────────────────────────────────────────── */

    fun placeSettlement(posId: Int, lobbyId: String) {
        sendBoardAction(
            type   = MessageType.PLACE_SETTLEMENT,
            lobby  = lobbyId
        ) { put("settlementPositionId", posId) }
    }

    fun placeRoad(roadId: Int, lobbyId: String) {
        sendBoardAction(
            type   = MessageType.PLACE_ROAD,
            lobby  = lobbyId
        ) { put("roadId", roadId) }
    }

    /* Helper – builds small JSON payloads and fires over WS */
    private inline fun sendBoardAction(
        type: MessageType,
        lobby: String,
        payloadBuilder: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit
    ) {
        val playerId = runCatching { MainApplication.getInstance().getPlayerId() }
            .getOrElse { Log.e(tag, "Player-ID not available ($type)", it); return }

        val ws = MainApplication.getInstance().webSocketClient

        if (!ws.isConnected()) {
            Log.e(tag, "WS not connected – cannot send $type"); return
        }

        val payload = buildJsonObject(payloadBuilder)

        ws.sendMessage(
            MessageDTO(
                type    = type,
                player  = playerId,
                lobbyId = lobby,
                message = payload
            )
        )
        Log.d(tag, "$type sent in lobby $lobby with payload $payload")
    }
}
