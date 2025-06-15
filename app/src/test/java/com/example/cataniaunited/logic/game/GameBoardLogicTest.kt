package com.example.cataniaunited.logic.game

import androidx.compose.runtime.mutableStateListOf
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.data.model.PlayerInfo
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameBoardLogicTest {

    private lateinit var mockMainApplication: MainApplication
    private lateinit var mockWebSocketClient: WebSocketClient
    private lateinit var mockPlayerSessionManager: PlayerSessionManager

    private lateinit var gameBoardLogic: GameBoardLogic

    private val testPlayerId = "test-player-123"
    private val testLobbyId = "test-lobby-456"
    private val testSettlementId = 15
    private val testRoadId = 25

    @BeforeEach
    fun setUp() {
        mockMainApplication = mockk(relaxed = true)
        mockWebSocketClient = mockk(relaxed = true)
        mockPlayerSessionManager = mockk(relaxed = true)
        gameBoardLogic = GameBoardLogic(mockPlayerSessionManager)
        mockkObject(MainApplication.Companion)
        every { mockPlayerSessionManager.getPlayerId() } returns testPlayerId
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
            type = MessageType.PLACE_SETTLEMENT,
            player = testPlayerId,
            lobbyId = testLobbyId,
            players = null,
            message = expectedPayload
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
        every { mockPlayerSessionManager.getPlayerId() } throws exception
        gameBoardLogic.placeSettlement(testSettlementId, testLobbyId)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun upgradeSettlementSendsCorrectMessageWhenConnected() {
        val expectedPayload = buildJsonObject { put("settlementPositionId", testSettlementId) }
        val expectedMessage = MessageDTO(
            type = MessageType.UPGRADE_SETTLEMENT,
            player = testPlayerId,
            lobbyId = testLobbyId,
            players = null,
            message = expectedPayload
        )
        val messageSlot = slot<MessageDTO>()
        gameBoardLogic.upgradeSettlement(testSettlementId, testLobbyId)
        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun upgradeSettlementDoesNotSendWhenNotConnected() {
        every { mockWebSocketClient.isConnected() } returns false
        gameBoardLogic.placeSettlement(testSettlementId, testLobbyId)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun upgradeSettlementDoesNotSendWhenGetPlayerIdThrows() {
        val exception = IllegalStateException("Player ID not set")
        every { mockPlayerSessionManager.getPlayerId() } throws exception
        gameBoardLogic.placeSettlement(testSettlementId, testLobbyId)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun placeRoadSendsCorrectMessageWhenConnected() {
        val expectedPayload = buildJsonObject { put("roadId", testRoadId) }
        val expectedMessage = MessageDTO(
            type = MessageType.PLACE_ROAD,
            player = testPlayerId,
            lobbyId = testLobbyId,
            players = null,
            message = expectedPayload
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
    fun placeRoadDoesNotSendWhenGetPlayerIdThrows() {
        val exception = IllegalStateException("Player ID not set")
        every { mockPlayerSessionManager.getPlayerId() } throws exception
        gameBoardLogic.placeRoad(testRoadId, testLobbyId)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

    @Test
    fun rollDiceSendsCorrectMessageWhenConnected() {
        val testPlayer = PlayerInfo(id = testPlayerId, username = "TestPlayer")
        val playersStateList = mutableStateListOf<PlayerInfo>()
        playersStateList.add(testPlayer)
        every { mockMainApplication.players } returns playersStateList

        val expectedPayload = buildJsonObject {
            put("action", "rollDice")
            put("player", testPlayerId)
            put("playerName", "TestPlayer")
        }
        val expectedMessage = MessageDTO(
            type = MessageType.ROLL_DICE,
            player = testPlayerId,
            lobbyId = testLobbyId,
            players = null,
            message = expectedPayload
        )
        gameBoardLogic.rollDice(testLobbyId)
        val messageSlot = slot<MessageDTO>()
        verify(exactly = 1) { mockWebSocketClient.sendMessage(capture(messageSlot)) }
        assertEquals(expectedMessage, messageSlot.captured)
    }

    @Test
    fun rollDiceDoesNotSendWhenGetPlayerIdThrows() {
        val exception = IllegalStateException("Player ID not set")
        every { mockPlayerSessionManager.getPlayerId() } throws exception
        gameBoardLogic.rollDice(testLobbyId)
        verify(exactly = 0) { mockWebSocketClient.sendMessage(any()) }
    }

}