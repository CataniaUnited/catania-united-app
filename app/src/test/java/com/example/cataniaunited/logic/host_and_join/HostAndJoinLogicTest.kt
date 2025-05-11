package com.example.cataniaunited.logic.host_and_join

import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.ws.WebSocketClient
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

class HostAndJoinLogicTest {

    private lateinit var mockMainApplication: MainApplication
    private lateinit var mockWebSocketClient: WebSocketClient
    private lateinit var mockPlayerSessionManager: PlayerSessionManager

    private lateinit var hostJoinLogic: HostJoinLogic

    private val testPlayerId = "test-player-123"
    private val testLobbyId = "test-lobby-456"

    @BeforeEach
    fun setUp() {
        mockMainApplication = mockk(relaxed = true)
        mockWebSocketClient = mockk(relaxed = true)
        mockPlayerSessionManager = mockk(relaxed = true)
        hostJoinLogic = HostJoinLogic(mockPlayerSessionManager)
        mockkObject(MainApplication.Companion)
        every { mockPlayerSessionManager.getPlayerId() } returns testPlayerId
        every { MainApplication.Companion.getInstance() } returns mockMainApplication
        every { mockMainApplication.getPlayerId() } returns testPlayerId
        every { mockMainApplication.getWebSocketClient() } returns mockWebSocketClient
        every { mockWebSocketClient.isConnected() } returns true
        every { mockWebSocketClient.sendMessage(any()) } returns true
    }

    @Test
    fun sendCreateLobby_whenPlayerIdAvailableAndConnected_sendsCreateLobbyMessage() {
        every { mockWebSocketClient.isConnected() } returns true

        hostJoinLogic.sendCreateLobby()

        verify { mockWebSocketClient.isConnected() }
        val expectedMessage = MessageDTO(MessageType.CREATE_LOBBY, testPlayerId, null, null, null)
        verify { mockWebSocketClient.sendMessage(eq(expectedMessage)) }
    }

    @Test
    fun sendCreateLobby_whenPlayerIdMissing_doesNotAttemptToSend() {
        every { mockPlayerSessionManager.getPlayerId() } throws IllegalStateException("Player ID not set")
        hostJoinLogic.sendCreateLobby()

        verify { mockPlayerSessionManager.getPlayerId() }
        verify { mockWebSocketClient wasNot Called }
    }

    @Test
    fun sendCreateLobby_whenNotConnected_doesNotAttemptToSend() {
        every { mockWebSocketClient.isConnected() } returns false
        hostJoinLogic.sendCreateLobby()
        verify { mockPlayerSessionManager.getPlayerId() }
        verify { mockWebSocketClient.isConnected() }
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun sendJoinLobby_whenPlayerIdAvailableAndConnected_sendsJoinLobbyMessage() {
        every { mockWebSocketClient.isConnected() } returns true

        hostJoinLogic.sendJoinLobby(testLobbyId)

        verify { mockPlayerSessionManager.getPlayerId() }
        verify { mockWebSocketClient.isConnected() }
        val expectedMessage =
            MessageDTO(MessageType.JOIN_LOBBY, testPlayerId, testLobbyId, null, null)
        verify { mockWebSocketClient.sendMessage(eq(expectedMessage)) }
    }

    @Test
    fun sendJoinLobby_whenPlayerIdMissing_doesNotAttemptToSend() {
        every { mockPlayerSessionManager.getPlayerId() } throws IllegalStateException("Player ID not set")
        hostJoinLogic.sendJoinLobby(testLobbyId)

        verify { mockPlayerSessionManager.getPlayerId() }
        verify { mockWebSocketClient wasNot Called }
    }

    @Test
    fun sendJoinLobby_whenNotConnected_doesNotAttemptToSend() {
        every { mockWebSocketClient.isConnected() } returns false

        hostJoinLogic.sendJoinLobby(testLobbyId)

        verify { mockPlayerSessionManager.getPlayerId() }
        verify { mockWebSocketClient.isConnected() }
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }
}