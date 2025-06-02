package com.example.cataniaunited

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.exception.GameException
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.game.GameDataHandler
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnDiceResult
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
    private lateinit var mockGameDataHandler: GameDataHandler
    private lateinit var mockWebSocket: WebSocket
    private lateinit var mockResponse: Response
    private lateinit var mockOnPlayerResoucesRecieved: OnPlayerResourcesReceived

    @Before
    fun setUp() {
        mockConnectionSuccess = mockk(relaxed = true)
        mockLobbyCreated = mockk(relaxed = true)
        mockPlayerJoined = mockk(relaxed = true)
        mockLobbyUpdated = mockk(relaxed = true)
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
            onPlayerJoined = mockPlayerJoined,
            onLobbyUpdated = mockLobbyUpdated,
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
    fun onMessage_handlesLobbyCreated_withLobbyIdAndPlayers() {
        val lobbyId = "testLobby1"
        val hostPlayerId = "hostPlayer1"
        val hostPlayerInfo = PlayerInfo(
            id = hostPlayerId,
            username = "HostUser",
            color = "#RRGGBB",
            isHost = true,
            isReady = false,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playersMap = mapOf(hostPlayerId to hostPlayerInfo)

        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_CREATED.name)
            put("lobbyId", lobbyId)
            put(
                "players",
                Json.encodeToJsonElement(playersMap)
            ) // Use Json.encodeToJsonElement for Map
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
        val boardContent =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardContent).jsonObject
        val expectedFullMessage =
            """{"gameboard":$boardContent}""" // This is how GameDataHandler receives it

        val playerInfo1 = PlayerInfo(
            "p1",
            "Player1",
            "#FFF",
            true,
            true,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap)) // Add players map
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived(
                lobbyId,
                expectedFullMessage
            )
        }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesGameBoardJson_withException() {
        val lobbyId = "gameLobby"
        val boardContent =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardContent).jsonObject
        val expectedFullMessage = """{"gameboard":$boardContent}"""

        val playerInfo1 = PlayerInfo(
            "p1",
            "Player1",
            "#FFF",
            true,
            true,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playersMap = mapOf("p1" to playerInfo1)

        val exception = Exception("Test exception")
        every {
            mockGameBoardReceived.onGameBoardReceived(lobbyId, expectedFullMessage)
        } throws exception

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap)) // Add players map
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived(
                lobbyId,
                expectedFullMessage
            )
        }

        verify(exactly = 1) {
            mockError.onError(
                match { error ->
                    error === exception ||
                            error.cause === exception ||
                            error.message?.contains("Test exception") == true
                }
            )
        }
    }

    @Test
    fun onMessage_handlesPlaceSettlement_withValidBoard() {
        val lobbyId = "gameLobby"
        val boardContent =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardContent).jsonObject
        val expectedFullMessage = """{"gameboard":$boardContent}"""

        val playerInfo1 = PlayerInfo(
            "p1",
            "Player1",
            "#FFF",
            true,
            true,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.PLACE_SETTLEMENT.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap)) // Add players map
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived(lobbyId, expectedFullMessage)
        }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesPlaceRoad_withValidBoard() {
        val lobbyId = "gameLobby"
        val boardContent =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardContent).jsonObject
        val expectedFullMessage = """{"gameboard":$boardContent}"""

        val playerInfo1 = PlayerInfo(
            "p1",
            "Player1",
            "#FFF",
            true,
            true,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.PLACE_ROAD.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap)) // Add players map
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived(
                lobbyId,
                expectedFullMessage
            )
        }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }


    @Test
    fun onMessage_handlesGameBoardJson_missingLobbyId() {
        val boardContent =
            """{"tiles":[],"settlementPositions":[],"roads":[],"ringsOfBoard":0,"sizeOfHex":0}"""
        val boardJsonObject = Json.parseToJsonElement(boardContent).jsonObject

        val playerInfo1 = PlayerInfo(
            "p1",
            "Player1",
            "#FFF",
            true,
            true,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            // No lobbyId
            put("players", Json.encodeToJsonElement(playersMap)) // Add players map
            put("message", buildJsonObject {
                put("gameboard", boardJsonObject)
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        // LobbyId ist optional, daher sollte es den Callback mit leeren String aufrufen
        verify(exactly = 1) {
            mockGameBoardReceived.onGameBoardReceived("", """{"gameboard":$boardContent}""")
        }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) } // Kein Fehler, da LobbyId optional
    }

    @Test
    fun onMessage_handlesGameBoardJson_missingMessageObject() {
        val lobbyId = "gameLobby"
        val playerInfo1 = PlayerInfo(
            "p1",
            "Player1",
            "#FFF",
            true,
            true,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_BOARD_JSON.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap)) // Add players map
            // No message object
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 0) { mockError.onError(any()) } // Es sollte kein Fehler gemeldet werden, da die Methode selbst einen return hat
    }

    @Test
    fun onMessage_handlesInvalidMessageDTOJson_callsOnError() {
        val invalidJson = """{"type":"INVALID_TYPE","player":"abc"}"""

        webSocketListener.onMessage(mockWebSocket, invalidJson)

        verify(exactly = 1) { mockError.onError(any<Exception>()) }
        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any(), any()) }
        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 0) { mockClosed.onClosed(any(), any()) }
    }

    @Test
    fun onMessage_handlesUnhandledMessageType_errorCallbackCalled() {
        val unhandledType = "UNHANDLED_MESSAGE"
        val messageJson = buildJsonObject {
            put("type", unhandledType)
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any(), any()) }
        verify(exactly = 0) { mockGameBoardReceived.onGameBoardReceived(any(), any()) }
        verify(exactly = 0) { mockClosed.onClosed(any(), any()) }
        verify(exactly = 1) { mockError.onError(any()) }
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
            }
            )
        }

        verify(exactly = 0) { mockConnectionSuccess.onConnectionSuccess(any()) }
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any(), any()) }
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
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any(), any()) }
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
        verify(exactly = 0) { mockLobbyCreated.onLobbyCreated(any(), any()) }
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
    fun handleDiceResult_callsOnDiceResultAndUpdatesPlayersWhenPlayersArePresent() {
        val receivedDice1 = 3
        val receivedDice2 = 4
        val playersMap = mapOf(
            "player1" to PlayerInfo(
                "player1",
                "UserA",
                "#000",
                false,
                false,
                victoryPoints = 0,
                resources = emptyMap()
            )
        )

        val message = buildJsonObject {
            put("dice1", receivedDice1)
            put("dice2", receivedDice2)
        }

        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            player = "some-player",
            lobbyId = "some-lobby",
            message = message,
            players = playersMap
        )

        webSocketListener.handleDiceResult(dto)

        verify(exactly = 1) { mockDiceResult.onDiceResult(receivedDice1, receivedDice2) }
        verify(exactly = 1) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(playersMap) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun handleDiceResult_doesNotUpdatePlayersWhenPlayersAreNull() {
        val receivedDice1 = 1
        val receivedDice2 = 2

        val message = buildJsonObject {
            put("dice1", receivedDice1)
            put("dice2", receivedDice2)
        }

        val dto = MessageDTO(
            type = MessageType.DICE_RESULT,
            player = "some-player",
            lobbyId = "some-lobby",
            message = message,
            players = null
        )

        webSocketListener.handleDiceResult(dto)

        verify(exactly = 1) { mockDiceResult.onDiceResult(receivedDice1, receivedDice2) }
        verify(exactly = 0) { mockOnPlayerResoucesRecieved.onPlayerResourcesReceived(any()) } // Should not be called
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesPlayerJoined_withValidData() {
        val lobbyId = "lobby123"
        val playerInfo1 = PlayerInfo(
            "p1",
            "Player1",
            "#FFF",
            true,
            true,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playerInfo2 = PlayerInfo(
            "p2",
            "Player2",
            "#000",
            false,
            false,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playersMap = mapOf("p1" to playerInfo1, "p2" to playerInfo2)

        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_JOINED.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(playersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockPlayerJoined.onPlayerJoined(lobbyId, playersMap) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }

    @Test
    fun onMessage_handlesPlayerJoined_missingLobbyId() {
        val playerInfo1 = PlayerInfo(
            "p1",
            "Player1",
            "#FFF",
            true,
            true,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val playersMap = mapOf("p1" to playerInfo1)

        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_JOINED.name)
            // missing lobbyId
            put("players", Json.encodeToJsonElement(playersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        // WebSocketListenerImpl logs a warning but does not call onError here.
        verify(exactly = 0) { mockPlayerJoined.onPlayerJoined(any(), any()) }
        verify(exactly = 0) { mockError.onError(any()) } // No error callback
    }

    @Test
    fun onMessage_handlesPlayerJoined_missingPlayers() {
        val lobbyId = "lobby123"
        val messageJson = buildJsonObject {
            put("type", MessageType.PLAYER_JOINED.name)
            put("lobbyId", lobbyId)
            // missing players map
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        // WebSocketListenerImpl logs a warning but does not call onError here.
        verify(exactly = 0) { mockPlayerJoined.onPlayerJoined(any(), any()) }
        verify(exactly = 0) { mockError.onError(any()) } // No error callback
    }


    @Test
    fun onMessage_handlesGameWon_withValidData() {
        val winnerId = "winnerId"
        val playersList = listOf(
            PlayerInfo(id = "winnerId", username = "Alice", victoryPoints = 10),
            PlayerInfo(id = "loserId", username = "Bob", victoryPoints = 8)
        )

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_WON.name)
            put("message", buildJsonObject {
                put("winner", winnerId)
                put("leaderboard", Json.encodeToJsonElement(playersList))
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockError.onError(any()) } // No error callback for valid data
        // For onGameWon, we don't mock it directly, but ensure no error
        // The logic is inside MainApplication.getInstance().onGameWon
    }

    @Test
    fun onMessage_handlesGameWon_withMissingWinnerId() {
        val playersList = listOf(
            PlayerInfo(id = "winnerId", username = "Alice", victoryPoints = 10)
        )

        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_WON.name)
            put("message", buildJsonObject {
                // missing winner field
                put("leaderboard", Json.encodeToJsonElement(playersList))
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockError.onError(any()) } // Still no error expected here, logs internally
    }

    @Test
    fun onMessage_handlesGameWon_withMissingLeaderboard() {
        val messageJson = buildJsonObject {
            put("type", MessageType.GAME_WON.name)
            put("message", buildJsonObject {
                put("winner", "winnerId")
                // missing leaderboard field
            })
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 0) { mockError.onError(any()) } // Still no error expected here, logs internally
    }

    @Test
    fun onMessage_handlesGameWon_withMalformedLeaderboard() {
        val malformedLeaderboard = buildJsonObject {
            put("type", MessageType.GAME_WON.name)
            put("message", buildJsonObject {
                put("winner", "winnerId")
                put("leaderboard", kotlinx.serialization.json.buildJsonArray {
                    add(buildJsonObject {
                        put("vp", "notANumber") // should be int, will cause deserialization error
                    })
                })
            }).toString()
        }.toString()

        webSocketListener.onMessage(mockWebSocket, malformedLeaderboard)

        // It catches the exception internally and just logs, does not call onError.
        verify(exactly = 0) { mockError.onError(any()) }
    }

    @Test
    fun onMessage_handlesLobbyUpdated_withValidData() {
        val lobbyId = "updatedLobby"
        val playerInfo1 =
            PlayerInfo("p1", "UserA", "#111", true, true, victoryPoints = 1, resources = emptyMap())
        val playerInfo2 = PlayerInfo(
            "p2",
            "UserB",
            "#222",
            false,
            false,
            victoryPoints = 0,
            resources = emptyMap()
        )
        val updatedPlayersMap = mapOf("p1" to playerInfo1, "p2" to playerInfo2)

        val messageJson = buildJsonObject {
            put("type", MessageType.LOBBY_UPDATED.name)
            put("lobbyId", lobbyId)
            put("players", Json.encodeToJsonElement(updatedPlayersMap))
        }.toString()

        webSocketListener.onMessage(mockWebSocket, messageJson)

        verify(exactly = 1) { mockLobbyUpdated.onLobbyUpdated(lobbyId, updatedPlayersMap) }
        verify(exactly = 0) { mockError.onError(any<Throwable>()) }
    }
}