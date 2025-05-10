package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
import com.example.cataniaunited.logic.game.GameDataHandler
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.callback.OnDiceResult
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnWebSocketClosed
import com.example.cataniaunited.ws.callback.OnWebSocketError
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.WebSocket
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class WebSocketListenerImplParameterizedTest {

    private lateinit var mainApplication: MainApplication
    private lateinit var webSocketListener: WebSocketListenerImpl
    private lateinit var mockWebSocket: WebSocket
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    private lateinit var mockLobbyCreated: OnLobbyCreated
    private lateinit var mockGameBoardReceived: OnGameBoardReceived
    private lateinit var mockOnDiceResult: OnDiceResult
    private lateinit var mockError: OnWebSocketError
    private lateinit var mockClosed: OnWebSocketClosed
    private lateinit var mockGameDataHandler: GameDataHandler

    @Before
    fun setup() {
        println("Setting up Parameterized Test...")
        mainApplication = ApplicationProvider.getApplicationContext()
        mainApplication.setPlayerId("initial_test_value_${System.currentTimeMillis()}")

        mockLobbyCreated = mockk(relaxed = true)
        mockGameBoardReceived = mockk(relaxed = true)
        mockOnDiceResult = mockk(relaxed = true)
        mockError = mockk(relaxed = true)
        mockClosed = mockk(relaxed = true)
        mockGameDataHandler = mockk(relaxed = true)
        mockWebSocket = mockk(relaxed = true)

        webSocketListener = WebSocketListenerImpl(
            onConnectionSuccess = { playerId ->
                println("Parameterized Test: onConnectionSuccess called with $playerId")
                mainApplication.setPlayerId(playerId)
            },
            onLobbyCreated = mockLobbyCreated,
            onGameBoardReceived = mockGameBoardReceived,
            onError = mockError,
            onClosed = mockClosed,
            onDiceResult = mockOnDiceResult,
            gameDataHandler = mockGameDataHandler
        )
        println("Parameterized Test Setup Complete.")
    }

    companion object {
        @JvmStatic
        @Parameters(name = "Test with type={0}, shouldProcess={1}")
        fun data() = listOf(
            arrayOf(MessageType.CONNECTION_SUCCESSFUL, true),
            arrayOf(MessageType.LOBBY_UPDATED, false),
            arrayOf(MessageType.LOBBY_CREATED, false),
            arrayOf(MessageType.GAME_BOARD_JSON, false),
            arrayOf(MessageType.PLACE_ROAD, false),
            arrayOf(MessageType.PLACE_SETTLEMENT, false),
        )
    }

    @Parameter(0)
    @JvmField
    var messageType: MessageType? = null

    @Parameter(1)
    @JvmField
    var shouldSetPlayerId: Boolean = false

    @Test
    fun testDifferentMessageTypesForConnectionSuccess() {
        val actualMessageType = messageType ?: return

        val expectedPlayerId = "player-${System.currentTimeMillis()}"
        mainApplication.getPlayerId()
        val messagePayload = buildJsonObject {
            if (actualMessageType == MessageType.CONNECTION_SUCCESSFUL) {
                put("playerId", expectedPlayerId)
            } else {
                put("info", "some other data")
            }
        }
        val messageDTO = MessageDTO(
            type = actualMessageType,
            player = null,
            lobbyId = null,
            players = null,
            message = messagePayload
        )

        val messageJsonString = jsonParser.encodeToString(MessageDTO.serializer(), messageDTO)
        println("Testing onMessage with: $messageJsonString")

        webSocketListener.onMessage(mockWebSocket, messageJsonString)

        val finalPlayerId = mainApplication.getPlayerId()

        if (shouldSetPlayerId) {
            assertEquals("Player ID should have been set for CONNECTION_SUCCESSFUL", expectedPlayerId, finalPlayerId)
            println("Test PASSED for ${actualMessageType}: Player ID was set correctly.")
        } else {
            assertNotEquals("Player ID should NOT have been set to the test ID for ${actualMessageType}", expectedPlayerId, finalPlayerId)
            println("Test PASSED for ${actualMessageType}: Player ID was not set by onConnectionSuccess.")
        }
    }
}