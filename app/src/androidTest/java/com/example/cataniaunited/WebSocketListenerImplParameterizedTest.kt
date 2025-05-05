package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.ws.WebSocketListenerImpl
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
import org.mockito.Mockito.mock

@RunWith(Parameterized::class)
class WebSocketListenerImplParameterizedTest {

    private lateinit var mainApplication: MainApplication
    private lateinit var webSocketListener: WebSocketListenerImpl
    private val mockWebSocket = mock(WebSocket::class.java)
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    private val dummyOnLobbyCreated: (String) -> Unit = { _ -> }
    private val dummyOnGameBoardReceived: (String, String) -> Unit = { _, _ -> }
    private val dummyOnError: (Throwable) -> Unit = { e -> println("Parameterized Test onError: ${e.message}") }
    private val dummyOnClosed: (Int, String) -> Unit = { _, _ -> }

    @Before
    fun setup() {
        println("Setting up Parameterized Test...")
        mainApplication = ApplicationProvider.getApplicationContext()
        mainApplication.setPlayerId("initial_test_value_${System.currentTimeMillis()}")

        webSocketListener = WebSocketListenerImpl(
            onConnectionSuccess = { playerId ->
                println("Parameterized Test: onConnectionSuccess called with $playerId")
                mainApplication.setPlayerId(playerId)
            },
            onLobbyCreated = dummyOnLobbyCreated,
            onGameBoardReceived = dummyOnGameBoardReceived,
            onError = dummyOnError,
            onClosed = dummyOnClosed
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
            arrayOf(MessageType.GAME_BOARD_JSON, false)
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
        val initialPlayerId = mainApplication.getPlayerId()
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