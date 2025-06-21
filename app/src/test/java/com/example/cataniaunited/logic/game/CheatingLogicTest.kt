package com.example.cataniaunited.logic.game

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.ws.WebSocketClient
import kotlinx.serialization.json.jsonPrimitive

class CheatingLogicTest {
    private lateinit var cheatingLogic: CheatingLogic
    private lateinit var mockSessionManager: PlayerSessionManager
    private lateinit var mockWsClient: WebSocketClient

    @BeforeEach
    fun setUp() {
        mockSessionManager = mockk(relaxed = true)
        mockWsClient = mockk(relaxed = true)
        mockkObject(MainApplication)
        every { MainApplication.getInstance() } returns mockk {
            every { getWebSocketClient() } returns mockWsClient
        }
        every { mockSessionManager.getPlayerId() } returns "cheater"
        every { mockWsClient.isConnected() } returns true

        cheatingLogic = CheatingLogic(mockSessionManager)
    }

    @Test
    fun sendCheatAttemptSendsCorrectMessageIfWebSocketConnected() {
        cheatingLogic.sendCheatAttempt(TileType.ORE, "lobby42")
        val slot = slot<MessageDTO>()
        verify { mockWsClient.sendMessage(capture(slot)) }
        val sent = slot.captured
        assert(sent.type == MessageType.CHEAT_ATTEMPT)
        assert(sent.player == "cheater")
        assert(sent.lobbyId == "lobby42")
        assert(sent.message?.get("resource")?.jsonPrimitive?.content == "ORE")
    }

    @Test
    fun sendCheatAttemptDoesNotSendIfWebSocketNotConnected() {
        every { mockWsClient.isConnected() } returns false
        cheatingLogic.sendCheatAttempt(TileType.ORE, "lobby42")
        verify(exactly = 0) { mockWsClient.sendMessage(any()) }
    }
}
