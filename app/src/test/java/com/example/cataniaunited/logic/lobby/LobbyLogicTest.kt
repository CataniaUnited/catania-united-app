package com.example.cataniaunited.logic.lobby

import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.dto.UsernameRequest
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.ws.WebSocketClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LobbyLogicTest {

    private lateinit var mockMainApplication: MainApplication
    private lateinit var mockWebSocketClient: WebSocketClient
    private lateinit var mockPlayerSessionManager: PlayerSessionManager

    private lateinit var lobbyLogic: LobbyLogic

    private val testPlayerId = "test-player-123"
    private val testLobbyId = "test-lobby-456"

    @BeforeEach
    fun setUp() {
        mockMainApplication = mockk(relaxed = true)
        mockWebSocketClient = mockk(relaxed = true)
        mockPlayerSessionManager = mockk(relaxed = true)
        lobbyLogic = LobbyLogic(mockPlayerSessionManager)
        mockkObject(MainApplication.Companion)

        every { MainApplication.getInstance() } returns mockMainApplication
        every { mockMainApplication.getWebSocketClient() } returns mockWebSocketClient
        every { mockWebSocketClient.isConnected() } returns true
        every { mockWebSocketClient.sendMessage(any()) } returns true

        every { mockPlayerSessionManager.getPlayerId() } returns testPlayerId
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(MainApplication.Companion)
    }

    @Test
    fun toggleReadySendsCorrectMessageWhenConnected() {
        val expectedMessage = MessageDTO(
            type = MessageType.SET_READY,
            player = testPlayerId,
            lobbyId = testLobbyId,
            players = null,
            message = null
        )
        val messageSlot = slot<MessageDTO>()

        lobbyLogic.toggleReady(testLobbyId)

        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun toggleReadyDoesNotSendWhenNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false

        lobbyLogic.toggleReady(testLobbyId)

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun toggleReadyDoesNotSendWhenGetPlayerIdThrows() {
        val exception = IllegalStateException("Player ID not set")
        every { mockPlayerSessionManager.getPlayerId() } throws exception

        lobbyLogic.toggleReady(testLobbyId)

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun leaveLobbySendsCorrectMessageWhenConnected() {
        val expectedMessage = MessageDTO(
            type = MessageType.LEAVE_LOBBY,
            player = testPlayerId,
            lobbyId = testLobbyId,
            players = null,
            message = null
        )
        val messageSlot = slot<MessageDTO>()

        lobbyLogic.leaveLobby(testLobbyId)

        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun leaveLobbyDoesNotSendWhenNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false

        lobbyLogic.leaveLobby(testLobbyId)

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun leaveLobbyDoesNotSendWhenGetPlayerIdThrows() {
        val exception = IllegalStateException("Player ID not set")
        every { mockPlayerSessionManager.getPlayerId() } throws exception

        lobbyLogic.leaveLobby(testLobbyId)

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun startGameSendsCorrectMessageWhenConnected() {
        val expectedMessage = MessageDTO(
            type = MessageType.START_GAME,
            player = testPlayerId,
            lobbyId = testLobbyId,
            players = null,
            message = null
        )
        val messageSlot = slot<MessageDTO>()

        lobbyLogic.startGame(testLobbyId)

        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun startGameDoesNotSendWhenWebSocketNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false

        lobbyLogic.startGame(testLobbyId)

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun startGameHandlesExceptionWhenGettingPlayerId() {
        every { mockPlayerSessionManager.getPlayerId() } throws java.lang.IllegalStateException("Player ID not available")

        lobbyLogic.startGame(testLobbyId)

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun endTurnSendsCorrectMessageWhenConnected() {
        val expectedMessage = MessageDTO(
            type = MessageType.END_TURN,
            player = testPlayerId,
            lobbyId = testLobbyId,
            players = null,
            message = null
        )
        val messageSlot = slot<MessageDTO>()

        lobbyLogic.endTurn(testLobbyId)

        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun endTurnDoesNotSendWhenNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false

        lobbyLogic.endTurn(testLobbyId)

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun endTurnDoesNotSendWhenGetPlayerIdThrows() {
        val exception = IllegalStateException("Player ID not set for endTurn")
        every { mockPlayerSessionManager.getPlayerId() } throws exception

        lobbyLogic.endTurn(testLobbyId)

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun setUsernameSendsCorrectMessageWhenConnected() {
        val username = "new_username"
        val expectedUsernameRequest = UsernameRequest(username)
        val expectedMessage = MessageDTO(
            type = MessageType.SET_USERNAME,
            player = testPlayerId,
            lobbyId = testLobbyId,
            players = null,
            message = Json.encodeToJsonElement(expectedUsernameRequest).jsonObject
        )
        val messageSlot = slot<MessageDTO>()

        lobbyLogic.setUsername(testLobbyId, username)

        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun setUsernameDoesNotSendWhenNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false

        lobbyLogic.setUsername(testLobbyId, "new_username")

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun setUsernameDoesNotSendWhenGetPlayerIdThrows() {
        val exception = IllegalStateException("Player ID not set for setUsername")
        every { mockPlayerSessionManager.getPlayerId() } throws exception

        lobbyLogic.setUsername(testLobbyId, "new_username")

        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }
}