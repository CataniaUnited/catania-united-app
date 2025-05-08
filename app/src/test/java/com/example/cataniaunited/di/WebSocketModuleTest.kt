package com.example.cataniaunited.di

import android.app.Application
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.data.GameDataHandler
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.callback.OnConnectionSuccess
import com.example.cataniaunited.ws.callback.OnGameBoardReceived
import com.example.cataniaunited.ws.callback.OnLobbyCreated
import com.example.cataniaunited.ws.callback.OnWebSocketClosed
import com.example.cataniaunited.ws.callback.OnWebSocketError
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WebSocketModuleTest {

    private lateinit var mockMainApplication: MainApplication
    private lateinit var mockGameDataHandler: GameDataHandler

    @BeforeEach
    fun setUp() {
        mockMainApplication = mockk<MainApplication>(relaxed = true)
        mockGameDataHandler = mockk<GameDataHandler>(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun provideOnConnectionSuccess_returnsMainApplicationAsOnConnectionSuccess() {
        val callback = WebSocketModule.provideOnConnectionSuccess(mockMainApplication)

        assertNotNull(callback)
        assertTrue(callback is OnConnectionSuccess)
        assertSame(mockMainApplication, callback)
    }

    @Test
    fun provideOnConnectionSuccess_throwsClassCastException_whenApplicationIsNotMainApplication() {
        val mockApplication = mockk<Application>()

        assertThrows<ClassCastException> {
            WebSocketModule.provideOnConnectionSuccess(mockApplication)
        }
    }

    @Test
    fun provideOnLobbyCreated_returnsMainApplicationAsOnLobbyCreated() {
        val callback = WebSocketModule.provideOnLobbyCreated(mockMainApplication)

        assertNotNull(callback)
        assertTrue(callback is OnLobbyCreated)
        assertSame(mockMainApplication, callback)
    }

    @Test
    fun provideOnLobbyCreated_throwsClassCastException_whenApplicationIsNotMainApplication() {
        val mockApplication = mockk<Application>()

        assertThrows<ClassCastException> {
            WebSocketModule.provideOnLobbyCreated(mockApplication)
        }
    }


    @Test
    fun provideOnGameBoardReceived_returnsMainApplicationAsOnGameBoardReceived() {
        val callback = WebSocketModule.provideOnGameBoardReceived(mockMainApplication)

        assertNotNull(callback)
        assertTrue(callback is OnGameBoardReceived)
        assertSame(mockMainApplication, callback)
    }

    @Test
    fun provideOnGameBoardReceived_throwsClassCastException_whenApplicationIsNotMainApplication() {
        val mockApplication = mockk<Application>()

        assertThrows<ClassCastException> {
            WebSocketModule.provideOnGameBoardReceived(mockApplication)
        }
    }


    @Test
    fun provideOnWebSocketError_returnsMainApplicationAsOnWebSocketError() {
        val callback = WebSocketModule.provideOnWebSocketError(mockMainApplication)

        assertNotNull(callback)
        assertTrue(callback is OnWebSocketError)
        assertSame(mockMainApplication, callback)
    }

    @Test
    fun provideOnWebSocketError_throwsClassCastException_whenApplicationIsNotMainApplication() {
        val mockApplication = mockk<Application>()

        assertThrows<ClassCastException> {
            WebSocketModule.provideOnWebSocketError(mockApplication)
        }
    }


    @Test
    fun provideOnWebSocketClosed_returnsMainApplicationAsOnWebSocketClosed() {
        val callback = WebSocketModule.provideOnWebSocketClosed(mockMainApplication)

        assertNotNull(callback)
        assertTrue(callback is OnWebSocketClosed)
        assertSame(mockMainApplication, callback)
    }

    @Test
    fun provideOnWebSocketClosed_throwsClassCastException_whenApplicationIsNotMainApplication() {
        val mockApplication = mockk<Application>()

        assertThrows<ClassCastException> {
            WebSocketModule.provideOnWebSocketClosed(mockApplication)
        }
    }


    @Test
    fun provideWebSocketListener_returnsWebSocketListenerImpl() {
        val listener = WebSocketModule.provideWebSocketListener(
            onConnectionSuccess = mockMainApplication,
            onLobbyCreated = mockMainApplication,
            onGameBoardReceived = mockMainApplication,
            onError = mockMainApplication,
            onClosed = mockMainApplication,
            gameDataHandler = mockGameDataHandler
        )

        assertNotNull(listener)
        assertTrue(listener is WebSocketListenerImpl)
    }
}