package com.example.cataniaunited.logic.game

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.spy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class GameBoardInstrumentedTest {

    private lateinit var mainApplication: MainApplication
    private lateinit var realClient: WebSocketClient
    private lateinit var spyClient: WebSocketClient
    private lateinit var gameBoard: GameBoard
    private lateinit var mockWebServer: MockWebServer;
    private val playerId: String = "TestPlayer"

    @Before
    fun setup() {
        mainApplication = ApplicationProvider.getApplicationContext()
        realClient = mainApplication.getWebSocketClient()

        mockWebServer = MockWebServer();
        mockWebServer.start()

        val wsUrl: String = mockWebServer.url("/")
            .toString().replace("http", "ws")

        spyClient = spy(WebSocketClient(wsUrl))
        mainApplication.webSocketClient = spyClient
        mainApplication.setPlayerId(playerId)
        gameBoard = GameBoard()
    }

    @After
    fun teardown() {
        mainApplication.webSocketClient = realClient
        mockWebServer.shutdown()
    }

    @Test
    fun testPlaceSettlement() {
        val lobbyId: String = "lobby1"
        val settlementPositionId = 100
        val message = buildJsonObject {
            put("settlementPositionId", settlementPositionId)
        }
        val expectedMessageDTO =
            MessageDTO(MessageType.PLACE_SETTLEMENT, playerId, lobbyId, null, message)
        val messageCountDown = CountDownLatch(1);
        val receivedMessage = mutableListOf<String>()

        //Setup mock server response
        mockWebServer.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        receivedMessage.add(text);
                        messageCountDown.countDown()
                    }
                }
            ))

        spyClient.connect(object : WebSocketListenerImpl() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Test action after connection is established
                gameBoard.placeSettlement(settlementPositionId, lobbyId)
            }
        })

        messageCountDown.await(5, TimeUnit.SECONDS)
        Assert.assertEquals(expectedMessageDTO.toString(), receivedMessage[0])
    }

    @Test
    fun testPlaceRoad() {
        val lobbyId: String = "lobby1"
        val roadId = 100
        val message = buildJsonObject {
            put("roadId", roadId)
        }
        val expectedMessageDTO =
            MessageDTO(MessageType.PLACE_ROAD, playerId, lobbyId, null, message)
        val messageCountDown = CountDownLatch(1);
        val receivedMessage = mutableListOf<String>()

        //Setup mock server response
        mockWebServer.enqueue(
            MockResponse().withWebSocketUpgrade(
                object : WebSocketListener() {
                    override fun onMessage(webSocket: WebSocket, text: String) {
                        receivedMessage.add(text);
                        messageCountDown.countDown()
                    }
                }
            ))

        spyClient.connect(object : WebSocketListenerImpl() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Test action after connection is established
                gameBoard.placeRoad(roadId, lobbyId)
            }
        })

        messageCountDown.await(5, TimeUnit.SECONDS)
        Assert.assertEquals(expectedMessageDTO.toString(), receivedMessage[0])
    }


}