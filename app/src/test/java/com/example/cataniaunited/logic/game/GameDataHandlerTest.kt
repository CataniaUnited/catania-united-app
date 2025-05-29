package com.example.cataniaunited.data

import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.data.util.parseGameBoard
import com.example.cataniaunited.logic.game.GameDataHandler
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
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
        mockkStatic(::parseGameBoard)
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
        "roads":[{"id":1,"owner":null,"coordinates":[5.0,5.0],"rotationAngle":0.0}],
        "ringsOfBoard":3,
        "sizeOfHex":50
      }
    }
    """.trimIndent()

        every { parseGameBoard(any()) } returns GameBoardModel(
            tiles = listOf(Tile(1, TileType.WOOD, 8, listOf(0.0, 0.0))),
            settlementPositions = listOf(SettlementPosition(1, null, listOf(10.0, 10.0))),
            roads = listOf(Road(1, null, listOf(5.0, 5.0), 0.0, null)),
            ringsOfBoard = 3,
            sizeOfHex = 50,
        )

        // Set initial state with players
        gameDataHandler.updateGameBoard(
            """{
        "gameboard": {},
        "players": {"existing": {"username": "Old", "color": "#000000", "victoryPoints": 2}}
    }"""
        )

        gameDataHandler.updateGameBoard(jsonString)

        val board = gameDataHandler.gameBoardState.value
        assertNotNull(board)
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
              {"id":1,"owner":null,"coordinates":[5.0,5.0],"rotationAngle":0.0}
            ],
            "ringsOfBoard": 3,
            "sizeOfHex": 50
          }
        }
    """.trimIndent()

        every { parseGameBoard(any()) } returns GameBoardModel(
            tiles = listOf(Tile(1, TileType.WOOD, 8, listOf(0.0, 0.0))),
            settlementPositions = listOf(SettlementPosition(1, null, listOf(10.0, 10.0))),
            roads = listOf(Road(1, null, listOf(5.0, 5.0), 0.0, null)),
            ringsOfBoard = 3,
            sizeOfHex = 50
        )

        gameDataHandler.updateGameBoard(jsonString)

        val board = gameDataHandler.gameBoardState.value
        assertNotNull(board)
    }


    @Test
    fun updateGameBoardHandlesExceptionAndDoesNotCrash() = runTest {
        val invalidJson = """{ not valid json }"""
        gameDataHandler.updateGameBoard(invalidJson)
        assertNull(gameDataHandler.gameBoardState.value) // Should remain null
    }


    @Test
    fun updateGameBoardSetsStateToNullOnParsingFailure() = runTest {
        val jsonString = """{"invalid":"json"}"""

        every { parseGameBoard(jsonString) } returns null

        gameDataHandler.updateGameBoard(jsonString)

        assertNull(gameDataHandler.gameBoardState.value)
        verify(exactly = 1) { parseGameBoard(jsonString) }
    }

    @Test
    fun updateGameBoardCorrectlyCopiesLists() = runTest {
        val jsonString = """{"some":"json_representing_a_board"}"""
        val originalTiles = mutableListOf(Tile(1, TileType.WOOD, 8, listOf(0.0, 0.0)))
        val originalSettlements = mutableListOf(SettlementPosition(1, null, listOf(10.0, 10.0)))
        val originalRoads = mutableListOf(Road(1, null, listOf(5.0, 5.0), 0.0, null))

        val originalBoard = GameBoardModel(
            tiles = originalTiles,
            settlementPositions = originalSettlements,
            roads = originalRoads,
            ringsOfBoard = 3,
            sizeOfHex = 50
        )

        every { parseGameBoard(jsonString) } returns originalBoard

        gameDataHandler.updateGameBoard(jsonString)

        val updatedBoard = gameDataHandler.gameBoardState.value

        assertNotNull(updatedBoard)
        assertEquals(originalTiles, updatedBoard?.tiles)
        assertEquals(originalSettlements, updatedBoard?.settlementPositions)
        assertEquals(originalRoads, updatedBoard?.roads)

        verify(exactly = 1) { parseGameBoard(jsonString) }
    }

    @Test
    fun updateGameBoardHandlesMissingGameboardSection() = runTest {
        // Test when "gameboard" key is missing - should fall back to root json
        val jsonString = """
    {
        "tiles": [{"id":1,"type":"WOOD","value":8,"coordinates":[0.0,0.0]}],
        "players": {}
    }
    """.trimIndent()

        every { parseGameBoard(any()) } returns GameBoardModel(
            tiles = listOf(Tile(1, TileType.WOOD, 8, listOf(0.0, 0.0))),
            settlementPositions = emptyList(),
            roads = emptyList(),
            ringsOfBoard = 0,
            sizeOfHex = 0
        )

        gameDataHandler.updateGameBoard(jsonString)

        verify(exactly = 1) {
            parseGameBoard(match { it.contains("\"tiles\"") && !it.contains("\"gameboard\"") })
        }
    }


}