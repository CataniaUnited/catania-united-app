package com.example.cataniaunited.logic.game

import android.util.Log
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
import org.junit.jupiter.api.AfterEach

class CheatingLogicTest {
    private lateinit var cheatingLogic: CheatingLogic
    private lateinit var mockSessionManager: PlayerSessionManager
    private lateinit var mockWsClient: WebSocketClient

    @BeforeEach
    fun setUp() {
        mockSessionManager = mockk(relaxed = true)
        mockWsClient = mockk(relaxed = true)
        mockkObject(MainApplication)
        mockkStatic(Log::class)
        every { MainApplication.getInstance() } returns mockk {
            every { getWebSocketClient() } returns mockWsClient
        }
        every { mockSessionManager.getPlayerId() } returns "cheater"
        every { mockWsClient.isConnected() } returns true

        cheatingLogic = CheatingLogic(mockSessionManager)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
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

    @Test
    fun sendCheatAttemptDoesNothingIfPlayerIdThrows() {
        every { mockSessionManager.getPlayerId() } throws IllegalStateException("Player ID missing")

        cheatingLogic.sendCheatAttempt(TileType.ORE, "lobby42")

        verify(exactly = 0) { mockWsClient.sendMessage(any()) }
        verify {
            Log.e(
                eq("CheatingLogic"),
                match { it.contains("Error when fetching player id") },
                any<IllegalStateException>()
            )
        }
    }

    @Test
    fun sendReportPlayerSendsCorrectMessageIfWebSocketConnected() {
        every { mockSessionManager.getPlayerId() } returns "reporter123"

        cheatingLogic.sendReportPlayer(reportedId = "reported456", lobbyId = "lobby42")

        val slot = slot<MessageDTO>()
        verify { mockWsClient.sendMessage(capture(slot)) }

        val sent = slot.captured
        assert(sent.type == MessageType.REPORT_PLAYER)
        assert(sent.player == "reporter123")
        assert(sent.lobbyId == "lobby42")
        assert(sent.message?.get("reportedId")?.jsonPrimitive?.content == "reported456")
    }

    @Test
    fun sendReportPlayerDoesNotSendIfWebSocketNotConnected() {
        every { mockWsClient.isConnected() } returns false
        every { mockSessionManager.getPlayerId() } returns "reporter123"

        cheatingLogic.sendReportPlayer(reportedId = "reported456", lobbyId = "lobby42")

        verify(exactly = 0) { mockWsClient.sendMessage(any()) }
    }

    @Test
    fun sendReportPlayerDoesNothingIfPlayerIdThrows() {
        every { mockSessionManager.getPlayerId() } throws IllegalStateException("No player ID")

        cheatingLogic.sendReportPlayer(reportedId = "reported456", lobbyId = "lobby42")

        verify(exactly = 0) { mockWsClient.sendMessage(any()) }

        verify {
            Log.e(
                eq("CheatingLogic"),
                match { it.contains("Failed to get player ID for report") },
                any<IllegalStateException>()
            )
        }
    }







}
