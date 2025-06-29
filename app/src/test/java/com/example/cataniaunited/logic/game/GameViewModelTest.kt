package com.example.cataniaunited.logic.game

import android.util.Log
import app.cash.turbine.test
import com.example.cataniaunited.data.model.Building
import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.logic.dto.TradeRequest
import com.example.cataniaunited.logic.lobby.LobbyLogic
import com.example.cataniaunited.logic.player.PlayerSessionManager
import com.example.cataniaunited.logic.trade.TradeLogic
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext



@ExperimentalCoroutinesApi
class MainCoroutineExtension(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : BeforeEachCallback,
    AfterEachCallback {

    override fun beforeEach(context: ExtensionContext?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun afterEach(context: ExtensionContext?) {
        Dispatchers.resetMain()
    }
}


@ExperimentalCoroutinesApi
@ExtendWith(MainCoroutineExtension::class)
class GameViewModelTest {

    private lateinit var mockPlayerSessionManager: PlayerSessionManager
    private lateinit var mockGameDataHandler: GameDataHandler
    private lateinit var mockGameBoardLogic: GameBoardLogic
    private lateinit var mockLobbyLogic: LobbyLogic
    private lateinit var mockTradeLogic: TradeLogic
    private lateinit var mockCheatingLogic: CheatingLogic
    private lateinit var viewModel: GameViewModel

    private val validBoardJson = """
        {
           "tiles":[{"id":1,"type":"CLAY","value":5,"coordinates":[0.0,0.0]}],
           "settlementPositions":[{"id":1,"building":null,"coordinates":[0.0,10.0]}],
           "roads":[{"id":1,"owner":null,"color":null,"coordinates":[0.0,5.0],"rotationAngle":0.0}],
           "ports": [],
           "ringsOfBoard":1,
           "sizeOfHex":6
        }
    """
    private val anotherValidBoardJson = """
        {
           "tiles":[{"id":2,"type":"WOOD","value":6,"coordinates":[1.0,1.0]}],
           "settlementPositions":[{"id":2,"building":null,"coordinates":[1.0,11.0]}],
           "roads":[{"id":2,"owner":null,"color":null,"coordinates":[1.0,6.0],"rotationAngle":0.0}],
           "ports": [],
           "ringsOfBoard":1,
           "sizeOfHex":6
        }
    """
    private val invalidBoardJson = "{ not json }"

    private lateinit var gameBoardMutableStateFlow: MutableStateFlow<GameBoardModel?>
    private lateinit var victoryPointsMutableStateFlow: MutableStateFlow<Map<String, Int>>
    private lateinit var playersMutableStateFlow: MutableStateFlow<Map<String, PlayerInfo>>
    private lateinit var diceMutableStateFlow: MutableStateFlow<GameViewModel.DiceState?>
    private lateinit var snackbarMessageFlow: MutableStateFlow<Pair<String, String>?>
    private val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())
    private val testPlayerId = "testPlayerId"


    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        gameBoardMutableStateFlow = MutableStateFlow<GameBoardModel?>(null)
        victoryPointsMutableStateFlow = MutableStateFlow(emptyMap())
        playersMutableStateFlow = MutableStateFlow(emptyMap()) // Initialize players flow
        diceMutableStateFlow = MutableStateFlow(null)
        snackbarMessageFlow = MutableStateFlow(null)

        // Mock Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>(), any()) } returns 0

        mockGameBoardLogic = mockk(relaxed = true)
        mockLobbyLogic = mockk(relaxed = true)
        mockTradeLogic = mockk(relaxed = true)
        mockCheatingLogic = mockk(relaxed = true)
        mockPlayerSessionManager = mockk(relaxed = true)
        every { mockPlayerSessionManager.getPlayerId() } returns testPlayerId

        mockGameDataHandler = mockk()
        diceMutableStateFlow = MutableStateFlow(null)

        every { mockGameDataHandler.gameBoardState } returns gameBoardMutableStateFlow.asStateFlow()
        every { mockGameDataHandler.victoryPointsState } returns victoryPointsMutableStateFlow.asStateFlow()
        every { mockGameDataHandler.playersState } returns playersMutableStateFlow.asStateFlow()
        every { mockGameDataHandler.diceState } returns diceMutableStateFlow.asStateFlow()
        every { mockGameDataHandler.snackbarMessage } returns snackbarMessageFlow.asStateFlow()


        every { mockGameDataHandler.updateDiceState(any()) } answers {
            val state = firstArg<GameViewModel.DiceState?>()
            diceMutableStateFlow.value = state
        }

        // This mocking for updateGameBoard should set the gameBoardMutableStateFlow
        every { mockGameDataHandler.updateGameBoard(any<String>()) } answers {
            val jsonArg = it.invocation.args[0] as String
            // Simulate parsing and updating the flow based on jsonArg
            if (jsonArg == validBoardJson) {
                gameBoardMutableStateFlow.value = GameBoardModel(
                    tiles = listOf(Tile(1, TileType.CLAY, 5, listOf(0.0, 0.0))),
                    settlementPositions = listOf(SettlementPosition(1, null, listOf(0.0, 10.0))),
                    roads = listOf(Road(1, null, listOf(0.0, 5.0), 0.0, null)),
                    ports = emptyList(),
                    ringsOfBoard = 1,
                    sizeOfHex = 6
                )
            } else if (jsonArg == anotherValidBoardJson) {
                gameBoardMutableStateFlow.value = GameBoardModel(
                    tiles = listOf(Tile(2, TileType.WOOD, 6, listOf(1.0, 1.0))),
                    settlementPositions = listOf(SettlementPosition(2, null, listOf(1.0, 11.0))),
                    roads = listOf(Road(2, null, listOf(1.0, 6.0), 0.0, null)),
                    ports = emptyList(),
                    ringsOfBoard = 1,
                    sizeOfHex = 6
                )
            } else {
                gameBoardMutableStateFlow.value = null
            }
        }


        viewModel = GameViewModel(
            mockGameBoardLogic,
            mockLobbyLogic,
            mockGameDataHandler,
            mockPlayerSessionManager,
            mockTradeLogic,
            mockCheatingLogic

        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class) // Unmock Log after each test
        Dispatchers.resetMain()
    }


    @Test
    @DisplayName("Initial state should be null")
    fun initialStateIsNull() = runTest {
        assertNull(viewModel.gameBoardState.value, "Initial gameBoardState should be null")
    }

    @Nested
    @DisplayName("Dice Rolling")
    inner class DiceRollTests {

        @Test
        fun testDiceResultIsInitiallyNull() = runTest {
            assertNull(viewModel.diceResult.value, "Initial diceResult should be null")
        }

        @Test
        fun testRollDiceCallsGameBoardLogicWithCorrectLobbyId() = runTest {
            val testLobbyId = "test-lobby-abc"

            val testPlayer =
                PlayerInfo(id = testPlayerId, username = "TestPlayer", canRollDice = true)
            val playersMap = mapOf(testPlayerId to testPlayer)

            playersMutableStateFlow.value = playersMap

            advanceUntilIdle()

            every { mockGameBoardLogic.rollDice(testLobbyId) } just runs

            viewModel.rollDice(testLobbyId)
            advanceUntilIdle()

            verify(exactly = 1) { mockGameBoardLogic.rollDice(testLobbyId) }
            assertNull(viewModel.diceResult.value)
        }

        @Test
        fun rollDiceSetsIsProcessingRollFromFalseToTrueAndBack() = runTest {
            val testLobbyId = "test-lobby-processing"

            val testPlayer =
                PlayerInfo(id = testPlayerId, username = "TestPlayer", canRollDice = true)
            val playersMap = mapOf(testPlayerId to testPlayer)

            playersMutableStateFlow.value = playersMap

            advanceUntilIdle()

            every { mockGameBoardLogic.rollDice(any()) } just runs

            val isProcessingRollField =
                GameViewModel::class.java.getDeclaredField("isProcessingRoll")
            isProcessingRollField.isAccessible = true

            assertFalse(
                isProcessingRollField.get(viewModel) as Boolean,
                "Initially should be false"
            )

            viewModel.rollDice(testLobbyId)
            assertTrue(
                isProcessingRollField.get(viewModel) as Boolean,
                "Should be true during processing"
            )

            advanceUntilIdle()
            assertFalse(
                isProcessingRollField.get(viewModel) as Boolean,
                "Should be false after processing"
            )
        }


        @Test
        fun testRollDiceDoesNothingWhenAlreadyProcessing() = runTest {
            val testLobbyId = "test-lobby-456"
            val isProcessingRollField =
                GameViewModel::class.java.getDeclaredField("isProcessingRoll")
            isProcessingRollField.isAccessible = true
            isProcessingRollField.set(viewModel, true)

            viewModel.rollDice(testLobbyId)
            advanceUntilIdle()

            verify(exactly = 0) { mockGameBoardLogic.rollDice(any()) }
            assertNull(viewModel.diceResult.value)
        }

        @Test
        fun testUpdateDiceResultSetsValidPairToStateFlow() = runTest {
            val dice1 = 5
            val dice2 = 2
            val newDiceResult = Pair(dice1, dice2)

            viewModel.updateDiceResult(dice1, dice2)
            advanceUntilIdle()

            assertEquals(newDiceResult, viewModel.diceResult.value)
        }

        @Test
        fun testUpdateDiceResultSetsNullWhenDice1IsNull() = runTest {
            viewModel.updateDiceResult(null, 4)
            advanceUntilIdle()
            assertNull(viewModel.diceResult.value)
        }

        @Test
        fun testUpdateDiceResultSetsNullWhenDice2IsNull() = runTest {
            viewModel.updateDiceResult(3, null)
            advanceUntilIdle()
            assertNull(viewModel.diceResult.value)
        }

        @Test
        fun testUpdateDiceResultSetsNullWhenBothDiceAreNull() = runTest {
            viewModel.updateDiceResult(null, null)
            advanceUntilIdle()
            assertNull(viewModel.diceResult.value)
        }

        @Test
        fun testRollDiceShouldStartRollingWhenPlayerCanRoll() = runTest {
            val states = mutableListOf<GameViewModel.DiceState?>()
            every { mockGameDataHandler.updateDiceState(any()) } answers {
                val state = firstArg<GameViewModel.DiceState?>()
                states.add(state)
                diceMutableStateFlow.value = state
            }

            val testPlayer = PlayerInfo(
                id = testPlayerId,
                username = "TestPlayer",
                canRollDice = true,
                isActivePlayer = true
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)
            advanceUntilIdle()

            viewModel.rollDice("test-lobby")
            advanceUntilIdle()

            assertTrue(states.isNotEmpty())
            val rollingState = states[0]
            assertNotNull(rollingState)
            assertEquals(true, rollingState?.isRolling)
            assertEquals("TestPlayer", rollingState?.rollingPlayerUsername)
            assertEquals(false, rollingState?.showResult)

            verify(exactly = 1) { mockGameBoardLogic.rollDice("test-lobby") }
        }

        @Test
        fun testRollDiceShouldNotStartRollingWhenPlayerCannotRoll() = runTest {
            val testPlayer = PlayerInfo(
                id = testPlayerId,
                username = "TestPlayer",
                canRollDice = false
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)
            advanceUntilIdle()

            viewModel.rollDice("test-lobby")
            advanceUntilIdle()

            assertFalse(viewModel.isProcessingRoll)
            verify(exactly = 0) { mockGameBoardLogic.rollDice(any()) }
            assertNull(diceMutableStateFlow.value)
        }

        @Test
        fun testRollDiceShouldTimeoutAfterThreeSeconds() = runTest {
            val testPlayer = PlayerInfo(
                id = testPlayerId,
                username = "TestPlayer",
                canRollDice = true,
                isActivePlayer = true
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)
            advanceUntilIdle()

            every { mockGameBoardLogic.rollDice(any()) } just runs

            viewModel.rollDice("test-lobby")
            advanceUntilIdle()

            advanceTimeBy(3000)
            advanceUntilIdle()

            assertFalse(viewModel.isProcessingRoll)
            assertNull(diceMutableStateFlow.value)
            verify(exactly = 1) { Log.e("GameViewModel", "Dice roll timeout") }
        }

        @Test
        fun testShowResultShouldDisplayDiceResultAndResetAfterDelay() = runTest {
            val states = mutableListOf<GameViewModel.DiceState?>()
            every { mockGameDataHandler.updateDiceState(any()) } answers {
                val state = firstArg<GameViewModel.DiceState?>()
                states.add(state)
                diceMutableStateFlow.value = state
            }

            viewModel.showResult("TestPlayer", 4, 3)

            advanceUntilIdle()

            assertTrue(states.isNotEmpty())
            val resultState = states[0]
            assertNotNull(resultState)
            assertEquals(false, resultState?.isRolling)
            assertEquals(true, resultState?.showResult)
            assertEquals(4, resultState?.dice1)
            assertEquals(3, resultState?.dice2)
            assertEquals("TestPlayer", resultState?.rollingPlayerUsername)

            advanceTimeBy(3000)
            advanceUntilIdle()

            assertEquals(2, states.size)
            assertNull(states[1])
            assertNull(diceMutableStateFlow.value)
        }

        @Test
        fun testShowResultShouldUpdateDiceStateCorrectly() = runTest {
            val states = mutableListOf<GameViewModel.DiceState?>()
            every { mockGameDataHandler.updateDiceState(any()) } answers {
                val state = firstArg<GameViewModel.DiceState?>()
                states.add(state)
                diceMutableStateFlow.value = state
            }

            viewModel.showResult("TestPlayer", 4, 3)

            advanceUntilIdle()
            assertTrue(states.isNotEmpty())

            val resultState = states[0]
            assertNotNull(resultState)

            assertEquals("TestPlayer", resultState?.rollingPlayerUsername)
            assertEquals(4, resultState?.dice1)
            assertEquals(3, resultState?.dice2)
            assertEquals(true, resultState?.showResult)
            assertEquals(false, resultState?.isRolling)

            advanceTimeBy(3000)
            advanceUntilIdle()

            assertEquals(2, states.size)
            assertNull(states[1])
            assertNull(diceMutableStateFlow.value)
        }

        @Test
        fun testResetDiceStateShouldClearDiceState() = runTest {
            diceMutableStateFlow.value = GameViewModel.DiceState(
                rollingPlayerUsername = "TestPlayer",
                isRolling = true,
                dice1 = 1,
                dice2 = 2,
                showResult = false
            )

            viewModel.resetDiceState()
            advanceUntilIdle()
            assertNull(diceMutableStateFlow.value)
        }

        @Test
        fun testRollDiceShouldPreventConcurrentRolls() = runTest {
            val testPlayer = PlayerInfo(
                id = testPlayerId,
                username = "TestPlayer",
                canRollDice = true,
                isActivePlayer = true
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)
            advanceUntilIdle()

            every { mockGameBoardLogic.rollDice(any()) } just runs

            val field = GameViewModel::class.java.getDeclaredField("isProcessingRoll")
            field.isAccessible = true
            field.set(viewModel, true)

            viewModel.rollDice("test-lobby")
            advanceUntilIdle()

            verify(exactly = 0) { mockGameBoardLogic.rollDice(any()) }
            assertNull(diceMutableStateFlow.value)
        }

        @Test
        fun testRollDiceShouldNotProceedIfAlreadyProcessing() = runTest {
            viewModel.isProcessingRoll = true

            viewModel.rollDice("lobby123")

            verify(exactly = 0) { mockGameBoardLogic.rollDice(any()) }
        }

        @Test
        fun testRollDiceShouldNotProceedIfPlayerCannotRoll() = runTest {
            val player = PlayerInfo(
                id = testPlayerId,
                username = "TestPlayer",
                canRollDice = false
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to player)

            viewModel.rollDice("lobby123")

            verify(exactly = 0) { mockGameBoardLogic.rollDice(any()) }
        }

        @Test
        fun testRollDiceShouldResetStateAfterTimeoutIfNoResultReceived() = runTest {
            val player = PlayerInfo(
                id = testPlayerId,
                username = "TestPlayer",
                canRollDice = true
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to player)

            viewModel.rollDice("lobby123")
            advanceUntilIdle()

            assertNull(diceMutableStateFlow.value)
            assertFalse(viewModel.isProcessingRoll)
        }

        @Test
        fun testShowResultShouldUpdatesDiceStateAndResetsAfterDelay() =
            runTest(dispatcher.scheduler) {
                viewModel.showResult("Tester", 3, 4)

                viewModel.diceState.test {
                    skipItems(1)
                    val state = awaitItem()
                    assert(state != null && state.showResult)
                    assert(state?.dice1 == 3 && state.dice2 == 4)
                    cancelAndIgnoreRemainingEvents()
                }
            }

        @Test
        fun testResetDiceStateShouldClearsStateAndHidesPopup() = runTest {
            viewModel.resetDiceState()
            advanceUntilIdle()

            viewModel.diceState.test {
                val state = awaitItem()
                assert(state == null)
            }

            viewModel.showDicePopup.test {
                assert(!awaitItem())
            }
        }
    }

    @Test
    fun onCheatAttemptDelegatesToCheatingLogic() = runTest {
        val mockCheatingLogic = mockk<CheatingLogic>(relaxed = true)
        viewModel = GameViewModel(
            mockGameBoardLogic,
            mockLobbyLogic,
            mockGameDataHandler,
            mockPlayerSessionManager,
            mockTradeLogic,
            mockCheatingLogic,
        )
        val lobbyId = "lobby123"
        val tileType = TileType.ORE

        viewModel.onCheatAttempt(tileType, lobbyId)
        verify { mockCheatingLogic.sendCheatAttempt(tileType, lobbyId) }
    }

    @Test
    fun onReportPlayerDelegatesToCheatingLogic() = runTest {
        val mockCheatingLogic = mockk<CheatingLogic>(relaxed = true)
        val viewModel = GameViewModel(
            mockGameBoardLogic,
            mockLobbyLogic,
            mockGameDataHandler,
            mockPlayerSessionManager,
            mockTradeLogic,
            mockCheatingLogic,
        )

        val reportedId = "reported456"
        val lobbyId = "lobbyXYZ"

        viewModel.onReportPlayer(reportedId, lobbyId)

        verify(exactly = 1) {
            mockCheatingLogic.sendReportPlayer(reportedId, lobbyId)
        }
    }

    @Nested
    @DisplayName("Highlighting Logic")

    inner class HighlightingTests {
        private fun getHighlightedSettlementIds(): Set<Int> {
            val field = GameViewModel::class.java.getDeclaredField("_highlightedSettlementIds")
            field.isAccessible = true
            return (field.get(viewModel) as MutableStateFlow<Set<Int>>).value
        }

        private fun getHighlightedRoadIds(): Set<Int> {
            val field = GameViewModel::class.java.getDeclaredField("_highlightedRoadIds")
            field.isAccessible = true
            return (field.get(viewModel) as MutableStateFlow<Set<Int>>).value
        }

        @Test
        fun testClearHighlightsClearsBothSettlementAndRoadHighlights() = runTest {
            val settlementField = GameViewModel::class.java.getDeclaredField("_highlightedSettlementIds")
            settlementField.isAccessible = true
            (settlementField.get(viewModel) as MutableStateFlow<Set<Int>>).value = setOf(1, 2, 3)

            val roadField = GameViewModel::class.java.getDeclaredField("_highlightedRoadIds")
            roadField.isAccessible = true
            (roadField.get(viewModel) as MutableStateFlow<Set<Int>>).value = setOf(4, 5, 6)

            viewModel.clearHighlights()
            advanceUntilIdle()

            assertTrue(getHighlightedSettlementIds().isEmpty())
            assertTrue(getHighlightedRoadIds().isEmpty())
        }

        @Test
        fun testUpdateHighlightedPositionsDoesNothingWhenBoardStateIsNull() = runTest {
            gameBoardMutableStateFlow.value = null
            viewModel.updateHighlightedPositions()
            advanceUntilIdle()

            assertTrue(viewModel.highlightedSettlementIds.value.isEmpty())
            assertTrue(viewModel.highlightedRoadIds.value.isEmpty())
        }

        @Test
        fun testUpdateHighlightedPositionsDoesNothingWhenPlayerInfoIsNull() = runTest {
            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = emptyList(),
                roads = emptyList(),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )
            playersMutableStateFlow.value = emptyMap()

            viewModel.updateHighlightedPositions()
            advanceUntilIdle()

            assertTrue(viewModel.highlightedSettlementIds.value.isEmpty())
            assertTrue(viewModel.highlightedRoadIds.value.isEmpty())
        }

        @Test
        fun testUpdateHighlightedPositionsDoesNothingWhenPlayerIsNotActive() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = false)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            viewModel.updateHighlightedPositions()
            advanceUntilIdle()

            assertTrue(viewModel.highlightedSettlementIds.value.isEmpty())
            assertTrue(viewModel.highlightedRoadIds.value.isEmpty())
        }

        @Test
        fun testUpdateHighlightedPositionsHighlightsAllRoadsWhenNoPlayerRoads() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true, isSetupRound = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val road1 = Road(1, null, listOf(0.0, 0.0), 0.0, null)
            val road2 = Road(2, null, listOf(1.0, 1.0), 0.0, null)

            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = emptyList(),
                roads = listOf(road1, road2),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )

            dispatcher.scheduler.advanceUntilIdle()

            viewModel.updateHighlightedPositions()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(setOf(1, 2), viewModel.highlightedRoadIds.value)
            assertTrue(viewModel.highlightedSettlementIds.value.isEmpty())
        }

        @Test
        fun testUpdateHighlightedPositionsHighlightsSettlementsWhenNoPlayerSettlements() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true, isSetupRound = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val road = Road(1, testPlayerId, listOf(0.0, 0.0), 0.0, null)
            val settlement = SettlementPosition(1, null, listOf(0.0, 5.0))

            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = listOf(settlement),
                roads = listOf(road),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )

            dispatcher.scheduler.advanceUntilIdle()

            viewModel.handleRoadClick(road, "testLobby")
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(setOf(1), viewModel.highlightedSettlementIds.value)
            assertTrue(viewModel.highlightedRoadIds.value.isEmpty())
        }

        @Test
        fun testUpdateHighlightedPositionsClearsHighlightsWhenPlayerHasRoadsAndSettlements() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = false)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val road = Road(1, testPlayerId, listOf(0.0, 0.0), 0.0, null)
            val settlement = SettlementPosition(1, Building(testPlayerId, "Settlement", "#FF0000"), listOf(0.0, 5.0))

            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = listOf(settlement),
                roads = listOf(road),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )

            viewModel.updateHighlightedPositions()

            assertTrue(viewModel.highlightedSettlementIds.value.isEmpty())
            assertTrue(viewModel.highlightedRoadIds.value.isEmpty())
        }

        @Test
        fun testUpdateHighlightedPositionsHighlightsViablePositionsInNormalRound() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true, isSetupRound = false)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val road1 = Road(1, null, listOf(0.0, 0.0), 0.0, null)
            val road2 = Road(2, testPlayerId, listOf(1.0, 1.0), 0.0, null)
            val settlement = SettlementPosition(1, null, listOf(0.0, 5.0))

            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = listOf(settlement),
                roads = listOf(road1, road2),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )

            dispatcher.scheduler.advanceUntilIdle()

            viewModel.updateHighlightedPositions()
            dispatcher.scheduler.advanceUntilIdle()

            assertNotNull(viewModel.highlightedSettlementIds.value)
            assertNotNull(viewModel.highlightedRoadIds.value)
        }

        @Test
        fun testUpdateHighlightedPositionsReturnsEarlyWhenBoardIsNull() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true, isSetupRound = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            dispatcher.scheduler.advanceUntilIdle()

            val initialSettlements = viewModel.highlightedSettlementIds.value
            val initialRoads = viewModel.highlightedRoadIds.value

            viewModel.updateHighlightedPositions()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(initialSettlements, viewModel.highlightedSettlementIds.value)
            assertEquals(initialRoads, viewModel.highlightedRoadIds.value)
        }

        @Test
        fun testUpdateHighlightedPositionsReturnsEarlyWhenPlayerIsNull() = runTest {
            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = emptyList(),
                roads = emptyList(),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )

            dispatcher.scheduler.advanceUntilIdle()

            val initialSettlements = viewModel.highlightedSettlementIds.value
            val initialRoads = viewModel.highlightedRoadIds.value

            viewModel.updateHighlightedPositions()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(initialSettlements, viewModel.highlightedSettlementIds.value)
            assertEquals(initialRoads, viewModel.highlightedRoadIds.value)
        }

        @Test
        fun testUpdateHighlightedPositionsReturnsEarlyWhenPlayerIsNotActive() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = false, isSetupRound = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = emptyList(),
                roads = emptyList(),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )

            dispatcher.scheduler.advanceUntilIdle()

            val initialSettlements = viewModel.highlightedSettlementIds.value
            val initialRoads = viewModel.highlightedRoadIds.value

            viewModel.updateHighlightedPositions()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(initialSettlements, viewModel.highlightedSettlementIds.value)
            assertEquals(initialRoads, viewModel.highlightedRoadIds.value)
        }

        @Test
        fun testClearsHighlightsAfterSettlementPlacement() = runTest {
            val testPlayer = PlayerInfo(
                id = testPlayerId,
                username = "Test",
                isActivePlayer = true,
                isSetupRound = true
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val playerRoad = Road(1, testPlayerId, listOf(0.0, 0.0), 0.0, null)
            val settlement = SettlementPosition(1, null, listOf(0.0, 5.0))

            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = listOf(settlement),
                roads = listOf(playerRoad),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )

            viewModel.handleRoadClick(playerRoad, "test-lobby")
            viewModel.handleSettlementClick(settlement, false, "test-lobby")
            advanceUntilIdle()

            viewModel.updateHighlightedPositions()
            advanceUntilIdle()

            assertTrue(viewModel.highlightedSettlementIds.value.isEmpty())
            assertTrue(viewModel.highlightedRoadIds.value.isEmpty())
        }

        private fun GameViewModel.setSetupRoundState(hasPlacedRoad: Boolean, hasPlacedSettlement: Boolean) {
            val roadField = GameViewModel::class.java.getDeclaredField("hasPlacedSetupRoad")
            val settlementField = GameViewModel::class.java.getDeclaredField("hasPlacedSetupSettlement")
            roadField.isAccessible = true
            settlementField.isAccessible = true
            roadField.setBoolean(this, hasPlacedRoad)
            settlementField.setBoolean(this, hasPlacedSettlement)
        }

        @Test
        fun testCombineCollectorUpdatesHighlightsWhenBuildMenuOpens() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            viewModel.setBuildMenuOpen(true)
            advanceUntilIdle()

            verify { Log.d("GameViewModel", any()) }
        }

        @Test
        fun testCombineCollectorClearsHighlightsWhenBuildMenuCloses() = runTest {
            val settlementField = GameViewModel::class.java.getDeclaredField("_highlightedSettlementIds")
            settlementField.isAccessible = true
            (settlementField.get(viewModel) as MutableStateFlow<Set<Int>>).value = setOf(1, 2, 3)

            val roadField = GameViewModel::class.java.getDeclaredField("_highlightedRoadIds")
            roadField.isAccessible = true
            (roadField.get(viewModel) as MutableStateFlow<Set<Int>>).value = setOf(4, 5, 6)

            viewModel.setBuildMenuOpen(false)
            advanceUntilIdle()

            assertTrue(getHighlightedSettlementIds().isEmpty())
            assertTrue(getHighlightedRoadIds().isEmpty())
        }

        @Test
        fun testUpdateHighlightedPositionsHighlightsCorrectValues() = runTest {
            val testSettlement = SettlementPosition(
                id = 1,
                building = null,
                coordinates = listOf(0.0, 10.0)
            )
            val testRoad = Road(
                id = 1,
                owner = null,
                coordinates = listOf(0.0, 5.0),
                rotationAngle = 0.0,
                color = null
            )
            val playerRoad = testRoad.copy(owner = testPlayerId)

            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = listOf(testSettlement),
                roads = listOf(playerRoad),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to PlayerInfo(id = testPlayerId, username = "Tester", isActivePlayer = true))

            viewModel.updatePlayerResources(
                mapOf(
                    TileType.WOOD to 1,
                    TileType.CLAY to 1,
                    TileType.WHEAT to 1,
                    TileType.SHEEP to 1
                )
            )

            viewModel.setBuildMenuOpen(true)
            advanceUntilIdle()

            assertEquals(setOf(1), viewModel.highlightedSettlementIds.value)
            assertEquals(emptySet<Int>(), viewModel.highlightedRoadIds.value)
        }

        @Test
        fun testIsValidSettlementLocationReturnsTrueForCityUpgrade() = runTest {
            val settlement = SettlementPosition(
                id = 1,
                building = Building(
                    owner = testPlayerId,
                    color = "#FF0000",
                    type = "Settlement"
                ),
                coordinates = listOf(0.0, 10.0)
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to PlayerInfo(id = testPlayerId, username = "Tester", isActivePlayer = true))
            viewModel.updatePlayerResources(mapOf(TileType.WHEAT to 2, TileType.ORE to 3))

            val result = viewModel.isValidSettlementLocation(
                settlement,
                listOf(settlement),
                roads = emptyList()
            )
            assertTrue(result)
        }

        @Test
        fun testIsValidSettlementLocationReturnsFalseForAdjacentBuildings() = runTest {
            val settlement = SettlementPosition(
                id = 1,
                building = null,
                coordinates = listOf(0.0, 10.0)
            )
            val adjacentSettlement = SettlementPosition(
                id = 2,
                building = Building(owner = "enemy", color = "#000", type = "Settlement"),
                coordinates = listOf(0.5, 10.5)
            )

            val playerRoad = Road(1, testPlayerId, listOf(0.0, 5.0), 0.0, null)
            playersMutableStateFlow.value = mapOf(testPlayerId to PlayerInfo(id = testPlayerId, username = "Tester", isActivePlayer = true))
            viewModel.updatePlayerResources(
                mapOf(TileType.WOOD to 1, TileType.CLAY to 1, TileType.WHEAT to 1, TileType.SHEEP to 1)
            )

            val result = viewModel.isValidSettlementLocation(
                settlement,
                listOf(settlement, adjacentSettlement),
                listOf(playerRoad)
            )
            assertFalse(result)
        }

        @Test
        fun testIsValidSettlementLocationReturnsFalseWhenNotOwner() = runTest {
            val testPlayer = PlayerInfo(
                id = testPlayerId,
                username = "Test",
                resources = mapOf(TileType.WHEAT to 2, TileType.ORE to 3)
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val settlement = SettlementPosition(
                1,
                Building("otherPlayer", "Settlement", "#0000FF"),
                listOf(0.0, 0.0)
            )

            val result = viewModel.isValidSettlementLocation(
                settlement,
                emptyList(),
                emptyList()
            )

            assertFalse(result)
        }

        @Test
        fun testIsValidSettlementLocationReturnsFalseWhenNotSettlementType() = runTest {
            val testPlayer = PlayerInfo(
                id = testPlayerId,
                username = "Test",
                resources = mapOf(TileType.WHEAT to 2, TileType.ORE to 3)
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val settlement = SettlementPosition(
                1,
                Building(testPlayerId, "City", "#FF0000"),
                listOf(0.0, 0.0)
            )

            val result = viewModel.isValidSettlementLocation(
                settlement,
                emptyList(),
                emptyList()
            )

            assertFalse(result)
        }

        @Test
        fun testIsValidSettlementLocationReturnsFalseWhenCannotBuildCity() = runTest {
            val testPlayer = PlayerInfo(
                id = testPlayerId,
                username = "Test",
                resources = mapOf(TileType.WHEAT to 1, TileType.ORE to 2)
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val settlement = SettlementPosition(
                1,
                Building(testPlayerId, "Settlement", "#FF0000"),
                listOf(0.0, 0.0)
            )

            val result = viewModel.isValidSettlementLocation(
                settlement,
                emptyList(),
                emptyList()
            )

            assertFalse(result)
        }

        @Test
        fun testIsValidRoadLocationReturnsTrueWhenConnectedToOwnedSettlement() = runTest {
            val road = Road(1, null, listOf(0.0, 5.0), 0.0, null)
            val settlement = SettlementPosition(
                id = 1,
                building = Building(owner = testPlayerId, color = "#FF0000", type = "Settlement"),
                coordinates = listOf(0.0, 5.5)
            )
            viewModel.updatePlayerResources(mapOf(TileType.WOOD to 1, TileType.CLAY to 1))

            val result = viewModel.isValidRoadLocation(road, listOf(settlement), listOf(road))
            assertTrue(result)
        }

        @Test
        fun testIsValidRoadLocationReturnsFalseWhenOwned() = runTest {
            val road = Road(1, owner = testPlayerId, coordinates = listOf(0.0, 5.0), rotationAngle = 0.0, color = null)
            viewModel.updatePlayerResources(mapOf(TileType.WOOD to 1, TileType.CLAY to 1))

            val result = viewModel.isValidRoadLocation(road, emptyList(), listOf(road))
            assertFalse(result)
        }

        @Test
        fun testIsValidRoadLocationReturnsFalseWhenCannotBuildRoad() = runTest {
            val testPlayer = PlayerInfo(
                id = testPlayerId,
                username = "Test",
                resources = emptyMap()
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val road = Road(1, null, listOf(0.0, 0.0), 0.0, null)

            val result = viewModel.isValidRoadLocation(
                road,
                emptyList(),
                emptyList()
            )

            assertFalse(result)
        }
    }

    @Nested
    @DisplayName("Building Validation")
    inner class BuildingValidationTests {

        @Test
        fun testCanBuildSettlementReturnsTrueWhenResourcesAvailable() = runTest {
            val resources = mapOf(
                TileType.WOOD to 1,
                TileType.CLAY to 1,
                TileType.WHEAT to 1,
                TileType.SHEEP to 1
            )
            viewModel.updatePlayerResources(resources)

            assertTrue(viewModel.canBuildSettlement())
        }

        @Test
        fun testCanBuildSettlementReturnsFalseWhenResourcesMissing() = runTest {
            val resources = mapOf(
                TileType.WOOD to 1,
                TileType.CLAY to 1,
                TileType.WHEAT to 1
            )
            viewModel.updatePlayerResources(resources)

            assertFalse(viewModel.canBuildSettlement())
        }

        @Test
        fun testCanBuildCityReturnsTrueWhenResourcesAvailable() = runTest {
            val resources = mapOf(
                TileType.WHEAT to 2,
                TileType.ORE to 3
            )
            viewModel.updatePlayerResources(resources)

            assertTrue(viewModel.canBuildCity())
        }

        @Test
        fun testCanBuildCityReturnsFalseWhenResourcesMissing() = runTest {
            val resources = mapOf(
                TileType.WHEAT to 2,
                TileType.ORE to 2
            )
            viewModel.updatePlayerResources(resources)

            assertFalse(viewModel.canBuildCity())
        }

        @Test
        fun testCanBuildRoadReturnsTrueWhenResourcesAvailable() = runTest {
            val resources = mapOf(
                TileType.WOOD to 1,
                TileType.CLAY to 1
            )
            viewModel.updatePlayerResources(resources)

            assertTrue(viewModel.canBuildRoad())
        }

        @Test
        fun testCanBuildRoadReturnsFalseWhenResourcesMissing() = runTest {
            val resources = mapOf(
                TileType.WOOD to 1
            )
            viewModel.updatePlayerResources(resources)

            assertFalse(viewModel.canBuildRoad())
        }

        @Test
        fun testCanBuildSettlementReturnsFalseWhenMissingResources() = runTest {
            val testCases = listOf(
                mapOf(TileType.CLAY to 1, TileType.WHEAT to 1, TileType.SHEEP to 1),
                mapOf(TileType.WOOD to 1, TileType.WHEAT to 1, TileType.SHEEP to 1),
                mapOf(TileType.WOOD to 1, TileType.CLAY to 1, TileType.SHEEP to 1),
                mapOf(TileType.WOOD to 1, TileType.CLAY to 1, TileType.WHEAT to 1)
            )

            testCases.forEach { resources ->
                viewModel.updatePlayerResources(resources)
                assertFalse(viewModel.canBuildSettlement())
            }
        }

        @Test
        fun testCanBuildCityReturnsFalseWhenMissingResources() = runTest {
            val testCases = listOf(
                mapOf(TileType.WHEAT to 1, TileType.ORE to 3),
                mapOf(TileType.WHEAT to 2, TileType.ORE to 2)
            )

            testCases.forEach { resources ->
                viewModel.updatePlayerResources(resources)
                assertFalse(viewModel.canBuildCity())
            }
        }

        @Test
        fun testCanBuildRoadReturnsFalseWhenMissingResources() = runTest {
            val testCases = listOf(
                mapOf(TileType.WOOD to 1),
                mapOf(TileType.CLAY to 1)
            )

            testCases.forEach { resources ->
                viewModel.updatePlayerResources(resources)
                assertFalse(viewModel.canBuildRoad())
            }
        }
    }

    @Nested
    @DisplayName("Position Validation")
    inner class PositionValidationTests {

        @Test
        fun testIsConnectedReturnsTrueForClosePoints() = runTest {
            val point1 = listOf(0.0, 0.0)
            val point2 = listOf(5.0, 5.0)

            assertTrue(viewModel.isConnected(point1, point2))
        }

        @Test
        fun testIsConnectedReturnsFalseForDistantPoints() = runTest {
            val point1 = listOf(0.0, 0.0)
            val point2 = listOf(100.0, 100.0)


            assertFalse(viewModel.isConnected(point1, point2))
        }

        @Test
        fun testIsConnectedReturnsFalseForInvalidPoints() = runTest {
            val point1 = listOf(0.0)
            val point2 = listOf(5.0, 5.0)

            val result = viewModel.isConnected(point1, point2)

            assertFalse(result)
        }

        @Test
        fun testIsAdjacentReturnsTrueForClosePoints() = runTest {
            val point1 = listOf(0.0, 0.0)
            val point2 = listOf(10.0, 10.0)

            assertTrue(viewModel.isAdjacent(point1, point2))
        }

        @Test
        fun testIsAdjacentReturnsFalseForInvalidPoints() = runTest {
            val point1 = listOf(0.0) 
            val point2 = listOf(10.0, 10.0)

            val result = viewModel.isAdjacent(point1, point2)

            assertFalse(result)
        }

        @Test
        fun testIsAdjacentReturnsFalseForDistantPoints() = runTest {
            val point1 = listOf(0.0, 0.0)
            val point2 = listOf(100.0, 100.0)

            assertFalse(viewModel.isAdjacent(point1, point2))
        }

        @Test
        fun testIsValidRoadLocationReturnsTrueWhenConnectedToPlayerOwnedRoad() = runTest {
            val targetRoad = Road(
                id = 99,
                owner = null,
                coordinates = listOf(5.0, 5.0),
                rotationAngle = 0.0,
                color = null
            )

            val existingRoad = Road(
                id = 1,
                owner = testPlayerId,
                coordinates = listOf(5.5, 5.5),
                rotationAngle = 0.0,
                color = null
            )

            viewModel.updatePlayerResources(
                mapOf(TileType.WOOD to 1, TileType.CLAY to 1)
            )

            val result = viewModel.isValidRoadLocation(
                road = targetRoad,
                settlements = emptyList(),
                roads = listOf(existingRoad)
            )

            assertTrue(result, "Should return true if the road is connected to an owned road")
        }
    }
    @Test
    fun snackbarMessageReflectsGameDataHandlerState() = runTest {
        val testSnackbar = Pair("Test message", "info")
        snackbarMessageFlow.value = testSnackbar

        assertEquals(testSnackbar, viewModel.snackbarMessage.value)
    }

    @Test
    fun clearSnackbarMessageCallsGameDataHandler() = runTest {
        coEvery { mockGameDataHandler.clearSnackbar() } just runs

        viewModel.clearSnackbarMessage()
        advanceUntilIdle()

        coVerify(exactly = 1) { mockGameDataHandler.clearSnackbar() }
    }



    @Nested
    @DisplayName("Initialization via initializeBoardState")
    inner class InitializationTests {

        @Test
        fun loadsBoardWhenValidJSONProvided() = runTest {
            // ViewModel's gameBoardState should initially be null
            assertEquals(null, viewModel.gameBoardState.value)

            viewModel.initializeBoardState(validBoardJson)
            advanceUntilIdle() // Allow loadGameBoardFromJson coroutine to run

            verify(exactly = 1) { mockGameDataHandler.updateGameBoard(validBoardJson) }

            val loadedState = viewModel.gameBoardState.value
            assertNotNull(loadedState, "GameBoardState should be loaded")
            assertEquals(1, loadedState?.tiles?.size, "Loaded state should have 1 tile")
            assertEquals(TileType.CLAY, loadedState?.tiles?.firstOrNull()?.type)
            assertEquals(0, loadedState?.ports?.size)
        }


        @Test
        fun setsNullStateWhenJSONIsInvalid() = runTest {
            assertEquals(null, viewModel.gameBoardState.value)

            viewModel.initializeBoardState(invalidBoardJson)
            advanceUntilIdle()

            verify(exactly = 1) { mockGameDataHandler.updateGameBoard(invalidBoardJson) }
            assertNull(viewModel.gameBoardState.value, "GameBoardState should be null after invalid JSON")
        }

        @Test
        fun doesNotCallLoadWhenJSONisNull() = runTest {
            assertEquals(null, viewModel.gameBoardState.value)

            viewModel.initializeBoardState(null)
            advanceUntilIdle()

            verify(exactly = 0) { mockGameDataHandler.updateGameBoard(any<String>()) }
            assertNull(viewModel.gameBoardState.value)
        }

        @Test
        fun doesNotReloadIfStateAlreadyExists() = runTest {
            viewModel.initializeBoardState(validBoardJson) // First load
            advanceUntilIdle()
            verify(exactly = 1) { mockGameDataHandler.updateGameBoard(validBoardJson) }
            val firstState = viewModel.gameBoardState.value
            assertNotNull(firstState)

            viewModel.initializeBoardState(anotherValidBoardJson) // Attempt second load
            advanceUntilIdle()

            // Should still have only called updateGameBoard once (for the first valid JSON)
            verify(exactly = 1) { mockGameDataHandler.updateGameBoard(any<String>()) }
            assertEquals(firstState, viewModel.gameBoardState.value, "State should not have changed")
        }
    }

    @Nested
    @DisplayName("loadGameBoardFromJson method")
    inner class LoadFromJsonTests {

        @Test
        @DisplayName("calls updateGameBoard and updates state when called with valid JSON")
        fun callsUpdateGameBoardAndUpdatesStateOnLoadFromJsonValidJson() = runTest {
             assertEquals(null, viewModel.gameBoardState.value) // Initial state

             viewModel.loadGameBoardFromJson(validBoardJson)
             advanceUntilIdle()

             verify(exactly = 1) { mockGameDataHandler.updateGameBoard(validBoardJson) }

             val loadedState = viewModel.gameBoardState.value
             assertNotNull(loadedState)
             assertEquals(1, loadedState?.tiles?.size)
             assertEquals(TileType.CLAY, loadedState?.tiles?.first()?.type)
             assertEquals(0, loadedState?.ports?.size)
        }

        @Test
        @DisplayName("calls updateGameBoard and state reflects null when called with invalid JSON")
        fun callsUpdateGameBoardAndReflectsNullStateOnLoadFromJsonInvalidJson() = runTest {
            assertEquals(null, viewModel.gameBoardState.value)

            viewModel.loadGameBoardFromJson(invalidBoardJson)
            advanceUntilIdle()

            verify(exactly = 1) { mockGameDataHandler.updateGameBoard(invalidBoardJson) }

            assertNull(viewModel.gameBoardState.value, "GameBoardState should be null")
        }
    }

    @Nested
    @DisplayName("Click Handlers")
    inner class ClickHandlerTests {

        @Test
        fun handleTileClickLogsMessage() = runTest {
            val testTile = Tile(id = 1, type = TileType.WOOD, value = 0, coordinates = listOf(0.0, 0.0))
            val testLobbyId = "tile-lobby-1"

            viewModel.handleTileClick(testTile, testLobbyId)
            advanceUntilIdle()
            verify { Log.d("GameViewModel", "handleTileClick: Tile ID=${testTile.id}") }
        }

        @Test
        fun handleSettlementClickLogsMessage() = runTest {
            val settlement = SettlementPosition(1, null, listOf(0.0, 0.0))
            viewModel.handleSettlementClick(settlement, false, "lobby1")
            verify { Log.d("GameViewModel", "handleSettlementClick: SettlementPosition ID=1") }
        }

        @Test
        fun handleSettlementClickCallsUpgradeWhenIsUpgradeTrue() = runTest {
            val playerInfo = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to playerInfo)

            dispatcher.scheduler.advanceUntilIdle()

            val settlement = SettlementPosition(1, null, listOf(0.0, 0.0))

            viewModel.handleSettlementClick(settlement, true, "lobby1")
            dispatcher.scheduler.advanceUntilIdle()

            verify { mockGameBoardLogic.upgradeSettlement(1, "lobby1") }
        }

        @Test
        fun handleSettlementClickCallsPlaceWhenIsUpgradeFalse() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            dispatcher.scheduler.advanceUntilIdle()

            val settlement = SettlementPosition(1, null, listOf(0.0, 0.0))

            viewModel.handleSettlementClick(settlement, false, "lobby1")

            dispatcher.scheduler.advanceUntilIdle()
            verify(exactly = 1) { mockGameBoardLogic.placeSettlement(settlement.id, "lobby1") }
        }

        @Test
        fun handleSettlementClickCallsGameBoardLogicPlaceSettlement() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            dispatcher.scheduler.advanceUntilIdle()

            val testPosition = SettlementPosition(id = 5, building = null, coordinates = listOf(1.0, 2.0))
            val testLobbyId = "click-lobby-1"

            viewModel.handleSettlementClick(testPosition, false, testLobbyId)
            dispatcher.scheduler.advanceUntilIdle()

            verify(exactly = 1) { mockGameBoardLogic.placeSettlement(testPosition.id, testLobbyId) }
        }

        @Test
        fun handleSettlementClickCallsGameBoardLogicUpgradeSettlement() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            dispatcher.scheduler.advanceUntilIdle()

            val testPosition = SettlementPosition(id = 5, building = null, coordinates = listOf(1.0, 2.0))
            val testLobbyId = "click-lobby-1"

            viewModel.handleSettlementClick(testPosition, true, testLobbyId)
            dispatcher.scheduler.advanceUntilIdle()
            verify(exactly = 1) { mockGameBoardLogic.upgradeSettlement(testPosition.id, testLobbyId) }
        }

        @Test
        fun handleSettlementClickClearsHighlightsInSetupRound() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val settlement = SettlementPosition(1, null, listOf(0.0, 0.0))
            viewModel.handleSettlementClick(settlement, false, "lobby1")

            assertTrue(viewModel.highlightedSettlementIds.value.isEmpty())
            assertTrue(viewModel.highlightedRoadIds.value.isEmpty())
        }

        @Test
        fun handleSettlementClickRemovesHighlightInNormalRound() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val settlement = SettlementPosition(1, null, listOf(0.0, 0.0))
            viewModel.handleSettlementClick(settlement, false, "lobby1")

            assertFalse(viewModel.highlightedSettlementIds.value.contains(1))
        }

        @Test
        fun handleRoadClickCallsPlaceRoad() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            dispatcher.scheduler.advanceUntilIdle()

            val road = Road(1, null, listOf(0.0, 0.0), 0.0, null)

            viewModel.handleRoadClick(road, "lobby1")
            dispatcher.scheduler.advanceUntilIdle()

            verify { mockGameBoardLogic.placeRoad(1, "lobby1") }
        }

        @Test
        fun handleRoadClickClearsHighlightsAndShowsAdjacentSettlementsInSetupRound() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true, isSetupRound = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val road = Road(1, null, listOf(0.0, 0.0), 0.0, null)
            val settlement = SettlementPosition(1, null, listOf(0.0, 5.0))

            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = listOf(settlement),
                roads = listOf(road),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )

            dispatcher.scheduler.advanceUntilIdle()

            viewModel.handleRoadClick(road, "lobby1")
            dispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.highlightedRoadIds.value.isEmpty())
            assertEquals(setOf(1), viewModel.highlightedSettlementIds.value)
        }

        @Test
        fun handleRoadClickClearsHighlightsAndHighlightsAdjacentSettlementsInSetupRound() = runTest {
            val testPlayer = PlayerInfo(id = testPlayerId, username = "Test", isActivePlayer = true, isSetupRound = true)
            playersMutableStateFlow.value = mapOf(testPlayerId to testPlayer)

            val road = Road(1, null, listOf(0.0, 0.0), 0.0, null)
            val settlement = SettlementPosition(2, null, listOf(0.0, 5.0))
            gameBoardMutableStateFlow.value = GameBoardModel(
                tiles = emptyList(),
                settlementPositions = listOf(settlement),
                roads = listOf(road),
                ports = emptyList(),
                ringsOfBoard = 1,
                sizeOfHex = 6
            )

            dispatcher.scheduler.advanceUntilIdle()

            viewModel.handleRoadClick(road, "lobby")
            dispatcher.scheduler.advanceUntilIdle()

            assertTrue(viewModel.highlightedRoadIds.value.isEmpty())
            assertEquals(setOf(2), viewModel.highlightedSettlementIds.value)
        }

        @Test
        fun handleRoadClickRemovesHighlightInNormalRound() = runTest {
            val playerInfo = PlayerInfo(
                username = "Test Player",
                isSetupRound = false
            )
            playersMutableStateFlow.value = mapOf(testPlayerId to playerInfo)

            val road = Road(1, null, listOf(0.0, 0.0), 0.0, null)
            viewModel.handleRoadClick(road, "lobby1")

            assertFalse(viewModel.highlightedRoadIds.value.contains(1))
        }
    }

    @Nested
    @DisplayName("Build Menu State")
    inner class BuildMenuStateTests {

        @Test
        fun isBuildMenuOpenDefaultsToFalse() = runTest {
            assertEquals(false, viewModel.isBuildMenuOpen.value)
        }

        @Test
        fun setBuildMenuOpenSetsStateToTrue() = runTest {
            viewModel.setBuildMenuOpen(true)
            advanceUntilIdle()
            assertEquals(true, viewModel.isBuildMenuOpen.value)
        }

        @Test
        fun setBuildMenuOpenSetsStateToFalse() = runTest {
            viewModel.setBuildMenuOpen(true)
            advanceUntilIdle()
            assertEquals(true, viewModel.isBuildMenuOpen.value)

            viewModel.setBuildMenuOpen(false)
            advanceUntilIdle()
            assertEquals(false, viewModel.isBuildMenuOpen.value)
        }
    }

    @Test
    fun playerIdReturnsPlayerIdFromSessionManager() = runTest {
        every { mockPlayerSessionManager.getPlayerId() } returns testPlayerId
        val retrievedPlayerId = viewModel.playerId
        assertEquals(testPlayerId, retrievedPlayerId)
        verify(atLeast = 1) { mockPlayerSessionManager.getPlayerId() } // atLeast = 1 due to init block
    }

    @Nested
    @DisplayName("Player Resources State")
    inner class PlayerResourcesTests {

        @Test
        fun playerResourcesInitializesToZeroForAllTypesWhenPlayerHasNoResources() = runTest {
            playersMutableStateFlow.value = mapOf(testPlayerId to PlayerInfo(id = testPlayerId, username = "Test", resources = emptyMap()))

            val expectedInitialResources = TileType.entries
                .filter { it != TileType.WASTE }
                .associateWith { 0 }
            assertEquals(expectedInitialResources, viewModel.playerResources.value)
        }

         @Test
         fun playerResourcesInitializesFromPlayerDataHandler() = runTest {
             val initialPlayerResources = mapOf(TileType.WOOD to 2, TileType.CLAY to 1)
             playersMutableStateFlow.value = mapOf(testPlayerId to PlayerInfo(id = testPlayerId, username = "Test", resources = initialPlayerResources))

             // Recreate ViewModel to pick up the new playersState from GameDataHandler during init
             viewModel = GameViewModel(mockGameBoardLogic, mockLobbyLogic, mockGameDataHandler, mockPlayerSessionManager, mockTradeLogic, mockCheatingLogic)
             advanceUntilIdle()

             assertEquals(initialPlayerResources, viewModel.playerResources.value)
         }


        @Test
        fun updatePlayerResourcesUpdatesStateFlowCorrectly() = runTest {
            val newResources = mapOf(
                TileType.WOOD to 5, TileType.CLAY to 2, TileType.SHEEP to 1,
                TileType.WHEAT to 0, TileType.ORE to 3
            )
            viewModel.updatePlayerResources(newResources)
            advanceUntilIdle()
            assertEquals(newResources, viewModel.playerResources.value)
        }
    }

    @Nested
    @DisplayName("Players State & Victory Points State Initialization")
    inner class InitialStatesFromDataHandler {
         @Test
         fun playersInitializesFromGameDataHandler() = runTest {
             val initialPlayers = mapOf("p1" to PlayerInfo(id = "p1", username = "Player One"))
             playersMutableStateFlow.value = initialPlayers // Simulate GameDataHandler having this state
             // Recreate ViewModel to pick up the new playersState from GameDataHandler during init
             viewModel = GameViewModel(mockGameBoardLogic, mockLobbyLogic, mockGameDataHandler, mockPlayerSessionManager, mockTradeLogic, mockCheatingLogic)
             advanceUntilIdle()
             assertEquals(initialPlayers, viewModel.players.value)
         }

         @Test
         fun victoryPointsInitializesFromGameDataHandler() = runTest {
             val initialVPs = mapOf("p1" to 5)
             victoryPointsMutableStateFlow.value = initialVPs // Simulate GameDataHandler having this state
             // Recreate ViewModel to pick up the new state from GameDataHandler during init
             viewModel = GameViewModel(mockGameBoardLogic, mockLobbyLogic, mockGameDataHandler, mockPlayerSessionManager, mockTradeLogic, mockCheatingLogic)
             advanceUntilIdle()
             assertEquals(initialVPs, viewModel.victoryPoints.value)
         }
    }


    @Nested
    @DisplayName("Turn and Player State Handlers")
    inner class TurnAndPlayerStateHandlers {
        private val otherPlayerId = "otherPlayerId"

        @Test
        fun handleEndTurnClickCallsLobbyLogicEndTurn() = runTest {
            val testLobbyId = "end-turn-lobby"
            every { mockLobbyLogic.endTurn(testLobbyId) } just runs
            viewModel.handleEndTurnClick(testLobbyId)
            advanceUntilIdle()
            verify(exactly = 1) { mockLobbyLogic.endTurn(testLobbyId) }
        }

        @Test
        fun playersStateCollectClosesBuildMenuWhenNotActivePlayer() = runTest {
            viewModel.setBuildMenuOpen(true)
            advanceUntilIdle()
            assertEquals(true, viewModel.isBuildMenuOpen.value)

            val playersMapNotActive = mapOf(
                testPlayerId to PlayerInfo(id = testPlayerId, username = "Me", isActivePlayer = false),
                otherPlayerId to PlayerInfo(id = otherPlayerId, username = "Other", isActivePlayer = true)
            )
            playersMutableStateFlow.emit(playersMapNotActive) // Emit new player state
            advanceUntilIdle() // Allow collectors to process

            assertEquals(false, viewModel.isBuildMenuOpen.value, "Build menu should be closed")
        }

         @Test
         fun playersStateCollectKeepsBuildMenuOpenWhenActivePlayer() = runTest {
             viewModel.setBuildMenuOpen(true)
             advanceUntilIdle()
             assertEquals(true, viewModel.isBuildMenuOpen.value)

             val playersMapActive = mapOf(
                 testPlayerId to PlayerInfo(id = testPlayerId, username = "Me", isActivePlayer = true),
                 otherPlayerId to PlayerInfo(id = otherPlayerId, username = "Other", isActivePlayer = false)
             )
             playersMutableStateFlow.emit(playersMapActive)
             advanceUntilIdle()

             assertEquals(true, viewModel.isBuildMenuOpen.value, "Build menu should remain open")
         }
    }
    
    @Nested
    @DisplayName("Trade Menu and Offer State")
    inner class TradeMenuAndOfferStateTests {

        @BeforeEach
        fun tradeSetup() {
            // Ensure the player has some resources for testing offer limits
            val initialPlayerResources = mapOf(TileType.WOOD to 3, TileType.SHEEP to 2)
            val initialPlayers = mapOf(testPlayerId to PlayerInfo(
                id = testPlayerId,
                username = "Test Trader",
                color = "#FF0000",
                isHost = false,
                isReady = true,
                isActivePlayer = true,
                canRollDice = false, // Set to false, as trading happens after rolling
                isSetupRound = false,
                victoryPoints = 2,
                resources = initialPlayerResources
            ))
            playersMutableStateFlow.value = initialPlayers

            // Recreate ViewModel to ensure it collects the initial player state
            viewModel = GameViewModel(mockGameBoardLogic, mockLobbyLogic, mockGameDataHandler, mockPlayerSessionManager, mockTradeLogic, mockCheatingLogic)
        }

        @Test
        fun isTradeMenuOpenDefaultsToFalse() = runTest {
            assertFalse(viewModel.isTradeMenuOpen.value)
        }

        @Test
        fun setTradeMenuOpenChangesState() = runTest {
            viewModel.setTradeMenuOpen(true)
            assertTrue(viewModel.isTradeMenuOpen.value)

            viewModel.setTradeMenuOpen(false)
            assertFalse(viewModel.isTradeMenuOpen.value)
        }

        @Test
        fun setTradeMenuOpenToFalseResetsTradeOffer() = runTest {
            viewModel.updateOfferedResource(TileType.WOOD, 1)
            viewModel.updateTargetResource(TileType.CLAY, 1)
            advanceUntilIdle()

            val expectedOffer = Pair(mapOf(TileType.WOOD to 1), mapOf(TileType.CLAY to 1))
            assertEquals(expectedOffer, viewModel.tradeOffer.value)

            viewModel.setTradeMenuOpen(false)
            advanceUntilIdle()

            val emptyOffer = Pair(emptyMap<TileType, Int>(), emptyMap<TileType, Int>())
            assertEquals(emptyOffer, viewModel.tradeOffer.value)
        }

        @Test
        fun updateOfferedResourceCorrectlyIncrementsAndDecrements() = runTest {
            // Increment
            viewModel.updateOfferedResource(TileType.WOOD, 1)
            assertEquals(mapOf(TileType.WOOD to 1), viewModel.tradeOffer.value.first)

            viewModel.updateOfferedResource(TileType.WOOD, 1)
            assertEquals(mapOf(TileType.WOOD to 2), viewModel.tradeOffer.value.first)

            // Decrement
            viewModel.updateOfferedResource(TileType.WOOD, -1)
            assertEquals(mapOf(TileType.WOOD to 1), viewModel.tradeOffer.value.first)
        }

        @Test
        fun updateOfferedResourceRemovesItemWhenCountIsZero() = runTest {
            viewModel.updateOfferedResource(TileType.SHEEP, 1)
            assertEquals(mapOf(TileType.SHEEP to 1), viewModel.tradeOffer.value.first)

            viewModel.updateOfferedResource(TileType.SHEEP, -1)
            assertTrue(viewModel.tradeOffer.value.first.isEmpty())
        }

        @Test
        fun updateOfferedResourceDoesNotGoBelowZero() = runTest {
            viewModel.updateOfferedResource(TileType.WOOD, -1)
            assertTrue(viewModel.tradeOffer.value.first.isEmpty())
        }

        @Test
        fun updateOfferedResourceDoesNotExceedPlayerResources() = runTest {
            // Player has 3 wood from setup
            viewModel.updateOfferedResource(TileType.WOOD, 1)
            viewModel.updateOfferedResource(TileType.WOOD, 1)
            viewModel.updateOfferedResource(TileType.WOOD, 1)
            assertEquals(3, viewModel.tradeOffer.value.first[TileType.WOOD])

            // This next increment should be ignored
            viewModel.updateOfferedResource(TileType.WOOD, 1)
            assertEquals(3, viewModel.tradeOffer.value.first[TileType.WOOD])
        }

        @Test
        fun updateTargetResourceCorrectlyUpdatesTargetMap() = runTest {
            viewModel.updateTargetResource(TileType.ORE, 1)
            assertEquals(mapOf(TileType.ORE to 1), viewModel.tradeOffer.value.second)

            viewModel.updateTargetResource(TileType.ORE, -1)
            assertTrue(viewModel.tradeOffer.value.second.isEmpty())
        }

        @Test
        fun submitBankTradeCallsTradeLogicAndClosesMenu() = runTest {
            val lobbyId = "trade-lobby"
            val offered = mapOf(TileType.WOOD to 2)
            val target = mapOf(TileType.CLAY to 1)
            val expectedRequest = TradeRequest(offered as Map<TileType, Int>,
                target as Map<TileType, Int>
            )

            viewModel.setTradeMenuOpen(true)
            viewModel.updateOfferedResource(TileType.WOOD, 2)
            viewModel.updateTargetResource(TileType.CLAY, 1)
            advanceUntilIdle()

            viewModel.submitBankTrade(lobbyId)
            advanceUntilIdle()

            verify(exactly = 1) { mockTradeLogic.sendBankTrade(lobbyId, expectedRequest) }
            assertFalse(viewModel.isTradeMenuOpen.value)
        }
    }
}