package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainApplicationInstrumentedTest {

    private lateinit var mainApplication: MainApplication

    @Before
    fun setup() {
        mainApplication = ApplicationProvider.getApplicationContext<MainApplication>()
        println("Setup: Using existing Application instance: $mainApplication")

    }

    @Test
    fun webSocketClientShouldBeInitializedAfterOnCreate() {
        println("Running webSocketClientShouldBeInitializedAfterOnCreate...")
        assertNotNull("WebSocketClient should not be null after Application onCreate", mainApplication.getWebSocketClient())
        println("Test Passed.")
    }

    @Test
    fun getPlayerIdShouldThrowExceptionWhenNotSet() {
        println("Running getPlayerIdShouldThrowExceptionWhenNotSet...")
        try {
            println("Attempting to get Player ID (expecting exception)...")
            mainApplication.getPlayerId()
            fail("Expected IllegalStateException was not thrown when Player ID is not set")
        } catch (e: IllegalStateException) {
            assertEquals("Player Id not initialized", e.message)
            println("Test Passed: Correct exception thrown.")
        } catch (e: Exception) {
            fail("Caught unexpected exception type: ${e::class.java.simpleName} - ${e.message}")
        }
    }

    /*
    @Test
    fun onCreateShouldConnectWebSocketManager() {

    }
    */

    @Test
    fun setAndGetPlayerIdWorks() {
        println("Running setAndGetPlayerIdWorks...")
        val testId = "player-id-${System.currentTimeMillis()}"
        mainApplication.setPlayerId(testId)
        assertEquals("Stored Player ID should match the set value", testId, mainApplication.getPlayerId())
        println("Test Passed.")
    }

    @Test
    fun currentLobbyIdFlowInitiallyNull() {
        println("Running currentLobbyIdFlowInitiallyNull...")
        assertNull("Initial lobby ID flow value should be null", mainApplication.currentLobbyIdFlow.value)
        println("Test Passed.")
    }

}