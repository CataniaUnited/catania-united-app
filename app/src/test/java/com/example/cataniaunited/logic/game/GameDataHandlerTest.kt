package com.example.cataniaunited.logic.game

import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.model.Port
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.data.util.parseGameBoard
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameDataHandlerTest {
    private lateinit var gameDataHandler: GameDataHandler

    @BeforeEach
    fun setUp() {
        mockkStatic("com.example.cataniaunited.data.util.JsonParserKt")
        gameDataHandler = GameDataHandler()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun gameBoardStateIsInitiallyNull() = runTest {
        assertNull(gameDataHandler.gameBoardState.value)
    }

    @Test
    fun updateVictoryPointsUpdatesStateCorrectly() = runTest {
        val vpMap = mapOf("player1" to 3, "player2" to 5)
        gameDataHandler.updateVictoryPoints(vpMap)
        assertEquals(vpMap, gameDataHandler.victoryPointsState.value)
    }

    @Test
    fun updateGameBoardHandlesMissingPlayersSection() = runTest {
        val jsonString = """
    {
      "gameboard": {
        "tiles":[{"id":1,"type":"WOOD","value":8,"coordinates":[0.0,0.0]}],
        "settlementPositions":[{"id":1,"building":null,"coordinates":[10.0,10.0]}],
        "roads":[{"id":1,"owner":null,"color":null,"coordinates":[5.0,5.0],"rotationAngle":0.0}],
        "ports": [], 
        "ringsOfBoard":3,
        "sizeOfHex":50
      }
    }
    """.trimIndent()

        // When parseGameBoard is called with the content of "gameboard"
        every { parseGameBoard("""{"tiles":[{"id":1,"type":"WOOD","value":8,"coordinates":[0.0,0.0]}],"settlementPositions":[{"id":1,"building":null,"coordinates":[10.0,10.0]}],"roads":[{"id":1,"owner":null,"color":null,"coordinates":[5.0,5.0],"rotationAngle":0.0}],"ports":[],"ringsOfBoard":3,"sizeOfHex":50}""") } returns GameBoardModel(
            tiles = listOf(Tile(1, TileType.WOOD, 8, listOf(0.0, 0.0))),
            settlementPositions = listOf(SettlementPosition(1, null, listOf(10.0, 10.0))),
            roads = listOf(Road(1, null, listOf(5.0, 5.0), 0.0, null)),
            ports = emptyList(),
            ringsOfBoard = 3,
            sizeOfHex = 50
        )

        // Mock for the initial state setting (ensure it also returns a valid GameBoardModel with ports)
        every { parseGameBoard("{}") } returns GameBoardModel(
            tiles = emptyList(),
            settlementPositions = emptyList(),
            roads = emptyList(),
            ports = emptyList(),
            ringsOfBoard = 0,
            sizeOfHex = 0
        )


        // Set initial state (this call to updateGameBoard also needs its parseGameBoard mock to be valid)
        gameDataHandler.updateGameBoard(
            """{
        "gameboard": {}, 
        "players": {"existing": {"username": "Old", "color": "#000000", "victoryPoints": 2}}
    }"""
        )

        gameDataHandler.updateGameBoard(jsonString)

        val board = gameDataHandler.gameBoardState.value
        assertNotNull(board)
        assertEquals(0, board?.ports?.size)
    }

    @Test
    fun updateGameBoardParsesBoardWithFallbacks() = runTest {
        val jsonString = """
        {
          "gameboard": {
            "tiles": [
              {"id":1,"type":"WOOD","value":8,"coordinates":[0.0,0.0]}
            ],
            "settlementPositions": [
              {"id":1,"building":null,"coordinates":[10.0,10.0]}
            ],
            "roads": [
              {"id":1,"owner":null,"color":null,"coordinates":[5.0,5.0],"rotationAngle":0.0}
            ],
            "ports": [], 
            "ringsOfBoard": 3,
            "sizeOfHex": 50
          }
        }
    """.trimIndent()

        every { parseGameBoard(any()) } returns GameBoardModel(
            tiles = listOf(Tile(1, TileType.WOOD, 8, listOf(0.0, 0.0))),
            settlementPositions = listOf(SettlementPosition(1, null, listOf(10.0, 10.0))),
            roads = listOf(Road(1, null, listOf(5.0, 5.0), 0.0, null)),
            ports = emptyList(),
            ringsOfBoard = 3,
            sizeOfHex = 50
        )

        gameDataHandler.updateGameBoard(jsonString)

        val board = gameDataHandler.gameBoardState.value
        assertNotNull(board)
        assertEquals(0, board?.ports?.size)
    }


    @Test
    fun updateGameBoardHandlesExceptionAndDoesNotCrash() = runTest {
        val invalidJson = """{ not valid json }"""
        every { parseGameBoard(any()) } returns null // Simulate parsing failure returning null

        gameDataHandler.updateGameBoard(invalidJson)
        assertNull(gameDataHandler.gameBoardState.value) // Should remain null
    }


    @Test
    fun updateGameBoardSetsStateToNullOnParsingFailure() = runTest {
        val jsonString = """{"invalid":"json"}"""
        val boardJsonContent = """{"invalid":"json"}""" // This is what GameDataHandler extracts

        every { parseGameBoard(boardJsonContent) } returns null

        gameDataHandler.updateGameBoard(jsonString) // GameDataHandler extracts boardJsonContent from jsonString

        assertNull(gameDataHandler.gameBoardState.value)
        verify(exactly = 1) { parseGameBoard(boardJsonContent) }
    }

    @Test
    fun updateGameBoardCorrectlyCopiesLists() = runTest {
        val boardJsonContent = """{"tiles":[],"settlementPositions":[],"roads":[],"ports":[],"ringsOfBoard":3,"sizeOfHex":50}"""
        val jsonString = """{"gameboard":$boardJsonContent}""" // Wrap it as GameDataHandler expects

        val originalTiles = mutableListOf(Tile(1, TileType.WOOD, 8, listOf(0.0, 0.0)))
        val originalSettlements = mutableListOf(SettlementPosition(1, null, listOf(10.0, 10.0)))
        val originalRoads = mutableListOf(Road(1, null, listOf(5.0, 5.0), 0.0, null))
        val originalPorts = mutableListOf<Port>()

        val originalBoard = GameBoardModel(
            tiles = originalTiles,
            settlementPositions = originalSettlements,
            roads = originalRoads,
            ports = originalPorts,
            ringsOfBoard = 3,
            sizeOfHex = 50
        )

        every { parseGameBoard(boardJsonContent) } returns originalBoard

        gameDataHandler.updateGameBoard(jsonString)

        val updatedBoard = gameDataHandler.gameBoardState.value

        assertNotNull(updatedBoard)
        assertEquals(originalTiles, updatedBoard?.tiles)
        assertEquals(originalSettlements, updatedBoard?.settlementPositions)
        assertEquals(originalRoads, updatedBoard?.roads)
        assertEquals(originalPorts, updatedBoard?.ports)

        verify(exactly = 1) { parseGameBoard(boardJsonContent) }
    }

    @Test
    fun testUpdateDiceStateShouldUpdateDiceStateFlow() = runTest {
        val testState = GameViewModel.DiceState(
            rollingPlayerUsername = "testPlayer",
            isRolling = true,
            dice1 = 3,
            dice2 = 4,
            showResult = false
        )

        gameDataHandler.updateDiceState(testState)

        val currentState = gameDataHandler.diceState.first()
        assertEquals(testState, currentState)
    }

    @Test
    fun testUpdateDiceStateWithNullShouldClearDiceState() = runTest {
        val testState = GameViewModel.DiceState(
            rollingPlayerUsername = "testPlayer",
            isRolling = true,
            dice1 = 3,
            dice2 = 4,
            showResult = false
        )
        gameDataHandler.updateDiceState(testState)

        gameDataHandler.updateDiceState(null)

        val currentState = gameDataHandler.diceState.first()
        assertNull(currentState)
    }

    @Test
    fun showSnackbarEmitsSnackbarMessage() = runTest {
        val message = "This is a test message"
        val severity = "warning"

        gameDataHandler.showSnackbar(message, severity)

        val snackbar = gameDataHandler.snackbarMessage.first()
        assertNotNull(snackbar)
        assertEquals(message, snackbar?.first)
        assertEquals(severity, snackbar?.second)
    }

    @Test
    fun clearSnackbarEmitsNullSnackbar() = runTest {
        gameDataHandler.showSnackbar("Temporary message")

        gameDataHandler.clearSnackbar()

        val snackbar = gameDataHandler.snackbarMessage.first()
        assertNull(snackbar)
    }


}