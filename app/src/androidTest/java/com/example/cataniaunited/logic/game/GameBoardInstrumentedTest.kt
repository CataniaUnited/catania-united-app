package com.example.cataniaunited.logic.game

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.data.GameDataHandler
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.spy

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
class GameBoardInstrumentedTest() {


    private lateinit var mainApplication: MainApplication
    private lateinit var realClient: WebSocketClient
    private lateinit var spyClient: WebSocketClient
    private lateinit var gameBoardLogic: GameBoardLogic
    private lateinit var mockWebServer: MockWebServer
    private lateinit var playerId: String
    private lateinit var playerSessionManagerMock: PlayerSessionManager
    private val gameDataHandlerMock = GameDataHandler()
    private val testJsonParser = Json { encodeDefaults = true }

    @Before
    fun setup() {
        println("Setting up GameBoardInstrumentedTest...")
        mainApplication = ApplicationProvider.getApplicationContext()
        realClient = mainApplication.getWebSocketClient()

        mockWebServer = MockWebServer()
        mockWebServer.start()
        val wsUrl: String = mockWebServer.url("/").toString().replace("http", "ws")

        val mockServerClient = WebSocketClient(wsUrl)
        spyClient = spy(mockServerClient)

        playerSessionManagerMock = PlayerSessionManager(mainApplication)

        runBlocking(Dispatchers.Main) {
            try {
                val clientField = MainApplication::class.java.getDeclaredField("webSocketClient")
                clientField.isAccessible = true
                clientField.set(mainApplication, spyClient)
                playerId = "TestPlayer-${System.currentTimeMillis()}"
                mainApplication.setPlayerId(playerId)
            } catch (e: Exception) {
                fail("Test setup failed during reflection/injection: ${e.message}")
            }
        }

        gameBoardLogic = GameBoardLogic(playerSessionManagerMock)
        println("Setup complete: PlayerID=$playerId, SpyClient injected.")
    }

    @After
    fun teardown() {
        println("Tearing down GameBoardInstrumentedTest...")
        try {
            val clientField = MainApplication::class.java.getDeclaredField("webSocketClient")
            clientField.isAccessible = true
            clientField.set(mainApplication, realClient)
        } catch (e: Exception) {
            println("Warning: Error restoring real WebSocketClient: ${e.message}")
        }
        spyClient.close()
        mockWebServer.shutdown()
        println("Teardown complete.")
    }

    private val dummyOnConnectionSuccess: (String) -> Unit = { pid -> println("DummyCallback: onConnectionSuccess $pid") }
    private val dummyOnLobbyCreated: (String) -> Unit = { lid -> println("DummyCallback: onLobbyCreated $lid") }
    private val dummyOnGameBoardReceived: (String, String) -> Unit = { lid, json -> println("DummyCallback: onGameBoardReceived $lid, json len: ${json.length}") }
    private val dummyOnClosed: (Int, String) -> Unit = { code, reason -> println("DummyCallback: onClosed $code $reason") }
    private val dummyOnDiceResult: (Int, Int) -> Unit = { _, _ -> }
    private val dummyOnPlayerResourcesRecieved: (Map<TileType, Int>) -> Unit = { _ -> }

