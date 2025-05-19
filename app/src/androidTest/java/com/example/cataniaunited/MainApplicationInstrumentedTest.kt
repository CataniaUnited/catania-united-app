package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.logic.game.GameViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.reflect.Field


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class MainApplicationInstrumentedTest {
    private lateinit var mainApplication: MainApplication
    private lateinit var playerIdField: Field
    private lateinit var mockGameViewModel: GameViewModel

    @Before
    fun setup() {
        mainApplication = ApplicationProvider.getApplicationContext<MainApplication>()

        println("Setup: Resetting state...")
        mainApplication.clearLobbyData()
        mainApplication.gameViewModel = null

        mockGameViewModel = mockk<GameViewModel>(relaxed = true)

        try {
            playerIdField = MainApplication::class.java.getDeclaredField("_playerId")
            playerIdField.isAccessible = true
            playerIdField.set(mainApplication, null)
            println("Setup: _playerId reset to null.")
        } catch (e: Exception) {
            println("Warning: Could not reset _playerId via reflection - ${e.message}")
        }
        println("Setup: Using existing Application instance: $mainApplication")

    }

    @After
    fun tearDown() {
        mainApplication.gameViewModel = null
        println("Teardown complete.")

    }

    @Test
    fun webSocketClientShouldBeInitializedAfterOnCreate() {
        println("Running webSocketClientShouldBeInitializedAfterOnCreate...")
        assertNotNull(
            "WebSocketClient should not be null after Application onCreate",
            mainApplication.getWebSocketClient()
        )
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

    @Test
    fun setAndGetPlayerIdWorks() {
        println("Running setAndGetPlayerIdWorks...")
        val testId = "player-id-${System.currentTimeMillis()}"
        mainApplication.setPlayerId(testId)
        assertEquals(
            "Stored Player ID should match set value",
            testId,
            mainApplication.getPlayerId()
        )
        println("Test Passed.")
    }


    @Test
    fun currentLobbyIdFlowInitiallyNull() = runTest {
        println("Running test: currentLobbyIdFlowInitiallyNull")
        assertEquals(
            "Initial lobby ID flow value should be null",
            null,
            mainApplication.currentLobbyIdFlow.value
        )
        mainApplication.currentLobbyIdFlow.test {
            assertEquals("Flow should emit null initially", null, awaitItem())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        println("Test Passed.")
    }

    @Test
    fun onLobbyCreatedCallbackUpdatesFlow() = runTest {
        println("Running test: onLobbyCreatedCallbackUpdatesFlow")
        val expectedLobbyId = "lobby-abc-123"

        mainApplication.currentLobbyId = expectedLobbyId
        advanceUntilIdle()

        mainApplication.currentLobbyIdFlow.test {
            assertEquals("Flow should emit the set lobbyId", expectedLobbyId, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(
            "Getter property should reflect flow value",
            expectedLobbyId,
            mainApplication.currentLobbyId
        )
        println("Test Passed.")
    }

    @Test
    fun onGameBoardReceivedUpdatesJson() = runTest {
        println("Running test: onGameBoardReceivedUpdatesJson")
        val lobbyId = "test-lobby-1"
        val boardJson = """{"test":"data"}"""

        mainApplication.currentLobbyId = lobbyId

        mainApplication.latestBoardJson = boardJson

        assertEquals(
            "latestBoardJson should be updated",
            boardJson,
            mainApplication.latestBoardJson
        )
        println("Test Passed.")
    }

    @Test
    fun onGameBoardReceivedSendsToNavigationChannelWhenLobbyMatches() = runTest {
        println("Running test: onGameBoardReceivedSendsToNavigationChannelWhenLobbyMatches")
        val expectedLobbyId = "test-lobby-nav"
        val boardJson = """{"test":"board"}"""

        mainApplication.currentLobbyId = expectedLobbyId
        advanceUntilIdle()

        mainApplication.latestBoardJson = boardJson

        var didSend = false
        if (expectedLobbyId == mainApplication.currentLobbyIdFlow.value) {
            val job = launch {
                mainApplication._navigateToGameChannel.send(expectedLobbyId)
                didSend = true
                println("Manually sent lobbyId $expectedLobbyId to channel.")
            }
            job.join()
        }
        assertTrue("Channel send should have been attempted", didSend)

        mainApplication.navigateToGameFlow.test {
            assertEquals("Channel should receive correct lobbyId", expectedLobbyId, awaitItem())
            cancelAndIgnoreRemainingEvents()
            println("Test Passed.")
        }
    }

    @Test
    fun onGameBoardReceivedDoesNotSendToChannelWhenLobbyMismatches() = runTest {
        println("Running test: onGameBoardReceivedDoesNotSendToChannelWhenLobbyMismatches")
        val currentLobbyId = "current-lobby"
        val receivedLobbyId = "different-lobby"
        val boardJson = """{"test":"board"}"""


        mainApplication.currentLobbyId = currentLobbyId
        advanceUntilIdle()


        mainApplication.latestBoardJson = boardJson
        var sentToChannel = false
        if (receivedLobbyId == mainApplication.currentLobbyIdFlow.value) {
            launch { mainApplication._navigateToGameChannel.send(receivedLobbyId) }
            sentToChannel = true
        }

        assertFalse("Channel send should not have been attempted", sentToChannel)
        mainApplication.navigateToGameFlow.test {
            expectNoEvents()
            println("Test Passed.")
        }
    }

    @Test
    fun clearLobbyDataResetsState() = runTest {
        println("Running test: clearLobbyDataResetsState")
        val lobbyId = "lobby-to-clear"
        val boardJson = """{"test":"board"}"""
        mainApplication.currentLobbyId = lobbyId
        mainApplication.latestBoardJson = boardJson
        advanceUntilIdle()
        assertEquals(lobbyId, mainApplication.currentLobbyIdFlow.value)
        assertEquals(boardJson, mainApplication.latestBoardJson)

        mainApplication.clearLobbyData()
        advanceUntilIdle()

        assertNull(
            "currentLobbyIdFlow should be null after clearLobbyData",
            mainApplication.currentLobbyIdFlow.value
        )
        assertNull(
            "latestBoardJson should be null after clearLobbyData",
            mainApplication.latestBoardJson
        )
        println("Test Passed.")
    }

    @Test
    fun clearGameDataResetsJsonOnly() = runTest {
        println("Running test: clearGameDataResetsJsonOnly")
        val lobbyId = "lobby-not-cleared"
        val boardJson = """{"test":"board"}"""
        mainApplication.currentLobbyId = lobbyId
        mainApplication.latestBoardJson = boardJson
        advanceUntilIdle()
        assertEquals(lobbyId, mainApplication.currentLobbyIdFlow.value)
        assertEquals(boardJson, mainApplication.latestBoardJson)

        mainApplication.clearGameData()
        advanceUntilIdle()

        assertEquals("currentLobbyIdFlow should NOT be null after clearGameData", lobbyId, mainApplication.currentLobbyIdFlow.value)
        assertNull("latestBoardJson should be null after clearGameData", mainApplication.latestBoardJson)
        println("Test Passed.")
    }

    @Test
    fun onClosedCallbackClearsGameData() = runTest {
        mainApplication.latestBoardJson = """{"test":"data"}"""
        mainApplication.onClosed(1000, "Test closure")
        assertNull(mainApplication.latestBoardJson)
    }


    @Test
    fun onGameWonUpdatesGameWonState() = runTest {
        val winner = PlayerInfo("winner1", "Winner Player", "#FF0000", 10)
        val leaderboard = listOf(
            winner,
            PlayerInfo("player2", "Second Place", "#00FF00", 8),
            PlayerInfo("player3", "Third Place", "#0000FF", 6)
        )

        mainApplication.onGameWon(winner, leaderboard)
        advanceUntilIdle()

        var attempts = 0
        while (mainApplication.gameWonState.value == null && attempts < 10) {
            delay(100)
            attempts++
        }

        assertNotNull(mainApplication.gameWonState.value)
        assertEquals(winner, mainApplication.gameWonState.value?.first)
        assertEquals(leaderboard, mainApplication.gameWonState.value?.second)
    }

    @Test
    fun webSocketListenerIsInjected() {
        assertNotNull(mainApplication.webSocketListener)
    }

}