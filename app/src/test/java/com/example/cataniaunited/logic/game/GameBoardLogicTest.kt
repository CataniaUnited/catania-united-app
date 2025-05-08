package com.example.cataniaunited.logic.game

import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.ws.WebSocketClient
import io.mockk.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameBoardLogicTest {

    private lateinit var mockMainApplication: MainApplication
    private lateinit var mockWebSocketClient: WebSocketClient

    private lateinit var gameBoardLogic: GameBoardLogic

    private val testPlayerId = "test-player-123"
    private val testLobbyId = "test-lobby-456"
    private val testSettlementId = 15
    private val testRoadId = 25
    private val testPlayerCount = 4

    @BeforeEach
    fun setUp() {
        mockMainApplication = mockk(relaxed = true)
        mockWebSocketClient = mockk(relaxed = true)
        gameBoardLogic = GameBoardLogic()
        mockkObject(MainApplication.Companion)
        every { MainApplication.getInstance() } returns mockMainApplication
        every { mockMainApplication.getPlayerId() } returns testPlayerId
        every { mockMainApplication.getWebSocketClient() } returns mockWebSocketClient
        every { mockWebSocketClient.isConnected() } returns true
        every { mockWebSocketClient.sendMessage(any()) } returns true
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(MainApplication.Companion)
    }


    @Test
    fun placeSettlementSendsCorrectMessageWhenConnected() {
        val expectedPayload = buildJsonObject { put("settlementPositionId", testSettlementId) }
        val expectedMessage = MessageDTO(
            type = MessageType.PLACE_SETTLEMENT, player = testPlayerId, lobbyId = testLobbyId, players = null, message = expectedPayload
        )
        val messageSlot = slot<MessageDTO>()
        gameBoardLogic.placeSettlement(testSettlementId, testLobbyId)
        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun placeSettlementDoesNotSendWhenNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false
        gameBoardLogic.placeSettlement(testSettlementId, testLobbyId)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun placeSettlementDoesNotSendWhenGetPlayerIdThrows() {
        val exception = IllegalStateException("Player ID not set")
        every { mockMainApplication.getPlayerId() } throws exception
        gameBoardLogic.placeSettlement(testSettlementId, testLobbyId)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }


    @Test
    fun placeRoadSendsCorrectMessageWhenConnected() {
        val expectedPayload = buildJsonObject { put("roadId", testRoadId) }
        val expectedMessage = MessageDTO(
            type = MessageType.PLACE_ROAD, player = testPlayerId, lobbyId = testLobbyId, players = null, message = expectedPayload
        )
        val messageSlot = slot<MessageDTO>()
        gameBoardLogic.placeRoad(testRoadId, testLobbyId)
        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun placeRoadDoesNotSendWhenNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false
        gameBoardLogic.placeRoad(testRoadId, testLobbyId)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }


    @Test
    fun requestCreateLobbySendsCorrectMessageWhenConnected() {
        val expectedMessage = MessageDTO(
            type = MessageType.CREATE_LOBBY, player = testPlayerId, lobbyId = null, players = null, message = null
        )
        val messageSlot = slot<MessageDTO>()
        gameBoardLogic.requestCreateLobby()
        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun requestCreateLobbyDoesNotSendWhenNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false
        gameBoardLogic.requestCreateLobby()
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }


    @Test
    fun requestBoardForLobbySendsCorrectMessageWhenConnected() {
        val expectedPayload = buildJsonObject { put("playerCount", testPlayerCount) }
        val expectedMessage = MessageDTO(
            type = MessageType.CREATE_GAME_BOARD, player = testPlayerId, lobbyId = testLobbyId, players = null, message = expectedPayload
        )
        val messageSlot = slot<MessageDTO>()
        gameBoardLogic.requestBoardForLobby(testLobbyId, testPlayerCount)
        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun requestBoardForLobbyUsesDefaultPlayerCountIfNotProvided() {
        val expectedPayload = buildJsonObject { put("playerCount", 4) }
        val expectedMessage = MessageDTO(
            type = MessageType.CREATE_GAME_BOARD, player = testPlayerId, lobbyId = testLobbyId, players = null, message = expectedPayload
        )
        val messageSlot = slot<MessageDTO>()
        gameBoardLogic.requestBoardForLobby(testLobbyId)
        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
        assertEquals(4, messageSlot.captured.message?.get("playerCount")?.jsonPrimitive?.int)
    }

    @Test
    fun requestBoardForLobbyDoesNotSendWhenNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false
        gameBoardLogic.requestBoardForLobby(testLobbyId, testPlayerCount)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun requestBoardForLobbyDoesNotSendWhenGetPlayerIdThrows() {
        val exception = IllegalStateException("Player ID not set")
        every { mockMainApplication.getPlayerId() } throws exception
        gameBoardLogic.requestBoardForLobby(testLobbyId, testPlayerCount)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }
}