    @Test
    fun testPlaceSettlement() {
        println("Running testPlaceSettlement...")
        val lobbyId = "lobby-settle-${System.currentTimeMillis()}"
        val settlementPositionId = 100
        val messagePayload = buildJsonObject { put("settlementPositionId", settlementPositionId) }
        val expectedMessageDTO = MessageDTO(MessageType.PLACE_SETTLEMENT, playerId, lobbyId, null, messagePayload)
        val expectedJson = testJsonParser.encodeToString(MessageDTO.serializer(), expectedMessageDTO)

        val messageLatch = CountDownLatch(1)
        val openLatch = CountDownLatch(1)
        val errorLatch = CountDownLatch(1)
        val receivedMessage = mutableListOf<String>()

        mockWebServer.enqueue(MockResponse()
            .setResponseCode(101)
            .withWebSocketUpgrade(object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) { println("MockServer: Opened") }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    println("MockServer: Received '$text'")
                    synchronized(receivedMessage) { receivedMessage.add(text) }
                    messageLatch.countDown()
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { println("MockServer: onFailure ${t.message}") }
            })
        )

        println("Connecting spyClient...")
        val t = thread {
            spyClient.connect(object : WebSocketListenerImpl(
                onConnectionSuccess = dummyOnConnectionSuccess,
                onLobbyCreated = dummyOnLobbyCreated,
                onGameBoardReceived = dummyOnGameBoardReceived,
                onError = { e ->
                    println("!!! Test onError CALLED: ${e.message}")
                    errorLatch.countDown()
                },
                onClosed = dummyOnClosed,
                gameDataHandler = gameDataHandlerMock,
                onDiceResult = dummyOnDiceResult,
                onPlayerResourcesReceived = dummyOnPlayerResourcesRecieved
            ) {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    println("Client Listener: Opened connection (on background thread).")
                    openLatch.countDown()
                    try {
                        println("Client Listener: Sending placeSettlement...")
                        gameBoardLogic.placeSettlement(settlementPositionId, lobbyId)
                    } catch (e: Exception) {
                        println("!!! Error calling placeSettlement: ${e.message}")
                        errorLatch.countDown()
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    println("!!! Client Listener onFailure triggered.")
                    errorLatch.countDown()
                }
            })
        }

        val messageReceived = messageLatch.await(8, TimeUnit.SECONDS)
        val errorOccurred = errorLatch.count == 0L

        assertFalse("onError callback was triggered during test", errorOccurred)
        assertTrue("Message not received by server within timeout", messageReceived)
        assertEquals("Expected exactly one message", 1, receivedMessage.size)
        assertEquals("Sent message JSON does not match expected", expectedJson, receivedMessage[0])
        t.join()
        println("testPlaceSettlement finished successfully.")
    }

    @Test
    fun testPlaceRoad() {
        println("Running testPlaceRoad...")
        val lobbyId = "lobby-road-${System.currentTimeMillis()}"
        val roadId = 100
        val messagePayload = buildJsonObject { put("roadId", roadId) }
        val expectedMessageDTO = MessageDTO(MessageType.PLACE_ROAD, playerId, lobbyId, null, messagePayload)
        val expectedJson = testJsonParser.encodeToString(MessageDTO.serializer(), expectedMessageDTO)

        val messageLatch = CountDownLatch(1)
        val openLatch = CountDownLatch(1)
        val errorLatch = CountDownLatch(1)
        val receivedMessage = mutableListOf<String>()

        mockWebServer.enqueue(MockResponse()
            .setResponseCode(101)
            .withWebSocketUpgrade(object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) { println("MockServer: Opened") }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    println("MockServer: Received '$text'")
                    synchronized(receivedMessage) { receivedMessage.add(text) }
                    messageLatch.countDown()
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { println("MockServer: onFailure ${t.message}")}
            })
        )

        println("Connecting spyClient...")
        val t = thread {
            spyClient.connect(object : WebSocketListenerImpl(
                onConnectionSuccess = dummyOnConnectionSuccess,
                onLobbyCreated = dummyOnLobbyCreated,
                onGameBoardReceived = dummyOnGameBoardReceived,
                onError = { e ->
                    println("!!! Test onError CALLED: ${e.message}")
                    errorLatch.countDown()
                },
                onClosed = dummyOnClosed,
                onDiceResult = dummyOnDiceResult,
                gameDataHandler = gameDataHandlerMock,
                onPlayerResourcesReceived = dummyOnPlayerResourcesRecieved
            ) {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    println("Client Listener: Opened connection (on background thread).")
                    openLatch.countDown()
                    try {
                        println("Client Listener: Sending placeRoad...")
                        gameBoardLogic.placeRoad(roadId, lobbyId)
                    } catch (e: Exception) {
                        println("!!! Error calling placeRoad: ${e.message}")
                        errorLatch.countDown()
                    }
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    println("!!! Client Listener onFailure triggered.")
                    errorLatch.countDown()
                }
            })
        }

        val messageReceived = messageLatch.await(8, TimeUnit.SECONDS)
        val errorOccurred = errorLatch.count == 0L

        assertFalse("onError callback was triggered during test", errorOccurred)
        assertTrue("Message not received by server within timeout", messageReceived)
        assertEquals("Expected exactly one message", 1, receivedMessage.size)
        assertEquals("Sent message JSON does not match expected", expectedJson, receivedMessage[0])
        t.join()
        println("testPlaceRoad finished successfully.")
    }

    @Test
    fun testRollDice() {
        println("Running testRollDice...")
        val lobbyId = "lobby-dice-${System.currentTimeMillis()}"

        val messagePayload = buildJsonObject { put("action", "rollDice") }
        val expectedMessageDTO = MessageDTO(MessageType.ROLL_DICE, playerId, lobbyId, null, messagePayload)
        val expectedJson = testJsonParser.encodeToString(MessageDTO.serializer(), expectedMessageDTO)

        val messageLatch = CountDownLatch(1)
        val openLatch = CountDownLatch(1)
        val errorLatch = CountDownLatch(1)
        val receivedMessage = mutableListOf<String>()

        mockWebServer.enqueue(MockResponse()
            .setResponseCode(101)
            .withWebSocketUpgrade(object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) { println("MockServer: Opened") }
                override fun onMessage(webSocket: WebSocket, text: String) {
                    println("MockServer: Received '$text'")
                    synchronized(receivedMessage) { receivedMessage.add(text) }
                    messageLatch.countDown()
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { println("MockServer: onFailure ${t.message}") }
            })
        )

        println("Connecting spyClient...")
        val t = thread {
            spyClient.connect(object : WebSocketListenerImpl(
                onConnectionSuccess = dummyOnConnectionSuccess,
                onLobbyCreated = dummyOnLobbyCreated,
                onGameBoardReceived = dummyOnGameBoardReceived,
                onError = { e ->
                    println("!!! Test onError CALLED: ${e.message}")
                    errorLatch.countDown()
                },
                onClosed = dummyOnClosed,
                onDiceResult = dummyOnDiceResult,
                gameDataHandler = gameDataHandlerMock,
                onPlayerResourcesReceived = dummyOnPlayerResourcesRecieved
            ) {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    println("Client Listener: Opened connection (on background thread).")
                    openLatch.countDown()
                    try {
                        println("Client Listener: Sending rollDice...")
                        gameBoardLogic.rollDice(lobbyId)
                    } catch (e: Exception) {
                        println("!!! Error calling rollDice: ${e.message}")
                        errorLatch.countDown()
                    }
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    println("!!! Client Listener onFailure triggered in dice test.")
                    errorLatch.countDown()
                }
            })
        }

        val connectionOpened = openLatch.await(5, TimeUnit.SECONDS)
        assertTrue("WebSocket connection was not opened within timeout", connectionOpened)

        val messageReceived = messageLatch.await(8, TimeUnit.SECONDS)
        val errorOccurred = errorLatch.count == 0L

        assertFalse("onError callback was triggered during test", errorOccurred)
        assertTrue("Message not received by server within timeout", messageReceived)
        assertEquals("Expected exactly one message", 1, receivedMessage.size)
        assertEquals("Sent message JSON does not match expected", expectedJson, receivedMessage[0])

        t.join()
        println("testRollDice finished successfully.")
    }
}