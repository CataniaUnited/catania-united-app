package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cataniaunited.ws.WebSocketListenerImpl
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
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
        assertNotNull(mainApplication.webSocketClient)
    }

    @Test
    fun onCreateShouldConnectWebSocketManager() {
        //creating web socket listener mock
        val mockListener = mock(WebSocketListenerImpl::class.java)

        val spyApplication = spy(mainApplication)
        val realManager = spyApplication.webSocketClient

        val spyManager = spy(realManager)
        spyApplication.webSocketClient = spyManager

        spyApplication.onCreate()

        verify(spyManager).connect(mockListener)
    }
}