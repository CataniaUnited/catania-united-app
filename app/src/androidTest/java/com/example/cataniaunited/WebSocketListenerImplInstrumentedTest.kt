package com.example.cataniaunited

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.exception.GameException
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.game.GameDataHandler
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnDiceResult
import com.example.cataniaunited.ws.callback.OnDiceRolling
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnLobbyUpdated
import com.example.cataniaunited.ws.callback.OnPlayerJoined
import com.example.cataniaunited.ws.callback.OnPlayerResourcesReceived
import com.example.cataniaunited.ws.callback.OnWebSocketClosed
import com.example.cataniaunited.ws.callback.OnWebSocketError
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
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
    private lateinit var mockPlayerJoined: OnPlayerJoined
    private lateinit var mockLobbyUpdated: OnLobbyUpdated
    private lateinit var mockGameBoardReceived: OnGameBoardReceived
    private lateinit var mockError: OnWebSocketError
    private lateinit var mockClosed: OnWebSocketClosed
    private lateinit var mockDiceResult: OnDiceResult
    private lateinit var mockDiceRolling: OnDiceRolling
    private lateinit var mockGameDataHandler: GameDataHandler
    private lateinit var mockWebSocket: WebSocket
    private lateinit var mockResponse: Response
    private lateinit var mockOnPlayerResoucesRecieved: OnPlayerResourcesReceived


    private val minimalBoardContent =
        """{"tiles":[],"settlementPositions":[],"roads":[],"ports":[],"ringsOfBoard":0,"sizeOfHex":0}"""


    @Before
    fun setUp() {
        mockConnectionSuccess = mockk(relaxed = true)
        mockLobbyCreated = mockk(relaxed = true)
        mockPlayerJoined = mockk(relaxed = true)
        mockLobbyUpdated = mockk(relaxed = true)
        mockGameBoardReceived = mockk(relaxed = true)
        mockDiceResult = mockk(relaxed = true)
        mockDiceRolling = mockk(relaxed = true)
        mockError = mockk(relaxed = true)
        mockClosed = mockk(relaxed = true)
        mockWebSocket = mockk(relaxed = true)
        mockResponse = mockk(relaxed = true)
        mockOnPlayerResoucesRecieved = mockk(relaxed = true)

        mockGameDataHandler = GameDataHandler()

        webSocketListener = WebSocketListenerImpl(
            onConnectionSuccess = mockConnectionSuccess,
            onLobbyCreated = mockLobbyCreated,
            onPlayerJoined = mockPlayerJoined,
            onLobbyUpdated = mockLobbyUpdated,
            onGameBoardReceived = mockGameBoardReceived,
            onError = mockError,
            onClosed = mockClosed,
            onDiceResult = mockDiceResult,
            onDiceRolling = mockDiceRolling,
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
    fun onMessage_handlesLobbyCreated_withLobbyIdAndPlayers() {
        val lobbyId = "testLobby1"
        val hostPlayerId = "hostPlayer1"
        val hostPlayerInfo = PlayerInfo(
            id = hostPlayerId,
            username = "HostUser",
            color = "#RRGGBB",
            isHost = true
        )
        val playersMap = mapOf(hostPlayerId to hostPlayerInfo)

        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_CREATED.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockLobbyCreated.onLobbyCreated(lobbyId, playersMap) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }


    @Test
    fun onMessage_handlesLobbyCreated_missingLobbyId() {
        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_CREATED.name)
            // No lobbyId
            put("players", Json.encodeToJsonElement(emptyMap<String, PlayerInfo>()))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any(), any()) }
        verify(exactly = 1) { mockError.onError(any<IllegalArgumentException>()) }
    }

    @Test
    fun onMessage_handlesLobbyCreated_missingPlayers() {
        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_CREATED.name)
            put("lobbyId", "testLobby")
            // No players map
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any(), any()) }
        verify(exactly = 1) { mockError.onError(any<IllegalArgumentException>()) }
    }

    @Test
    fun onMessage_handlesGameBoardJson_withValidBoard() {
        val lobbyId = "gameLobby"
        val boardJsonObject = Json.parseToJsonElement(minimalBoardContent).jsonObject
        val expectedFullMessageString = """{"gameboard":$minimalBoardContent}"""

        val playerInfo1 = PlayerInfo("p1", "Player1", "#FFF", true, true)
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived(
                lobbyId,
                expectedFullMessageString
            )
        }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesGameBoardJson_withException() {
        val lobbyId = "gameLobby"
        val boardJsonObject = Json.parseToJsonElement(minimalBoardContent).jsonObject
        val expectedFullMessageString = """{"gameboard":$minimalBoardContent}"""

        val playerInfo1 = PlayerInfo("p1", "Player1", "#FFF", true, true)
        val playersMap = mapOf("p1" to playerInfo1)

        val exception = Exception("Test exception")
        every {
            mockGameBoardReceived.onGameBoardReceived(lobbyId, expectedFullMessageString)
        } throws exception

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived(
                lobbyId,
                expectedFullMessageString
            )
        }
        // The exception will be caught by the outer try-catch in onMessage
        verify(exactly = 1) { mockError.onError(any()) }
    }

    @Test
    fun onMessage_handlesPlaceSettlement_withValidBoard() {
        val lobbyId = "gameLobby"
        val boardJsonObject = Json.parseToJsonElement(minimalBoardContent).jsonObject
        val expectedFullMessageString = """{"gameboard":$minimalBoardContent}"""

        val playerInfo1 = PlayerInfo("p1", "Player1", "#FFF", true, true)
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.PLACE_SETTLEMENT.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived(lobbyId, expectedFullMessageString)
        }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesPlaceRoad_withValidBoard() {
        val lobbyId = "gameLobby"
        val boardJsonObject = Json.parseToJsonElement(minimalBoardContent).jsonObject
        val expectedFullMessageString = """{"gameboard":$minimalBoardContent}"""

        val playerInfo1 = PlayerInfo("p1", "Player1", "#FFF", true, true)
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.PLACE_ROAD.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived(
                lobbyId,
                expectedFullMessageString
            )
        }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }


    @Test
    fun onMessage_handlesGameBoardJson_missingLobbyId() {
        val boardJsonObject = Json.parseToJsonElement(minimalBoardContent).jsonObject
        val expectedFullMessageString = """{"gameboard":$minimalBoardContent}"""


        val playerInfo1 = PlayerInfo("p1", "Player1", "#FFF", true, true)
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            // No lobbyId
            put("players", Json.encodeToJsonElement(playersMap))
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived("", expectedFullMessageString)
        }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesGameBoardJson_missingMessageObject() {
        val lobbyId = "gameLobby"
        val playerInfo1 = PlayerInfo("p1", "Player1", "#FFF", true, true)
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
            // No message object, so messageDTO.message will be null
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 0) { mockError.onError(any()) }
    }

    @Test
    fun onMessage_handlesInvalidMessageDTOJson_callsOnError() {
        val invalidJson =
            """{"type":"INVALID_TYPE","player":"abc"}""" // Missing required fields for MessageDTO

        webSocketListener.onMessage(mockWebSocket, invalidJson)

        verify(exactly = 1) { mockError.onError(any<kotlinx.serialization.SerializationException>()) }
        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
    }

    @Test
    fun onMessage_handlesUnhandledMessageType_logsWarning() {
        val unhandledTypeJson = """{"type":"CLIENT_DISCONNECTED"}"""

        webSocketListener.onMessage(mockWebSocket, unhandledTypeJson)

        // Verify no specific callbacks are made, and no error is thrown (just a log)
        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 0) { mockError.onError(any()) }
    }

    @Test
    fun onMessage_handlesErrorMessageType_errorCallbackCalled() {
        val errorMessageContent = "Something went wrong on the server!"
        val messageJson = buildJsonObject {
            put("type", MessageType.ERROR.name)
            put("message", buildJsonObject {
                put("error", errorMessageContent)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockError.onError(match {
                it is GameException && it.message == "\"$errorMessageContent\""
            })
        }
    }

    @Test
    fun onFailure_callsOnErrorCallback() {
        val testErrorThrowable = Throwable("WebSocket failure")
        webSocketListener.onFailure(mockWebSocket, testErrorThrowable, null)
        verify(exactly = 1) { mockError.onError(testErrorThrowable) }
    }

    @Test
    fun onClosed_callsOnClosedCallback() {
        val code = 1000
        val reason = "Normal closure"

        webSocketListener.onClosed(mockWebSocket, code, reason)
        verify(exactly = 1) { mockClosed.onClosed(code, reason) }
    }

    @Test
    fun handleDiceResult_callsOnDiceResultWithParsedValues() {
        val message = buildJsonObject {
            put("dice1", 3)
            put("dice2", 4)
            put("total", 7)
            put("rollingUsername", "Player1")
        }

        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            player = "p1",
            lobbyId = "l1",
            players = mapOf("p1" to PlayerInfo("p1", "Player1", "#8C4E27")),
            message = message
        )

        webSocketListener.handleDiceResult(dto)

        verify {
            mockDiceResult.onDiceResult(3, 4, "Player1")
        }
    }


    @Test
    fun handleDiceResult_callsOnDiceResultAndUpdatesPlayersWhenPlayersArePresent() {
        val players = mapOf("player1" to PlayerInfo("player1", "UserA", "#8C4E27"))

        val message = buildJsonObject {
            put("dice1", 1)
            put("dice2", 2)
            put("total", 3)
            put("rollingUsername", "UserA")
        }

        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            player = "player1",
            lobbyId = "lobby1",
            players = players,
            message = message
        )

        webSocketListener.handleDiceResult(dto)

        verify {
            mockGameDataHandler.updatePlayers(players)
            mockDiceResult.onDiceResult(1, 2, "UserA")
        }
    }

    @Test
    fun handleDiceResult_doesNotUpdatePlayersWhenPlayersAreNull() {
        val receivedDice1 = 1
        val receivedDice2 = 2
        val playerName = "Player1"

        val messagePayload = buildJsonObject {
            put("dice1", receivedDice1)
            put("dice2", receivedDice2)
            put("rollingUsername", playerName)
        }

        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            message = messagePayload,
            players = null
        )

        webSocketListener.handleDiceResult(dto)

        verify(exactly = 1) { mockDiceResult.onDiceResult(receivedDice1, receivedDice2, playerName) }
        verify(exactly = 0) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(any()) }
    }

    @Test
    fun handleDiceRolling_callsOnDiceRollingWithUsernameFromMessage() {
        val expectedUsername = "DiceRoller"

        val messagePayload = buildJsonObject {
            put("rollingUsername", expectedUsername)
        }

        val dto = MessageDTO(
            type = MessageType.ROLL_DICE,
            message = messagePayload
        )

        webSocketListener.handleDiceRolling(dto)

        verify(exactly = 1) { mockDiceRolling.onDiceRolling(expectedUsername) }
    }

    @Test
    fun handleDiceRolling_callsOnDiceRollingWithPlayerNameWhenUsernameMissing() {
        val playerId = "player123"
        val expectedUsername = "PlayerFromMap"

        val playersMap = mapOf(playerId to PlayerInfo(playerId, expectedUsername))

        val dto = MessageDTO(
            type = MessageType.ROLL_DICE,
            player = playerId,
            players = playersMap
        )

        webSocketListener.handleDiceRolling(dto)

        verify(exactly = 1) { mockDiceRolling.onDiceRolling(expectedUsername) }
    }

    @Test
    fun handleDiceRolling_callsOnDiceRollingWithDefaultWhenNoNamesAvailable() {
        val dto = MessageDTO(
            type = MessageType.ROLL_DICE
        )

        webSocketListener.handleDiceRolling(dto)

        verify(exactly = 1) { mockDiceRolling.onDiceRolling("Player") }
    }

    @Test
    fun handleDiceRolling_prefersMessageUsernameOverPlayerMap() {
        val messageUsername = "MessageUsername"
        val playerId = "player456"
        val mapUsername = "MapUsername"

        val messagePayload = buildJsonObject {
            put("rollingUsername", messageUsername)
        }

        val playersMap = mapOf(playerId to PlayerInfo(playerId, mapUsername))

        val dto = MessageDTO(
            type = MessageType.ROLL_DICE,
            message = messagePayload,
            player = playerId,
            players = playersMap
        )

        webSocketListener.handleDiceRolling(dto)

        verify(exactly = 1) { mockDiceRolling.onDiceRolling(messageUsername) }
    }

    @Test
    fun handleDiceResult_usesPlayerNameFromPlayersFallback() {
        val players = mapOf("player1" to PlayerInfo("player1", "FallbackUser", "#123456"))
        val message = buildJsonObject {
            put("dice1", 3)
            put("dice2", 4)
            put("total", 7)
        }
        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            player = "player1",
            lobbyId = "lobbyX",
            players = players,
            message = message
        )

        webSocketListener.handleDiceResult(dto)

        verify {
            mockDiceResult.onDiceResult(3, 4, "FallbackUser")
        }
    }

    @Test
    fun handleDiceResult_defaultsToUnknownPlayer() {
        val message = buildJsonObject {
            put("dice1", 1)
            put("dice2", 2)
            put("total", 3)
        }
        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            player = "missingPlayer",
            lobbyId = "lobbyX",
            players = null,
            message = message
        )

        webSocketListener.handleDiceResult(dto)

        verify {
            mockDiceResult.onDiceResult(1, 2, "Unknown Player")
        }
    }

    @Test
    fun onMessage_triggersHandleDiceRolling() {
        val message = buildJsonObject {
            put("rollingUsername", "TestUser")
        }

        val dto = MessageDTO(
            type = MessageType.ROLL_DICE,
            player = "p1",
            lobbyId = "lobby1",
            players = null,
            message = message
        )

        val json = Json.encodeToString(MessageDTO.serializer(), dto)
        webSocketListener.onMessage(mockWebSocket, json)

        verify {
            mockDiceRolling.onDiceRolling("TestUser")
        }
    }

    @Test
    fun onMessage_triggersHandleDiceResult() {
        val message = buildJsonObject {
            put("dice1", 2)
            put("dice2", 3)
            put("total", 5)
            put("rollingUsername", "DiceUser")
        }

        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            player = "p1",
            lobbyId = "lobby1",
            players = null,
            message = message
        )

        val json = Json.encodeToString(MessageDTO.serializer(), dto)
        webSocketListener.onMessage(mockWebSocket, json)

        verify {
            mockDiceResult.onDiceResult(2, 3, "DiceUser")
        }
    }

    @Test
    fun onMessage_handlesPlayerJoined_withValidData() {
        val lobbyId = "lobby123"
        val playerInfo1 = PlayerInfo("p1", "Player1")
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_JOINED.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockPlayerJoined.onPlayerJoined(lobbyId, playersMap) }
    }

    @Test
    fun onMessage_handlesPlayerJoined_missingLobbyId() {
        val playerInfo1 = PlayerInfo("p1", "Player1")
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_JOINED.name)
            put("players", Json.encodeToJsonElement(playersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)
        // Warning logged, no error callback
        verify(exactly = 0) { mockPlayerJoined.onPlayerJoined(any(), any()) }
        verify(exactly = 0) { mockError.onError(any()) }
    }

    @Test
    fun onMessage_handlesPlayerJoined_missingPlayers() {
        val lobbyId = "lobby123"
        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_JOINED.name)
            put("lobbyId", lobbyId)
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)
        // Warning logged, no error callback
        verify(exactly = 0) { mockPlayerJoined.onPlayerJoined(any(), any()) }
        verify(exactly = 0) { mockError.onError(any()) }
    }


    @Test
    fun onMessage_handlesGameWon_withMissingWinnerId() {
        val playersList =
            listOf(PlayerInfo(id = "winnerId", username = "Alice", victoryPoints = 10))
        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_WON.name)
            put("message", buildJsonObject {
                put("leaderboard", Json.encodeToJsonElement(playersList))
            })
        }.toString()
        webSocketListener.onMessage(mockWebSocket, messageJson)
        verify(exactly = 0) { mockError.onError(any()) }
    }

    @Test
    fun onMessage_handlesGameWon_withMissingLeaderboard() {
        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_WON.name)
            put("message", buildJsonObject {
                put("winner", "winnerId")
            })
        }.toString()
        webSocketListener.onMessage(mockWebSocket, messageJson)
        verify(exactly = 0) { mockError.onError(any()) }
    }

    @Test
    fun onMessage_handlesGameWon_withMalformedLeaderboard() {
        // This will cause a serialization exception inside handleGameWon, which is caught and logged
        val malformedLeaderboardJson = """
            {
                "type": "GAME_WON",
                "message": {
                    "winner": "winnerId",
                    "leaderboard": [{"vp": "notANumber"}] 
                }
            }
        """.trimIndent()

        webSocketListener.onMessage(mockWebSocket, malformedLeaderboardJson)
        verify(exactly = 0) { mockError.onError(any()) } // Exception is caught internally
    }

    @Test
    fun onMessage_handlesLobbyUpdated_withValidData() {
        val lobbyId = "updatedLobby"
        val playerInfo1 = PlayerInfo("p1", "UserA")
        val updatedPlayersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_UPDATED.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(updatedPlayersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockLobbyUpdated.onLobbyUpdated(lobbyId, updatedPlayersMap) }
    }

    @Test
    fun onMessage_handlesPlayerResourceUpdate_withValidData() {
        val lobbyId = "resourceUpdateLobby"
        val playerInfo1 = PlayerInfo("p1", "UserA", resources = mapOf())
        val updatedPlayersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_RESOURCE_UPDATE.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(updatedPlayersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(updatedPlayersMap) }
    }

    @Test
    fun onMessageWithPlayer_JoinedCallsCorrectHandlerAndUpdatesData(){
        val lobbyId = "test-lobby"
        val playersMap = mapOf("p1" to PlayerInfo("p1", "UserA"))
        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_JOINED.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockPlayerJoined.onPlayerJoined(lobbyId, playersMap) }
        verify(exactly = 0) { mockLobbyUpdated.onLobbyUpdated(any(), any()) }
        verify(exactly = 0) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(any()) }
        verify(exactly = 0) { mockError.onError(any()) }
    }

    @Test
    fun onMessageWithPlayer_ResourceUpdateCallsCorrectHandlerAndUpdatesData(){
        val lobbyId = "test-lobby"
        val playersMap = mapOf("p1" to PlayerInfo("p1", "UserA"))
        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_RESOURCE_UPDATE.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(playersMap) }
        verify(exactly = 0) { mockPlayerJoined.onPlayerJoined(any(), any()) }
        verify(exactly = 0) { mockLobbyUpdated.onLobbyUpdated(any(), any()) }
        verify(exactly = 0) { mockError.onError(any()) }
    }

    @Test
    fun doesNotProcessUpdateWhenLobbyIdIsMissing(){
        val playersMap = mapOf("p1" to PlayerInfo("p1", "UserA"))
        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_UPDATED.name)
            // Missing lobbyId
            put("players", Json.encodeToJsonElement(playersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockLobbyUpdated.onLobbyUpdated(any(), any()) }
    }

    @Test
    fun doesNotProcessUpdateWhenPlayersMapIsMissing(){
        val lobbyId = "test-lobby"
        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_UPDATED.name)
            put("lobbyId", lobbyId)
            // Missing players map
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockLobbyUpdated.onLobbyUpdated(any(), any()) }
    }
}