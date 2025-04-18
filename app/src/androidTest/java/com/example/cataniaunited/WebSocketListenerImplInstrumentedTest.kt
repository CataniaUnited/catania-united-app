package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.ws.WebSocketListenerImpl
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.WebSocket
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class WebSocketListenerImplInstrumentedTest {

    private lateinit var mainApplication: MainApplication
    private lateinit var webSocketListener: WebSocketListenerImpl
    private val mockWebSocket = mock(WebSocket::class.java)

    @Before
    fun setup() {
        mainApplication = ApplicationProvider.getApplicationContext()
        webSocketListener = WebSocketListenerImpl()
    }

    @Test
    fun onSuccessfulConnectionShouldSetPlayerId() {
        val expectedPlayerId = "1234567890";
        val message = buildJsonObject {
            put("playerId", expectedPlayerId)
        }
        val messageDTO = MessageDTO(MessageType.CONNECTION_SUCCESSFUL, null, null, null, message)

        webSocketListener.onMessage(mockWebSocket, messageDTO.toString())
        Assert.assertEquals(expectedPlayerId, MainApplication.getInstance().getPlayerId())
    }

    @Test
    fun testOnClosing() {
        webSocketListener.onClosing(mockWebSocket, 1000, "Closed")
        verify(mockWebSocket).close(1000, null)
    }
}