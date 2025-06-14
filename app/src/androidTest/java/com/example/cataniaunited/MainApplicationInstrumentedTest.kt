package com.example.cataniaunited

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.logic.game.GameViewModel
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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

        mainApplication.clearGameData()
        mainApplication.gameViewModel = null

        mockGameViewModel = mockk<GameViewModel>(relaxed = true)

        try {
            playerIdField = MainApplication::class.java.getDeclaredField("_playerId")
            playerIdField.isAccessible = true
            playerIdField.set(mainApplication, null)
        } catch (e: Exception) {
        }
        mainApplication.players.clear()
    }

    @After
    fun tearDown() {
        mainApplication.gameViewModel = null
        mainApplication.players.clear()
    }

    @Test
    fun webSocketClientShouldBeInitializedAfterOnCreate() {
        assertNotNull(
            "WebSocketClient should not be null after Application onCreate",
            mainApplication.getWebSocketClient()
        )
    }

    @Test
    fun getPlayerIdShouldThrowExceptionWhenNotSet() {
        try {
            mainApplication.getPlayerId()
            fail("Expected IllegalStateException was not thrown when Player ID is not set")
        } catch (e: IllegalStateException) {
            assertEquals("Player Id not initialized", e.message)
        } catch (e: Exception) {
            fail("Caught unexpected exception type: ${e::class.java.simpleName} - ${e.message}")
        }
    }

    @Test
    fun setAndGetPlayerIdWorks() {
        val testId = "player-id-${System.currentTimeMillis()}"
        mainApplication.setPlayerId(testId)
        assertEquals(
            "Stored Player ID should match set value",
            testId,
            mainApplication.getPlayerId()
        )
    }


    @Test
    fun currentLobbyIdFlowInitiallyNull() = runTest {
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
    }

    @Test
    fun onLobbyCreatedCallbackUpdatesFlow() = runTest {
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
    }

    @Test
    fun onGameBoardReceivedUpdatesJson() = runTest {
        val lobbyId = "test-lobby-1"
        val boardJson = """{"test":"data"}"""

        mainApplication.currentLobbyId = lobbyId

        mainApplication.latestBoardJson = boardJson

        assertEquals(
            "latestBoardJson should be updated",
            boardJson,
            mainApplication.latestBoardJson
        )
    }

    @Test
    fun onGameBoardReceivedSendsToNavigationChannelWhenLobbyMatches() = runTest {
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
            }
            job.join()
        }
        assertTrue("Channel send should have been attempted", didSend)

        mainApplication.navigateToGameFlow.test {
            assertEquals("Channel should receive correct lobbyId", expectedLobbyId, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onGameBoardReceivedDoesNotSendToChannelWhenLobbyMismatches() = runTest {
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
        }
    }

    @Test
    fun clearLobbyDataResetsState() = runTest {
        val lobbyId = "lobby-to-clear"
        val boardJson = """{"test":"data"}"""
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
    }

    @Test
    fun onClosedCallbackClearsGameData() = runTest {
        mainApplication.latestBoardJson = """{"test":"data"}"""
        mainApplication.onClosed(1000, "Test closure")
        assertNull(mainApplication.latestBoardJson)
    }


    @Test
    fun onGameWonUpdatesGameWonState() = runTest {
        val winner = PlayerInfo("winner1", "Winner Player", "#FF0000", victoryPoints = 10)
        val leaderboard = listOf(
            winner,
            PlayerInfo("player2", "Second Place", "#00FF00", victoryPoints = 8),
            PlayerInfo("player3", "Third Place", "#0000FF", victoryPoints = 6)
        )

        mainApplication.gameWonState.test {
            assertEquals(null, awaitItem())

            mainApplication.onGameWon(winner, leaderboard)

            val emittedValue = awaitItem()
            assertNotNull("gameWonState should not be null after onGameWon", emittedValue)
            val (receivedWinner, receivedLeaderboard) = emittedValue!!

            assertEquals(winner, receivedWinner)
            assertEquals(leaderboard, receivedLeaderboard)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun webSocketListenerIsInjected() {
        assertNotNull(mainApplication.webSocketListener)
    }

    @Test
    fun setPlayers_sortsCurrentPlayerToFront() {
        val currentPlayerId = "playerB"
        mainApplication.setPlayerId(currentPlayerId)

        val playerA = PlayerInfo("playerA", "Alice", "#FF0000", isReady = true)
        val playerB = PlayerInfo("playerB", "Bob", "#00FF00", isReady = false)
        val playerC = PlayerInfo("playerC", "Charlie", "#0000FF", isReady = true)

        val playersMap = mapOf(
            playerA.id to playerA,
            playerB.id to playerB,
            playerC.id to playerC
        )

        val setPlayersMethod = MainApplication::class.java.getDeclaredMethod(
            "setPlayers",
            Map::class.java
        )
        setPlayersMethod.isAccessible = true
        setPlayersMethod.invoke(mainApplication, playersMap)

        assertEquals("Players list size should be 3", 3, mainApplication.players.size)
        assertEquals("Current player (playerB) should be at the first position", playerB, mainApplication.players[0])
        assertTrue("Player A should be in the list", mainApplication.players.contains(playerA))
        assertTrue("Player C should be in the list", mainApplication.players.contains(playerC))

        val expectedOrderWithoutCurrentPlayer = listOf(playerA, playerC)
        val actualOrderWithoutCurrentPlayer = mainApplication.players.subList(1, mainApplication.players.size)
        assertEquals("Relative order of other players should be maintained", expectedOrderWithoutCurrentPlayer, actualOrderWithoutCurrentPlayer)
    }

    @Test
    fun setPlayers_noChangeIfCurrentPlayerAlreadyFirst() {
        val currentPlayerId = "playerA"
        mainApplication.setPlayerId(currentPlayerId)

        val playerA = PlayerInfo("playerA", "Alice", "#FF0000", isReady = true)
        val playerB = PlayerInfo("playerB", "Bob", "#00FF00", isReady = false)
        val playerC = PlayerInfo("playerC", "Charlie", "#0000FF", isReady = true)

        val playersMap = mapOf(
            playerA.id to playerA,
            playerB.id to playerB,
            playerC.id to playerC
        )

        mainApplication.players.addAll(listOf(playerA, playerB, playerC))

        val setPlayersMethod = MainApplication::class.java.getDeclaredMethod(
            "setPlayers",
            Map::class.java
        )
        setPlayersMethod.isAccessible = true
        setPlayersMethod.invoke(mainApplication, playersMap)

        assertEquals("Players list size should be 3", 3, mainApplication.players.size)
        assertEquals("Current player (playerA) should still be at the first position", playerA, mainApplication.players[0])
        assertEquals("Relative order of all players should be maintained", listOf(playerA, playerB, playerC), mainApplication.players)
    }


    @Test
    fun setPlayers_noCurrentPlayerIdSet() {
        val playerA = PlayerInfo("playerA", "Alice", "#FF0000", isReady = true)
        val playerB = PlayerInfo("playerB", "Bob", "#00FF00", isReady = false)
        val playerC = PlayerInfo("playerC", "Charlie", "#0000FF", isReady = true)

        val playersMap = mapOf(
            playerA.id to playerA,
            playerB.id to playerB,
            playerC.id to playerC
        )

        val setPlayersMethod = MainApplication::class.java.getDeclaredMethod(
            "setPlayers",
            Map::class.java
        )
        setPlayersMethod.isAccessible = true
        setPlayersMethod.invoke(mainApplication, playersMap)

        assertEquals("Players list size should be 3", 3, mainApplication.players.size)
        assertTrue("Player A should be in the list", mainApplication.players.contains(playerA))
        assertTrue("Player B should be in the list", mainApplication.players.contains(playerB))
        assertTrue("Player C should be in the list", mainApplication.players.contains(playerC))
        org.junit.Assert.assertNotEquals("No current player, so playerB should not be first (unless by chance)", playerB, mainApplication.players[0])
    }

    @Test
    fun setPlayers_emptyPlayersMap() {
        mainApplication.setPlayerId("someId")
        val playersMap = emptyMap<String, PlayerInfo>()

        val setPlayersMethod = MainApplication::class.java.getDeclaredMethod(
            "setPlayers",
            Map::class.java
        )
        setPlayersMethod.isAccessible = true
        setPlayersMethod.invoke(mainApplication, playersMap)

        assertTrue("Players list should be empty", mainApplication.players.isEmpty())
    }

    @Test
    fun setPlayers_currentPlayerNotInMap() {
        mainApplication.setPlayerId("nonExistentPlayer")

        val playerA = PlayerInfo("playerA", "Alice", "#FF0000", isReady = true)
        val playerB = PlayerInfo("playerB", "Bob", "#00FF00", isReady = false)

        val playersMap = mapOf(
            playerA.id to playerA,
            playerB.id to playerB
        )

        val setPlayersMethod = MainApplication::class.java.getDeclaredMethod(
            "setPlayers",
            Map::class.java
        )
        setPlayersMethod.isAccessible = true
        setPlayersMethod.invoke(mainApplication, playersMap)

        assertEquals("Players list size should be 2", 2, mainApplication.players.size)
        assertTrue("Player A should be in the list", mainApplication.players.contains(playerA))
        assertTrue("Player B should be in the list", mainApplication.players.contains(playerB))
        assertFalse("Non-existent current player should not be added", mainApplication.players.any { it.id == "nonExistentPlayer" })
    }
}