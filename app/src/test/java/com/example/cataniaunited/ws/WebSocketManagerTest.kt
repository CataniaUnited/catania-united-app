package com.example.cataniaunited.ws

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebSocketManagerTest {

    private lateinit var mockWebServer: MockWebServer;
    private lateinit var webSocketManager: WebSocketManager

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer();
        mockWebServer.start();

        val wsUrl: String = mockWebServer.url("/")
            .toString().replace("http", "ws")
        webSocketManager = WebSocketManager(wsUrl)

    }

    @Test
    fun testWebSocketManagerConnect() {
        val expectedMessage = "Connection successful";
        val messageCountDown = CountDownLatch(1);
        var receivedMessage: String? = null;

        //Setup mock server response
        mockWebServer.enqueue(MockResponse().withWebSocketUpgrade(
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    webSocket.send(expectedMessage)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    webSocket.send(text)
                }
            }
        ))

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Received message $text")
                receivedMessage = text;
                messageCountDown.countDown()
            }
        }

        webSocketManager.connect(listener)
        messageCountDown.await(5, TimeUnit.SECONDS);

        Assertions.assertEquals(expectedMessage, receivedMessage)
    }

    @Test
    fun testWebSocketManagerSendMessage() {

        var expecteMessageCount = 2;
        val expectedMessage1 = "First message";
        val expectedMessage2 = "Second message"
        val messageCountDown = CountDownLatch(expecteMessageCount);
        val receivedMessage = mutableListOf<String>()

        //Setup mock server response
        mockWebServer.enqueue(MockResponse().withWebSocketUpgrade(
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    receivedMessage.add(text);
                    messageCountDown.countDown()
                }
            }
        ))

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Received message $text")
            }
        }

        webSocketManager.connect(listener)
        webSocketManager.sendMessage(expectedMessage1)
        webSocketManager.sendMessage(expectedMessage2)
        messageCountDown.await(5, TimeUnit.SECONDS);

        Assertions.assertEquals(expecteMessageCount, receivedMessage.size)
        Assertions.assertEquals(expectedMessage1, receivedMessage[0])
        Assertions.assertEquals(expectedMessage2, receivedMessage[1])
    }
}