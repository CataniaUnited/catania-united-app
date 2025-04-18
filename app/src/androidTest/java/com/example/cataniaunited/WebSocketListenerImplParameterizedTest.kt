package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
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
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameter
import org.junit.runners.Parameterized.Parameters
import org.mockito.Mockito.mock

@RunWith(Parameterized::class)
class WebSocketListenerImplParameterizedTest {

    private lateinit var mainApplication: MainApplication
    private lateinit var webSocketListener: WebSocketListenerImpl
    private val mockWebSocket = mock(WebSocket::class.java)

    @Before
    fun setup() {
        mainApplication = ApplicationProvider.getApplicationContext()
        MainApplication.getInstance().setPlayerId("")
        webSocketListener = WebSocketListenerImpl()
    }

    companion object {
        @JvmStatic
        @Parameters(name = "Test with type={0}, shouldProcess={1}")
        fun data() = listOf(
            arrayOf(MessageType.CONNECTION_SUCCESSFUL, true),
            arrayOf(MessageType.LOBBY_UPDATED, false)
        )
    }

    @Parameter(0)
    @JvmField
    var messageType: MessageType? = null

    @Parameter(1)
    @JvmField
    var shouldProcess: Boolean = false

    @Test
    fun testDifferentMessageTypes() {
        val expectedPlayerId = "1234567890"
        val message = buildJsonObject {
            put("playerId", expectedPlayerId)
        }
        val messageDTO: MessageDTO? = messageType?.let {
            MessageDTO(
                type = it,
                message = message
            )
        }

        webSocketListener.onMessage(mockWebSocket, messageDTO.toString())

        if (shouldProcess) {
            Assert.assertEquals(expectedPlayerId, MainApplication.getInstance().getPlayerId())
        } else {
            Assert.assertEquals("", MainApplication.getInstance().getPlayerId())
        }
    }
}