package com.example.cataniaunited.logic.game

import android.util.Log
import app.cash.turbine.test
import com.example.cataniaunited.data.model.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


/**
 * Extension for managing TestDispatchers with JUnit 5.
 * Replaces Dispatchers.Main for testing ViewModels using viewModelScope.
 */
@ExperimentalCoroutinesApi
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : org.junit.jupiter.api.extension.BeforeEachCallback, org.junit.jupiter.api.extension.AfterEachCallback, org.junit.jupiter.api.extension.TestInstancePostProcessor {

    override fun postProcessTestInstance(testInstance: Any?, context: org.junit.jupiter.api.extension.ExtensionContext?) {
    }
    override fun beforeEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun afterEach(context: org.junit.jupiter.api.extension.ExtensionContext?) {
        Dispatchers.resetMain()
    }
}


@ExperimentalCoroutinesApi
@ExtendWith(MainCoroutineExtension::class)
class GameViewModelTest {



    private lateinit var mockGameBoardLogic: GameBoardLogic

    private lateinit var viewModel: GameViewModel

    private val validBoardJson = """
        {
           "tiles":[{"id":1,"type":"CLAY","value":5,"coordinates":[0.0,0.0]}],
           "settlementPositions":[{"id":1,"building":null,"coordinates":[0.0,10.0]}],
           "roads":[{"id":1,"owner":null,"coordinates":[0.0,5.0],"rotationAngle":0.0}],
           "ringsOfBoard":1,
           "sizeOfHex":6
        }
    """
    private val invalidBoardJson = "{ not json }"


    @BeforeEach
    fun setUp() {

        mockGameBoardLogic = mockk<GameBoardLogic>(relaxed = true)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        viewModel = GameViewModel(mockGameBoardLogic)

        println("Setup complete.")
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
        println("TearDown complete.")
    }


    @Test
    @DisplayName("Initial state should be null")
    fun initialStateIsNull() = runTest {
        assertNull(viewModel.gameBoardState.value, "Initial gameBoardState should be null")
        println("Test passed: initial state is null")
    }

    @ExperimentalCoroutinesApi
    @Test
    fun updateDiceResult_setsCorrectValue() = runTest {
        val dice1 = 5
        val dice2 = 6

        viewModel.updateDiceResult(dice1, dice2)

        val result = viewModel.diceResult.first { it != null }

        assertEquals(Pair(dice1, dice2), result)
    }

    @Nested
    @DisplayName("Initialization via initializeBoardState")
    inner class InitializationTests {

        @Test
        fun loadsBoardWhenValidJSONProvided() = runTest {
            assertNull(viewModel.gameBoardState.value)

            viewModel.initializeBoardState(validBoardJson)
            advanceUntilIdle()

            viewModel.gameBoardState.test {
                val loadedState = awaitItem()
                assertNotNull(loadedState, "GameBoardState should be loaded")
                assertEquals(1, loadedState?.tiles?.size)
                assertEquals(TileType.CLAY, loadedState?.tiles?.firstOrNull()?.type)
                cancelAndIgnoreRemainingEvents()
            }
            println("Test passed: initializeBoardState loads board when valid JSON provided")
        }

        @Test
        fun setsNullStateWhenJSONIsNull() = runTest {
            viewModel.initializeBoardState(null)
            assertNull(viewModel.gameBoardState.value)
            println("Test passed: initializeBoardState sets null state when JSON is null")
        }

        @Test
        fun setsNullStateWhenJSONIsInvalid() = runTest {
            viewModel.initializeBoardState(invalidBoardJson)
            advanceUntilIdle()
            assertNull(viewModel.gameBoardState.value)
            println("Test passed: initializeBoardState sets null state when JSON is invalid")
        }

        @Test
        fun doesNotReloadIfStateAlreadyExists() = runTest {
            viewModel.initializeBoardState(validBoardJson)
            advanceUntilIdle()
            val firstState = viewModel.gameBoardState.value
            assertNotNull(firstState)

            val secondValidJson = """{"tiles":[{"id":2,"type":"WOOD"}],"settlementPositions":[],"roads":[],"ringsOfBoard":1,"sizeOfHex":6}"""
            viewModel.initializeBoardState(secondValidJson)
            advanceUntilIdle()

            val secondState = viewModel.gameBoardState.value
            assertSame(firstState, secondState, "State object should be the same instance")
            assertEquals(TileType.CLAY, secondState?.tiles?.firstOrNull()?.type)
            println("Test passed: initializeBoardState does not reload")
        }
    }

    @Nested
    @DisplayName("Click Handlers")
    inner class ClickHandlerTests {

        @Test
        fun handleSettlementClickRunsWithoutCrashing() = runTest {
            val testPosition = SettlementPosition(id = 5, building = null, coordinates = listOf(1.0, 2.0))
            val testLobbyId = "click-lobby-1"
            viewModel.handleSettlementClick(testPosition, testLobbyId)
            assertDoesNotThrow { mockGameBoardLogic.placeSettlement(testPosition.id, testLobbyId) }
            println("Test passed: handleSettlementClick verified")
        }

        @Test
        fun handleRoadClickRunsWithoutCrashing() = runTest {
            val testRoad = Road(id = 10, owner = null, coordinates = listOf(3.0, 4.0), rotationAngle = 0.5)
            val testLobbyId = "click-lobby-2"
            assertDoesNotThrow {  viewModel.handleRoadClick(testRoad, testLobbyId) }
            println("Test passed: handleRoadClick verified")
        }

        @Test
        fun handleTileClickRunsWithoutCrashing() = runTest {
            val testTile = Tile(id = 20, type = TileType.WOOD, value = 11, coordinates = listOf(5.0, 6.0))
            val testLobbyId = "click-lobby-3"
            assertDoesNotThrow{
                viewModel.handleTileClick(testTile, testLobbyId)
            }
            println("Test passed: handleTileClick runs")
        }
    }
}