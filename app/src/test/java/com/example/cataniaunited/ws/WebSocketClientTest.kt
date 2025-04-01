package com.example.cataniaunited.ws

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class WebSocketClientTest {

    private lateinit var mockWebServer: MockWebServer;
    private lateinit var webSocketClient: WebSocketClient

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer();
        mockWebServer.start();

        val wsUrl: String = mockWebServer.url("/")
            .toString().replace("http", "ws")
        webSocketClient = WebSocketClient(wsUrl)

    }

    @Test
    fun testWebSocketManagerConnect() {
        val expectedMessage = "Connection successful";
        val messageCountDown = CountDownLatch(1);
        var receivedMessage: String? = null;

        //Setup mock server response
        mockWebServer.enqueue(
            MockResponse().withWebSocketUpgrade(
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

        webSocketClient.connect(listener)
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
        mockWebServer.enqueue(
            MockResponse().withWebSocketUpgrade(
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

        webSocketClient.connect(listener)
        webSocketClient.sendMessage(expectedMessage1)
        webSocketClient.sendMessage(expectedMessage2)
        messageCountDown.await(5, TimeUnit.SECONDS);

        Assertions.assertEquals(expecteMessageCount, receivedMessage.size)
        Assertions.assertEquals(expectedMessage1, receivedMessage[0])
        Assertions.assertEquals(expectedMessage2, receivedMessage[1])
    }

    @Test
    fun `close() should terminate WebSocket connection`() {
        val mockWebSocket: WebSocket = mock()
        val manager = WebSocketClient("ws://dummy-url")

        // Use reflection to set private web socket field
        val field = manager::class.java.getDeclaredField("webSocket")
        field.isAccessible = true
        field.set(manager, mockWebSocket)  // Hier wird der Mock gesetzt, nicht das Field-Objekt

        manager.close()

        verify(mockWebSocket).close(1000, "Client closed connection")
    }

    @Test
    fun `close() should do nothing if WebSocket is null`() {
        val manager = WebSocketClient("ws://dummy-url")
        assertDoesNotThrow { manager.close() }
    }

    @Test
    fun `sendMessage() should do nothing if WebSocket is null`() {
        val manager = WebSocketClient("ws://dummy-url")
        assertDoesNotThrow { manager.sendMessage("Test") }
    }
}