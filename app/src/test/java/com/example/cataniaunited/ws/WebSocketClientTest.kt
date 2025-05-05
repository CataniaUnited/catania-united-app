package com.example.cataniaunited.ws

import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class WebSocketClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var wsUrl: String
    private val testJsonParser = Json { encodeDefaults = true }

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        wsUrl = mockWebServer.url("/")
            .toString().replace("http", "ws")
        webSocketClient = WebSocketClient(wsUrl)
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
        webSocketClient.close()
    }

    @Test
    fun connectShouldTriggerOnOpenAndReceiveMessage() {
        val expectedMessage = "Connection successful"
        val openLatch = CountDownLatch(1)
        val messageLatch = CountDownLatch(1)
        val receivedMessage = AtomicReference<String?>()
        val isOpenCalled = AtomicBoolean(false)

        // Setup mock server response
        mockWebServer.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("MockServer: onOpen called")
                webSocket.send(expectedMessage)
            }
        }))

        // Client listener
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("Client Listener: onOpen called")
                isOpenCalled.set(true)
                openLatch.countDown()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Client Listener: Received message '$text'")
                receivedMessage.set(text)
                messageLatch.countDown()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                fail<String>("onFailure called unexpectedly: ${t.message}")
            }
        }

        webSocketClient.connect(listener)

        assertTrue(openLatch.await(5, TimeUnit.SECONDS), "onOpen was not called")
        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "Message was not received")
        assertTrue(isOpenCalled.get(), "Listener's onOpen should be true")
        assertEquals(expectedMessage, receivedMessage.get())
    }

    @Test
    fun sendMessageSendsCorrectJsonAndReturnsTrueWhenConnected() {
        val expectedMessage1 = MessageDTO(MessageType.CREATE_LOBBY, player = "p1", lobbyId = null)
        val expectedMessage2 = MessageDTO(MessageType.SET_USERNAME, player = "user123", lobbyId = "lobbyA")
        val expectedJson1 = testJsonParser.encodeToString(expectedMessage1)
        val expectedJson2 = testJsonParser.encodeToString(expectedMessage2)

        val messageLatch = CountDownLatch(2)
        val receivedMessages = mutableListOf<String>()

        // Setup mock server to receive messages
        mockWebServer.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("MockServer: onOpen")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                println("MockServer: Received message '$text'")
                receivedMessages.add(text)
                messageLatch.countDown()
            }
        }))

        val connectionLatch = CountDownLatch(1)
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("Client Listener: onOpen")
                connectionLatch.countDown()
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                fail<String>("onFailure called unexpectedly: ${t.message}")
            }
        }

        webSocketClient.connect(listener)
        assertTrue(connectionLatch.await(5, TimeUnit.SECONDS), "Client did not connect")

        val sent1 = webSocketClient.sendMessage(expectedMessage1)
        val sent2 = webSocketClient.sendMessage(expectedMessage2)

        assertTrue(messageLatch.await(5, TimeUnit.SECONDS), "Server did not receive all messages")

        assertTrue(sent1, "sendMessage should return true for message 1")
        assertTrue(sent2, "sendMessage should return true for message 2")
        assertEquals(2, receivedMessages.size)
        assertEquals(expectedJson1, receivedMessages[0])
        assertEquals(expectedJson2, receivedMessages[1])
    }

    @Test
    fun closeCallsWebSocketClose() {
        val mockWebSocket: WebSocket = mock()
        val manager = WebSocketClient("ws://dummy-url")

        val field = manager::class.java.getDeclaredField("webSocket")
        field.isAccessible = true
        field.set(manager, mockWebSocket)

        manager.close()

        verify(mockWebSocket).close(1000, "Client closed connection")
        field.isAccessible = true
        assertNull(field.get(manager), "webSocket field should be null after close()")
    }

    @Test
    fun closeDoesNothingIfWebSocketIsNull() {
        val manager = WebSocketClient("ws://dummy-url")
        assertDoesNotThrow { manager.close() }
    }

    @Test
    fun sendMessageReturnsFalseWhenNotConnected() {
        val manager = WebSocketClient("ws://dummy-url")
        val result = manager.sendMessage(MessageDTO(MessageType.ERROR))
        assertFalse(result, "sendMessage should return false when not connected")
    }

    @Test
    fun isConnectedReturnsCorrectState() {
        assertFalse(webSocketClient.isConnected(), "Should not be connected initially")

        val connectLatch = CountDownLatch(1)
        mockWebServer.enqueue(MockResponse().withWebSocketUpgrade(object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                connectLatch.countDown()
            }
        }))

        webSocketClient.connect(object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {}
        })

        assertTrue(connectLatch.await(5, TimeUnit.SECONDS), "Connection did not open")
        assertTrue(webSocketClient.isConnected(), "Should be connected after successful connect")

        webSocketClient.close()
        assertFalse(webSocketClient.isConnected(), "Should not be connected after close")
    }

    @Test
    fun connectCalledTwiceClosesOldConnection() {
        val closeLatch1 = CountDownLatch(1)
        val openLatch1 = CountDownLatch(1)
        val openLatch2 = CountDownLatch(1)

        mockWebServer.enqueue(MockResponse().withWebSocketUpgrade(object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) { openLatch1.countDown() }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("First connection closing: $code / $reason")
                closeLatch1.countDown()
            }
        }))
        webSocketClient.connect(object : WebSocketListener(){
            override fun onOpen(webSocket: WebSocket, response: Response) {}
        })
        assertTrue(openLatch1.await(5, TimeUnit.SECONDS), "First connection failed to open")

        mockWebServer.enqueue(MockResponse().withWebSocketUpgrade(object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) { openLatch2.countDown() }
        }))
        webSocketClient.connect(object : WebSocketListener(){
            override fun onOpen(webSocket: WebSocket, response: Response) {}
        })


        assertTrue(openLatch2.await(5, TimeUnit.SECONDS), "Second connection failed to open")
    }

    @Test
    fun onFailureCallbackTriggeredOnConnectionFailure() {
        val failureLatch = CountDownLatch(1)
        val failureThrowable = AtomicReference<Throwable?>()

        mockWebServer.shutdown()
        val invalidClient = WebSocketClient(wsUrl)

        val listener = object : WebSocketListener() {
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("Client Listener: onFailure called: ${t.message}")
                failureThrowable.set(t)
                failureLatch.countDown()
            }
            override fun onOpen(webSocket: WebSocket, response: Response) {
                fail<String>("onOpen called unexpectedly")
            }
        }

        invalidClient.connect(listener)

        assertTrue(failureLatch.await(7, TimeUnit.SECONDS), "onFailure was not called")
        assertNotNull(failureThrowable.get(), "Throwable in onFailure should not be null")
    }
}