package com.example.cataniaunited.logic.game

import android.util.Log
import app.cash.turbine.test
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
           "tiles":[{"id":1,"type":"CLAY","value":5,"coordinates":[0.0,0.0], "isRobbed": false}],
           "settlementPositions":[{"id":1,"building":null,"coordinates":[0.0,10.0]}],
           "roads":[{"id":1,"owner":null,"color":null,"coordinates":[0.0,5.0],"rotationAngle":0.0}],
           "ports": [],
           "ringsOfBoard":1,
           "sizeOfHex":6
        }
    """
    private val anotherValidBoardJson = """
        {
           "tiles":[{"id":2,"type":"WOOD","value":6,"coordinates":[1.0,1.0], "isRobbed": false}],
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
    private val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())
    private val testPlayerId = "testPlayerId"


    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        gameBoardMutableStateFlow = MutableStateFlow<GameBoardModel?>(null)
        victoryPointsMutableStateFlow = MutableStateFlow(emptyMap())
        playersMutableStateFlow = MutableStateFlow(emptyMap()) // Initialize players flow
        diceMutableStateFlow = MutableStateFlow(null)

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

    @Test
    fun testDiceResultIsInitiallyNull() = runTest {
        assertNull(viewModel.diceResult.value, "Initial diceResult should be null")
    }

    @Test
    fun testRollDiceCallsGameBoardLogicWithCorrectLobbyId() = runTest {
        val testLobbyId = "test-lobby-abc"

        val testPlayer = PlayerInfo(id = testPlayerId, username = "TestPlayer", canRollDice = true)
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

        val testPlayer = PlayerInfo(id = testPlayerId, username = "TestPlayer", canRollDice = true)
        val playersMap = mapOf(testPlayerId to testPlayer)

        playersMutableStateFlow.value = playersMap

        advanceUntilIdle()

        every { mockGameBoardLogic.rollDice(any()) } just runs

        val isProcessingRollField = GameViewModel::class.java.getDeclaredField("isProcessingRoll")
        isProcessingRollField.isAccessible = true

        assertFalse(isProcessingRollField.get(viewModel) as Boolean, "Initially should be false")

        viewModel.rollDice(testLobbyId)
        assertTrue(isProcessingRollField.get(viewModel) as Boolean, "Should be true during processing")

        advanceUntilIdle()
        assertFalse(isProcessingRollField.get(viewModel) as Boolean, "Should be false after processing")
        }


    @Test
    fun testRollDiceDoesNothingWhenAlreadyProcessing() = runTest {
        val testLobbyId = "test-lobby-456"
        val isProcessingRollField = GameViewModel::class.java.getDeclaredField("isProcessingRoll")
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
    fun testShowResultShouldUpdatesDiceStateAndResetsAfterDelay() = runTest(dispatcher.scheduler) {
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
        fun handleSettlementClickCallsGameBoardLogicPlaceSettlement() = runTest {
            val testPosition =
                SettlementPosition(id = 5, building = null, coordinates = listOf(1.0, 2.0))
            val testLobbyId = "click-lobby-1"

            viewModel.handleSettlementClick(testPosition, false, testLobbyId)
            advanceUntilIdle()
            verify(exactly = 1) { mockGameBoardLogic.placeSettlement(testPosition.id, testLobbyId) }
        }

        @Test
        fun handleSettlementClickCallsGameBoardLogicUpgradeSettlement() = runTest {
            val testPosition =
                SettlementPosition(id = 5, building = null, coordinates = listOf(1.0, 2.0))
            val testLobbyId = "click-lobby-1"

            viewModel.handleSettlementClick(testPosition, true, testLobbyId)
            advanceUntilIdle()
            verify(exactly = 1) {
                mockGameBoardLogic.upgradeSettlement(
                    testPosition.id,
                    testLobbyId
                )
            }
        }

        @Test
        fun handleRoadClickCallsGameBoardLogicPlaceRoad() = runTest {
            val testRoad = Road(
                id = 10,
                owner = null,
                coordinates = listOf(3.0, 4.0),
                rotationAngle = 0.5,
                color = null
            )
            val testLobbyId = "click-lobby-2"

            viewModel.handleRoadClick(testRoad, testLobbyId)
            advanceUntilIdle()
            verify(exactly = 1) { mockGameBoardLogic.placeRoad(testRoad.id, testLobbyId) }
        }

        @Test
        fun handleTileClickLogsMessage() = runTest {
            val testTile =
                Tile(id = 1, type = TileType.WOOD, value = 0, coordinates = listOf(0.0, 0.0))
            val testLobbyId = "tile-lobby-1"

            viewModel.handleTileClick(testTile, testLobbyId)
            advanceUntilIdle()
            verify { Log.d("GameViewModel", "handleTileClick: Tile ID=${testTile.id}") }
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
