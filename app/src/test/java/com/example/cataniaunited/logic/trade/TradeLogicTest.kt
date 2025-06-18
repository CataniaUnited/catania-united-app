package com.example.cataniaunited.logic.trade


import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.dto.TradeRequest
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.ws.WebSocketClient
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TradeLogicTest {

    private lateinit var mockPlayerSessionManager: PlayerSessionManager
    private lateinit var mockMainApplication: MainApplication
    private lateinit var mockWebSocketClient: WebSocketClient
    private lateinit var tradeLogic: TradeLogic

    private val testPlayerId = "player123"
    private val testLobbyId = "lobby456"

    @BeforeEach
    fun setUp() {
        // Mock static Android Log class
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        // Mock dependencies
        mockPlayerSessionManager = mockk()
        mockWebSocketClient = mockk(relaxed = true)
        mockMainApplication = mockk()

        // Mock the MainApplication singleton and its getter for the WebSocketClient
        mockkObject(MainApplication.Companion)
        every { MainApplication.getInstance() } returns mockMainApplication
        every { mockMainApplication.getWebSocketClient() } returns mockWebSocketClient

        // Initialize the class under test
        tradeLogic = TradeLogic(mockPlayerSessionManager)
    }

    @AfterEach
    fun tearDown() {
        // Clean up all mocks after each test
        unmockkAll()
    }

    @Nested
    @DisplayName("sendBankTrade functionality")
    inner class SendBankTradeTests {

        @Test
        fun sendsCorrectlyFormattedMessageWhenWebsocketIsConnected() {
            // Arrange
            val tradeRequest = TradeRequest(
                offeredResources = mapOf(TileType.WOOD to 4),
                targetResources = mapOf(TileType.ORE to 1)
            )
            val messageSlot = slot<MessageDTO>()

            every { mockPlayerSessionManager.getPlayerId() } returns testPlayerId
            every { mockWebSocketClient.isConnected() } returns true
            every { mockWebSocketClient.sendMessage(capture(messageSlot)) } returns true

            // Act
            tradeLogic.sendBankTrade(testLobbyId, tradeRequest)

            // Assert
            verify(exactly = 1) { mockWebSocketClient.sendMessage(any()) }

            val capturedMessage = messageSlot.captured
            assertEquals(MessageType.TRADE_WITH_BANK, capturedMessage.type)
            assertEquals(testPlayerId, capturedMessage.player)
            assertEquals(testLobbyId, capturedMessage.lobbyId)

            // Verify the JSON payload content
            val payload = capturedMessage.message
            assertNotNull(payload)
            val offered = payload?.get("offeredResources")?.jsonObject
            val target = payload?.get("targetResources")?.jsonObject
            assertEquals(4, offered?.get("WOOD")?.jsonPrimitive?.content?.toInt())
            assertEquals(1, target?.get("ORE")?.jsonPrimitive?.content?.toInt())
        }

        @Test
        fun doesNotSendMessageWhenWebsocketIsNotConnected() {
            // Arrange
            val tradeRequest = TradeRequest(mapOf(TileType.SHEEP to 2), mapOf(TileType.CLAY to 1))
            every { mockPlayerSessionManager.getPlayerId() } returns testPlayerId
            every { mockWebSocketClient.isConnected() } returns false // Key condition for this test

            // Act
            tradeLogic.sendBankTrade(testLobbyId, tradeRequest)

            // Assert
            verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
            verify(exactly = 1) { Log.e("TradeLogic", "WebSocket not connected, cannot send trade.") }
        }

        @Test
        fun doesNotSendMessageWhenPlayerIdIsNotAvailable() {
            // Arrange
            val tradeRequest = TradeRequest(mapOf(TileType.SHEEP to 2), mapOf(TileType.CLAY to 1))
            val exception = IllegalStateException("Player ID not set")
            every { mockPlayerSessionManager.getPlayerId() } throws exception

            // Act
            tradeLogic.sendBankTrade(testLobbyId, tradeRequest)

            // Assert
            verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
            verify(exactly = 1) { Log.e("TradeLogic", "Cannot send trade, player ID not available", exception) }
        }
    }
}