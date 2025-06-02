package com.example.cataniaunited.logic.lobby

import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.ws.WebSocketClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
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


}