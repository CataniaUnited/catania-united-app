package com.example.cataniaunited.logic.game

import android.util.Log
import app.cash.turbine.test
import com.example.cataniaunited.data.GameDataHandler
import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.logic.player.PlayerSessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExperimentalCoroutinesApi
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : org.junit.jupiter.api.extension.BeforeEachCallback,
    org.junit.jupiter.api.extension.AfterEachCallback {

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

    private lateinit var mockPlayerSessionManager: PlayerSessionManager
    private lateinit var mockGameDataHandler: GameDataHandler
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
    private val anotherValidBoardJson = """
        {
           "tiles":[{"id":2,"type":"WOOD","value":6,"coordinates":[1.0,1.0]}],
           "settlementPositions":[{"id":2,"building":null,"coordinates":[1.0,11.0]}],
           "roads":[{"id":2,"owner":null,"coordinates":[1.0,6.0],"rotationAngle":0.0}],
           "ringsOfBoard":1,
           "sizeOfHex":6
        }
    """
    private val invalidBoardJson = "{ not json }"

    private lateinit var gameBoardMutableStateFlow: MutableStateFlow<GameBoardModel?>


    @BeforeEach
    fun setUp() {
        gameBoardMutableStateFlow = MutableStateFlow<GameBoardModel?>(null)

        unmockkStatic(Log::class)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockGameBoardLogic = mockk(relaxed = true)
        mockPlayerSessionManager = mockk(relaxed = true)

        mockGameDataHandler = mockk {
            every { gameBoardState } returns gameBoardMutableStateFlow.asStateFlow()

            every { updateGameBoard(any<String>()) } answers {
                val json = it.invocation.args[0] as String
                println("Mock GameDataHandler.updateGameBoard called with: $json")
                when (json) {
                    validBoardJson -> {
                        gameBoardMutableStateFlow.value = GameBoardModel(
                            tiles = listOf(
                                Tile(
                                    id = 1,
                                    type = TileType.CLAY,
                                    value = 5,
                                    coordinates = listOf(0.0, 0.0)
                                )
                            ),
                            settlementPositions = listOf(
                                SettlementPosition(
                                    id = 1,
                                    building = null,
                                    coordinates = listOf(0.0, 10.0)
                                )
                            ),
                            roads = listOf(
                                Road(
                                    id = 1,
                                    owner = null,
                                    coordinates = listOf(0.0, 5.0),
                                    rotationAngle = 0.0,
                                    color = "#000000"
                                )
                            ),
                            ringsOfBoard = 1,
                            sizeOfHex = 6
                        )
                    }

                    anotherValidBoardJson -> {
                        gameBoardMutableStateFlow.value = GameBoardModel(
                            tiles = listOf(
                                Tile(
                                    id = 2,
                                    type = TileType.WOOD,
                                    value = 6,
                                    coordinates = listOf(1.0, 1.0)
                                )
                            ),
                            settlementPositions = listOf(
                                SettlementPosition(
                                    id = 2,
                                    building = null,
                                    coordinates = listOf(1.0, 11.0)
                                )
                            ),
                            roads = listOf(
                                Road(
                                    id = 2,
                                    owner = null,
                                    coordinates = listOf(1.0, 6.0),
                                    rotationAngle = 0.0,
                                    color = "#000000"
                                )
                            ),
                            ringsOfBoard = 1,
                            sizeOfHex = 6
                        )
                    }

                    invalidBoardJson -> {
                        gameBoardMutableStateFlow.value = null
                    }

                    else -> {
                        gameBoardMutableStateFlow.value = null
                    }
                }
            }
        }

        viewModel = GameViewModel(mockGameBoardLogic, mockGameDataHandler, mockPlayerSessionManager)

        println("Setup complete.")
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
        println("TearDown complete.")
    }

    @Nested
    @DisplayName("Initialization via initializeBoardState")
    inner class InitializationTests {

        @Test
        fun loadsBoardWhenValidJSONProvided() = runTest {
            viewModel.gameBoardState.test {
                assertEquals(null, awaitItem())

                viewModel.initializeBoardState(validBoardJson)
                advanceUntilIdle()

                verify(exactly = 1) { mockGameDataHandler.updateGameBoard(validBoardJson) }

                val loadedState = awaitItem()

                assertNotNull(loadedState, "GameBoardState should be loaded")
                assertEquals(1, loadedState?.tiles?.size, "Loaded state should have 1 tile")
                assertEquals(
                    TileType.CLAY,
                    loadedState?.tiles?.firstOrNull()?.type,
                    "First tile type should be CLAY"
                )

                cancelAndIgnoreRemainingEvents()
            }
            println("Test passed: initializeBoardState loads board when valid JSON provided")
        }

        @Test
        fun setsNullStateWhenJSONIsInvalid() = runTest {
            viewModel.gameBoardState.test {
                assertEquals(null, awaitItem())

                viewModel.initializeBoardState(invalidBoardJson)
                advanceUntilIdle()

                verify(exactly = 1) { mockGameDataHandler.updateGameBoard(invalidBoardJson) }

                assertNull(
                    viewModel.gameBoardState.value,
                    "GameBoardState should remain null after invalid JSON"
                )
            }
            println("Test passed: initializeBoardState sets null state when JSON is invalid")
        }

        @Test
        fun doesNotCallLoadWhenJSONisNull() = runTest {
            viewModel.gameBoardState.test {
                assertEquals(null, awaitItem())

                viewModel.initializeBoardState(null)
                advanceUntilIdle()

                verify(exactly = 0) { mockGameDataHandler.updateGameBoard(any() as String) }

                assertNull(
                    viewModel.gameBoardState.value,
                    "GameBoardState should remain null after invalid initial JSON"
                )
            }
            println("Test passed: initializeBoardState sets null state when JSON is invalid")
        }

        @Test
        fun doesNotReloadIfStateAlreadyExists() = runTest {
            viewModel.initializeBoardState(validBoardJson)
            advanceUntilIdle()

            verify(exactly = 1) { mockGameDataHandler.updateGameBoard(validBoardJson) }

            val firstState = viewModel.gameBoardState.value
            assertNotNull(firstState, "First state should be loaded")

            viewModel.initializeBoardState(anotherValidBoardJson)
            advanceUntilIdle()

            verify(exactly = 1) { mockGameDataHandler.updateGameBoard(any<String>()) }
            verify(exactly = 1) { mockGameDataHandler.updateGameBoard(validBoardJson) }

            val secondState = viewModel.gameBoardState.value
            assertSame(firstState, secondState, "State object should be the same instance")
            assertEquals(
                1,
                secondState?.tiles?.size,
                "State content should still be from the first load"
            )

            println("Test passed: initializeBoardState does not reload")
        }
    }

    @Nested
    @DisplayName("loadGameBoardFromJson method")
    inner class LoadFromJsonTests {

        @Test
        @DisplayName("calls updateGameBoard and updates state when called with valid JSON")
        fun callsUpdateGameBoardAndUpdatesStateOnLoadFromJsonValidJson() = runTest {
            viewModel.gameBoardState.test {
                assertEquals(null, awaitItem())

                viewModel.loadGameBoardFromJson(validBoardJson)
                advanceUntilIdle()

                verify(exactly = 1) { mockGameDataHandler.updateGameBoard(validBoardJson) }

                val simulatedGameBoard = GameBoardModel(
                    tiles = listOf(
                        Tile(
                            id = 1,
                            type = TileType.CLAY,
                            value = 5,
                            coordinates = listOf(0.0, 0.0)
                        )
                    ),
                    settlementPositions = listOf(
                        SettlementPosition(
                            id = 1, building = null, coordinates = listOf(
                                0.0, 10.0
                            )
                        )
                    ), roads = listOf(
                        Road(
                            id = 1, owner = null, coordinates = listOf(
                                0.0, 5.0
                            ), rotationAngle = 0.0, color = "#000000"
                        )
                    ), ringsOfBoard = 1, sizeOfHex = 6
                )
                gameBoardMutableStateFlow.value = simulatedGameBoard

                val loadedState = awaitItem()
                assertNotNull(loadedState)
                assertEquals(simulatedGameBoard, loadedState)

                cancelAndIgnoreRemainingEvents()
            }
            println("Test passed: loadGameBoardFromJson calls updateGameBoard and updates state on valid JSON")
        }

        @Test
        @DisplayName("calls updateGameBoard and sets state to null when called with invalid JSON")
        fun callsUpdateGameBoardAndSetsStateToNullOnLoadFromJsonInvalidJson() = runTest {
            viewModel.gameBoardState.test {
                assertEquals(null, awaitItem())

                viewModel.loadGameBoardFromJson(invalidBoardJson)
                advanceUntilIdle()

                verify(exactly = 1) { mockGameDataHandler.updateGameBoard(invalidBoardJson) }

                gameBoardMutableStateFlow.value = null


                assertNull(
                    viewModel.gameBoardState.value,
                    "GameBoardState should be null after invalid JSON"
                )

                cancelAndIgnoreRemainingEvents()
            }
            println("Test passed: loadGameBoardFromJson calls updateGameBoard and sets state to null on invalid JSON")
        }
    }

    @Nested
    @DisplayName("Click Handlers")
    inner class ClickHandlerTests {

        @Test
        fun handleSettlementClickCallsGameBoardLogicPlaceSettlement() = runTest {
            val testPosition =
                SettlementPosition(id = 5, building = null, coordinates = listOf(1.0, 2.0))
            val testLobbyId = "click-lobby-1"

            viewModel.handleSettlementClick(testPosition, testLobbyId)
            advanceUntilIdle()

            verify(exactly = 1) { mockGameBoardLogic.placeSettlement(testPosition.id, testLobbyId) }

            println("Test passed: handleSettlementClick calls gameBoardLogic.placeSettlement")
        }

        @Test
        fun handleRoadClickCallsGameBoardLogicPlaceRoad() = runTest {
            val testRoad = Road(
                id = 10,
                owner = null,
                coordinates = listOf(3.0, 4.0),
                rotationAngle = 0.5,
                color = "#000000"
            )
            val testLobbyId = "click-lobby-2"

            viewModel.handleRoadClick(testRoad, testLobbyId)
            advanceUntilIdle()

            verify(exactly = 1) { mockGameBoardLogic.placeRoad(testRoad.id, testLobbyId) }

            println("Test passed: handleRoadClick calls gameBoardLogic.placeRoad")
        }
    }

    @Nested
    @DisplayName("Build Menu State")
    inner class BuildMenuStateTests {

        @Test
        fun isBuildMenuOpenDefaultsToFalse() = runTest {
            viewModel.isBuildMenuOpen.test {
                assertEquals(false, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(
                false,
                viewModel.isBuildMenuOpen.value,
                "isBuildMenuOpen value should default to false"
            )
            println("Test passed: isBuildMenuOpen defaults to false")
        }

        @Test
        fun setBuildMenuOpenSetsStateToTrue() = runTest {
            viewModel.isBuildMenuOpen.test {
                assertEquals(false, awaitItem())

                viewModel.setBuildMenuOpen(true)
                advanceUntilIdle()

                assertEquals(true, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
            println("Test passed: setBuildMenuOpen sets state to true")
        }

        @Test
        fun setBuildMenuOpenSetsStateToFalse() = runTest {
            viewModel.setBuildMenuOpen(true)
            advanceUntilIdle()
            assertEquals(
                true,
                viewModel.isBuildMenuOpen.value,
                "State should be true before setting to false"
            )

            viewModel.isBuildMenuOpen.test {
                viewModel.setBuildMenuOpen(false)
                advanceUntilIdle()

                assertEquals(true, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
            assertEquals(false, viewModel.isBuildMenuOpen.value, "Final state should be false")

            println("Test passed: setBuildMenuOpen sets state to false")
        }
    }

    @Test
    fun playerIdReturnsPlayerIdFromSessionManager() = runTest {
        val testPlayerId = "test-player-123"
        every { mockPlayerSessionManager.getPlayerId() } returns testPlayerId

        val retrievedPlayerId = viewModel.playerId

        assertEquals(testPlayerId, retrievedPlayerId)

        verify(exactly = 1) { mockPlayerSessionManager.getPlayerId() }

        println("Test passed: playerId returns value from session manager")
    }
}