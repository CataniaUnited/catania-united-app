package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.ws.WebSocketListenerImpl
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.WebSocket
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class WebSocketListenerImplInstrumentedTest {

    private lateinit var mainApplication: MainApplication
    private lateinit var webSocketListener: WebSocketListenerImpl
    private val mockWebSocket = mock(WebSocket::class.java)
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }


    private val dummyOnLobbyCreated: (String) -> Unit = { _ -> }
    private val dummyOnGameBoardReceived: (String, String) -> Unit = { _, _ -> }
    private val dummyOnError: (Throwable) -> Unit = { e -> println("Parameterized Test onError: ${e.message}") }
    private val dummyOnClosed: (Int, String) -> Unit = { _, _ -> }
    private val dummyOnDiceResult: (Int, Int) -> Unit = { _, _ -> }

    @Before
    fun setup() {
        mainApplication = ApplicationProvider.getApplicationContext()

        webSocketListener = WebSocketListenerImpl(
            onConnectionSuccess = { _ -> },
            onLobbyCreated = { _ -> },
            onGameBoardReceived = { _, _ -> },
            onError = { _ -> },
            onClosed = { _, _ -> },
            onDiceResult = { _, _ -> }
        )
    }

    @Test
    fun onSuccessfulConnectionShouldSetPlayerId() {
        val expectedPlayerId = "1234567890"
        val messagePayload = buildJsonObject {
            put("playerId", expectedPlayerId)
        }
        val messageDTO = MessageDTO(
            type = MessageType.CONNECTION_SUCCESSFUL,
            message = messagePayload
        )
        val messageJsonString = jsonParser.encodeToString(MessageDTO.serializer(), messageDTO)

        var receivedPlayerIdByCallback: String? = null
        val specificTestListener = WebSocketListenerImpl(
            onConnectionSuccess = { playerId ->
                println("Test specific listener: onConnectionSuccess called with $playerId")
                mainApplication.setPlayerId(playerId)
                receivedPlayerIdByCallback = playerId
            },
            onLobbyCreated = dummyOnLobbyCreated,
            onGameBoardReceived = dummyOnGameBoardReceived,
            onError = dummyOnError,
            onClosed = dummyOnClosed,
            onDiceResult = dummyOnDiceResult
        )

        specificTestListener.onMessage(mockWebSocket, messageJsonString)


        assertEquals("Callback did not receive correct player ID", expectedPlayerId, receivedPlayerIdByCallback)
        assertEquals("MainApplication Player ID was not set correctly", expectedPlayerId, MainApplication.getInstance().getPlayerId())
    }
    @Test
    fun onClosingDoesNotThrow() {
        webSocketListener.onClosing(mockWebSocket, 1000, "Closed")
    }

    @Test
    fun onLobbyCreatedCallbackIsInvoked() {
        var receivedLobbyId: String? = null
        val expectedLobbyId = "lobbyABC"
        val testListener = WebSocketListenerImpl(
            onConnectionSuccess = { _ -> },
            onLobbyCreated = { lobbyId -> receivedLobbyId = lobbyId },
            onGameBoardReceived = { _, _ -> },
            onError = { e -> fail("onError called: ${e.message}") },
            onClosed = { _, _ -> },
            onDiceResult = { _, _ -> }
        )
        val messageDTO = MessageDTO(type = MessageType.LOBBY_CREATED, lobbyId = expectedLobbyId)
        val messageJson = jsonParser.encodeToString(MessageDTO.serializer(), messageDTO)

        testListener.onMessage(mockWebSocket, messageJson)

        assertEquals("onLobbyCreated callback was not invoked correctly", expectedLobbyId, receivedLobbyId)
    }

    @Test
    fun onGameBoardReceivedCallbackIsInvoked() {
        var receivedLobbyId: String? = null
        var receivedBoardJson: String? = null
        val expectedLobbyId = "lobbyXYZ"
        val boardPayload = buildJsonObject {
            put("tiles", buildJsonArray { /* ... add tile objects ... */ })
            put("settlementPositions", buildJsonArray { /* ... */ })
            put("ringsOfBoard", 3)
        }
        val messageDTO = MessageDTO(
            type = MessageType.GAME_BOARD_JSON,
            lobbyId = expectedLobbyId,
            message = boardPayload
        )
        val messageJson = jsonParser.encodeToString(MessageDTO.serializer(), messageDTO)
        val expectedBoardJson = jsonParser.encodeToString(JsonObject.serializer(), boardPayload)

        val testListener = WebSocketListenerImpl(
            onConnectionSuccess = { _ -> },
            onLobbyCreated = { _ -> },
            onGameBoardReceived = { lobbyId, boardJson ->
                receivedLobbyId = lobbyId
                receivedBoardJson = boardJson
            },
            onError = { e -> fail("onError called: ${e.message}") },
            onClosed = { _, _ -> },
            onDiceResult = { _, _ -> }
        )

        testListener.onMessage(mockWebSocket, messageJson)

        assertEquals("Lobby ID mismatch in onGameBoardReceived", expectedLobbyId, receivedLobbyId)
        assertEquals("Board JSON mismatch in onGameBoardReceived", expectedBoardJson, receivedBoardJson)
    }

    @Test
    fun onErrorCallbackIsInvokedOnJsonParseError() {
        var receivedError: Throwable? = null
        val testListener = WebSocketListenerImpl(
            onConnectionSuccess = { _ -> fail("onConnectionSuccess called")},
            onLobbyCreated = { _ -> fail("onLobbyCreated called") },
            onGameBoardReceived = { _, _ -> fail("onGameBoardReceived called")},
            onError = { error -> receivedError = error },
            onClosed = { _, _ -> fail("onClosed called") },
            onDiceResult = { _, _ -> fail("onDiceResult called") }
        )
        val invalidJson = "{ type: INVALID JSON"

        testListener.onMessage(mockWebSocket, invalidJson)

        assertNotNull("onError callback was not invoked for invalid JSON", receivedError)
    }

    @Test
    fun onClosedCallbackIsInvoked() {
        var closedCode = -1
        var closedReason = ""
        val expectedCode = 1005
        val expectedReason = "Test Close"

        val testListener = WebSocketListenerImpl(
            onConnectionSuccess = { _ -> },
            onLobbyCreated = { _ -> },
            onGameBoardReceived = { _, _ -> },
            onError = { e -> fail("onError called: ${e.message}") },
            onClosed = { code, reason ->
                closedCode = code
                closedReason = reason
            },
            onDiceResult = { _, _ -> }
        )

        testListener.onClosed(mockWebSocket, expectedCode, expectedReason)

        assertEquals("Close code mismatch", expectedCode, closedCode)
        assertEquals("Close reason mismatch", expectedReason, closedReason)
    }
}