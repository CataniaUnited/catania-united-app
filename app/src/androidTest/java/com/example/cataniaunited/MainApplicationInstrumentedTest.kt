package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ws.WebSocketListenerImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.spy

@RunWith(AndroidJUnit4::class)
class MainApplicationInstrumentedTest {

    private lateinit var mainApplication: MainApplication

    @Before
    fun setup() {
        mainApplication = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testWebSocketManagerInitialization() {
        assertNotNull(mainApplication.getWebSocketClient())
    }

    @Test
    fun onCreateShouldConnectWebSocketManager() {
        val realManager = mainApplication.webSocketClient
        try {
            val spyManager = spy(WebSocketClient("http://localhost/"))
            mainApplication.webSocketClient = spyManager

            mainApplication.onCreate()

            verify(spyManager).connect(any<WebSocketListenerImpl>())
        } finally {
            mainApplication.webSocketClient = realManager
        }

    }

    @Test
    fun getPlayerIdShouldThrowExceptionIfPlayerIdIsNotSet() {
        try {
            mainApplication.getPlayerId();
            fail("Expected exception not thrown")
        } catch (iae: IllegalStateException) {
            assertEquals("Player Id not initialized", iae.message)
        }
    }
}