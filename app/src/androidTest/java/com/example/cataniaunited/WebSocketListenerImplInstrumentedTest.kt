package com.example.cataniaunited

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cataniaunited.data.GameDataHandler
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.exception.GameException
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnDiceResult
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnPlayerResourcesReceived
import com.example.cataniaunited.ws.callback.OnWebSocketClosed
import com.example.cataniaunited.ws.callback.OnWebSocketError
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import okhttp3.Response
import okhttp3.WebSocket
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WebSocketListenerImplInstrumentedTest {

    private lateinit var webSocketListener: WebSocketListenerImpl
    private lateinit var mockConnectionSuccess: OnConnectionSuccess
    private lateinit var mockLobbyCreated: OnLobbyCreated
    private lateinit var mockGameBoardReceived: OnGameBoardReceived
    private lateinit var mockError: OnWebSocketError
    private lateinit var mockClosed: OnWebSocketClosed
    private lateinit var mockDiceResult: OnDiceResult
    private lateinit var mockGameDataHandler: GameDataHandler
    private lateinit var mockWebSocket: WebSocket
    private lateinit var mockResponse: Response
    private lateinit var mockOnPlayerResoucesRecieved: OnPlayerResourcesReceived

    @Before
    fun setUp() {
        mockConnectionSuccess = mockk(relaxed = true)
        mockLobbyCreated = mockk(relaxed = true)
        mockGameBoardReceived = mockk(relaxed = true)
        mockDiceResult = mockk(relaxed = true)
        mockError = mockk(relaxed = true)
        mockClosed = mockk(relaxed = true)
        mockWebSocket = mockk(relaxed = true)
        mockResponse = mockk(relaxed = true)
        mockOnPlayerResoucesRecieved = mockk(relaxed = true)

        mockGameDataHandler = GameDataHandler()

        webSocketListener = WebSocketListenerImpl(
            onConnectionSuccess = mockConnectionSuccess,
            onLobbyCreated = mockLobbyCreated,
            onGameBoardReceived = mockGameBoardReceived,
            onError = mockError,
            onClosed = mockClosed,
            onDiceResult = mockDiceResult,
            gameDataHandler = mockGameDataHandler,
            onPlayerResourcesReceived = mockOnPlayerResoucesRecieved
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onMessage_handlesConnectionSuccessful_withPlayerId() {
        val playerId = "testPlayer1"
        val messageJson = buildJsonObject {
            put("type", MessageType.CONNECTION_SUCCESSFUL.name)
            put("message", buildJsonObject {
                put("playerId", playerId)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockConnectionSuccess.onConnectionSuccess(playerId) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesConnectionSuccessful_missingPlayerId() {
        val messageJson = buildJsonObject {
            put("type", MessageType.CONNECTION_SUCCESSFUL.name)
            put("message", buildJsonObject {
                put("otherField", "value")
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 1) { mockError.onError(any<IllegalArgumentException>()) }
    }

    @Test
    fun onMessage_handlesLobbyCreated_withLobbyId() {
        val lobbyId = "testLobby1"
        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_CREATED.name)
            put("lobbyId", lobbyId)
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockLobbyCreated.onLobbyCreated(lobbyId) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesLobbyCreated_missingLobbyId() {
        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_CREATED.name)
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any()) }
        verify(exactly = 1) { mockError.onError(any<IllegalArgumentException>()) }
    }

    @Test
    fun onMessage_handlesGameBoardJson_withValidBoard() {
        val lobbyId = "gameLobby"
        val boardJsonString =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardJsonString).jsonObject

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("lobbyId", lobbyId)
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockGameBoardReceived.onGameBoardReceived(lobbyId, boardJsonString) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesGameBoardJson_withException() {
        val lobbyId = "gameLobby"
        val boardJsonString =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardJsonString).jsonObject

        val exception: Exception = Exception("Test exception")

        every {
            mockGameBoardReceived.onGameBoardReceived(
                lobbyId,
                boardJsonString
            )
        } throws exception

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("lobbyId", lobbyId)
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockGameBoardReceived.onGameBoardReceived(lobbyId, boardJsonString) }
        verify(exactly = 1) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesPlaceSettlement_withValidBoard() {
        val lobbyId = "gameLobby"
        val boardJsonString =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardJsonString).jsonObject

        val messageJson = buildJsonObject {
            put("type", MessageType.PLACE_SETTLEMENT.name)
            put("lobbyId", lobbyId)
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockGameBoardReceived.onGameBoardReceived(lobbyId, boardJsonString) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesPlaceRoad_withValidBoard() {
        val lobbyId = "gameLobby"
        val boardJsonString =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardJsonString).jsonObject

        val messageJson = buildJsonObject {
            put("type", MessageType.PLACE_ROAD.name)
            put("lobbyId", lobbyId)
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockGameBoardReceived.onGameBoardReceived(lobbyId, boardJsonString) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }


    @Test
    fun onMessage_handlesGameBoardJson_missingLobbyId() {
        val boardJsonString =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardJsonString).jsonObject

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("message", boardJsonObject)
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 1) { mockError.onError(any<IllegalArgumentException>()) }
    }

    @Test
    fun onMessage_handlesGameBoardJson_missingMessageObject() {
        val lobbyId = "gameLobby"
        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("lobbyId", lobbyId)
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 1) { mockError.onError(any<IllegalArgumentException>()) }
    }

    @Test
    fun onMessage_handlesInvalidMessageDTOJson_callsOnError() {
        val invalidJson = """{"type":"INVALID_TYPE","player":"abc"}"""

        webSocketListener.onMessage(mockWebSocket, invalidJson)

        verify(exactly = 1) { mockError.onError(any<Exception>()) }
        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any()) }
        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 0) { mockClosed.onClosed(any(), any()) }
    }

    @Test
    fun onMessage_handlesUnhandledMessageType_noCallbacksCalled() {
        val unhandledType = "UNHANDLED_MESSAGE"
        val messageJson = buildJsonObject {
            put("type", unhandledType)
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any()) }
        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 0) { mockClosed.onClosed(any(), any()) }
    }

    @Test
    fun onMessage_handlesErrorMessageType_errorCallbackCalled() {
        val messageJson = buildJsonObject {
            put("type", MessageType.ERROR.name)
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockError.onError(any<GameException>()) }

        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any()) }
        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 0) { mockClosed.onClosed(any(), any()) }
    }

    @Test
    fun onFailure_callsOnErrorCallback() {
        val testErrorThrowable = Throwable("WebSocket failure")
        val mockResponse = mockk<Response>(relaxed = true)

        webSocketListener.onFailure(mockWebSocket, testErrorThrowable, mockResponse)

        verify(exactly = 1) { mockError.onError(testErrorThrowable) }
        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any()) }
        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 0) { mockClosed.onClosed(any(), any()) }
    }

    @Test
    fun onClosed_callsOnClosedCallback() {
        val code = 1000
        val reason = "Normal closure"

        webSocketListener.onClosed(mockWebSocket, code, reason)

        verify(exactly = 1) { mockClosed.onClosed(code, reason) }
        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any()) }
        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 0) { mockError.onError(any()) }
    }

    @Test
    fun handleDiceResult_callsOnDiceResultWithParsedValues() {
        val receivedDice1 = 3
        val receivedDice2 = 4

        val message = buildJsonObject {
            put("dice1", receivedDice1)
            put("dice2", receivedDice2)
        }

        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            player = "some-player",
            lobbyId = "some-lobby",
            message = message
        )

        webSocketListener.handleDiceResult(dto)

        verify(exactly = 1) { mockDiceResult.onDiceResult(receivedDice1, receivedDice2) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun handleDiceResult_ignoresDuplicateResults() {
        val message = buildJsonObject {
            put("dice1", 2)
            put("dice2", 5)
        }

        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            player = "player",
            lobbyId = "lobby",
            message = message
        )

        webSocketListener.handleDiceResult(dto)

        verify(exactly = 1) { mockDiceResult.onDiceResult(any(), any()) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesPlayerResources_withValidData() {
        val expectedResources = mapOf(
            TileType.WOOD to 2,
            TileType.CLAY to 1,
            TileType.SHEEP to 0,
            TileType.WHEAT to 3,
            TileType.ORE to 0
        )

        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_RESOURCES.name)
            put("lobbyId", "testLobby")
            put("message", buildJsonObject {
                put("WOOD", 2)
                put("CLAY", 1)
                put("WHEAT", 3)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(expectedResources) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesPlayerResources_withAllResourcesPresent() {
        val expectedResources = mapOf(
            TileType.WOOD to 1,
            TileType.CLAY to 2,
            TileType.SHEEP to 3,
            TileType.WHEAT to 4,
            TileType.ORE to 5
        )

        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_RESOURCES.name)
            put("message", buildJsonObject {
                put("WOOD", 1)
                put("CLAY", 2)
                put("SHEEP", 3)
                put("WHEAT", 4)
                put("ORE", 5)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(expectedResources) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }


    @Test
    fun onMessage_handlesPlayerResources_withEmptyResourcesObject() {
        val expectedResources = mapOf(
            TileType.WOOD to 0,
            TileType.CLAY to 0,
            TileType.SHEEP to 0,
            TileType.WHEAT to 0,
            TileType.ORE to 0
        )
        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_RESOURCES.name)
            put("message", buildJsonObject { /* empty object */ })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(expectedResources) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesPlayerResources_withNonIntegerValues() {
        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_RESOURCES.name)
            put("message", buildJsonObject {
                put("WOOD", "two")
                put("CLAY", 1)
            })
        }.toString()

        val expectedResources = mapOf(
            TileType.WOOD to 0,
            TileType.CLAY to 1,
            TileType.SHEEP to 0,
            TileType.WHEAT to 0,
            TileType.ORE to 0
        )

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(expectedResources) }
        verify(exactly = 0) { mockError.onError(any<IllegalArgumentException>()) }
    }


    @Test
    fun onMessage_handlesPlayerResources_missingMessageObject() {
        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_RESOURCES.name)
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(any()) }
        verify(exactly = 1) { mockError.onError(match { it is IllegalArgumentException && it.message == "PLAYER_RESOURCES message missing resource object" }) }
    }
}