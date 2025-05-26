package com.example.cataniaunited.data

import com.example.cataniaunited.data.model.GameBoardModel
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.data.model.Road
import com.example.cataniaunited.data.model.SettlementPosition
import com.example.cataniaunited.data.model.Tile
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.data.util.jsonParser
import com.example.cataniaunited.data.util.parseGameBoard
import com.example.cataniaunited.logic.game.GameDataHandler
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// fixme  extract jsons into getter methods, eg., getUpdateGameBoardMessage
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
    fun updateGameBoardParsesPlayersSuccessfully() = runTest {
        val jsonString = """
        {
          "gameboard": {
            "tiles":[{"id":1,"type":"WOOD","value":8,"coordinates":[0.0,0.0]}],
            "settlementPositions":[{"id":1,"building":null,"coordinates":[10.0,10.0]}],
            "roads":[{"id":1,"owner":null,"coordinates":[5.0,5.0],"rotationAngle":0.0}],
            "ringsOfBoard":3,
            "sizeOfHex":50
          },
          "players": {
            "player1": {
              "username": "UserOne",
              "color": "#FF0000",
              "victoryPoints": 4
            },
            "player2": {
              "username": "UserTwo",
              "color": "#00FF00",
              "victoryPoints": 3
            }
          }
        }
    """.trimIndent()

        every { parseGameBoard(any()) } answers {
            GameBoardModel(
                tiles = listOf(Tile(1, TileType.WOOD, 8, listOf(0.0, 0.0))),
                settlementPositions = listOf(SettlementPosition(1, null, listOf(10.0, 10.0))),
                roads = listOf(Road(1, null, listOf(5.0, 5.0), 0.0, null)),
                ringsOfBoard = 3,
                sizeOfHex = 50
            )
        }

        gameDataHandler.updateGameBoard(jsonString)

        val board = gameDataHandler.gameBoardState.value
        assertNotNull(board)
        assertEquals(2, board?.players?.size)
        assertEquals("UserOne", board?.players?.find { it.playerId == "player1" }?.username)
        assertEquals("#00FF00", board?.players?.find { it.playerId == "player2" }?.colorHex)
        assertEquals(4, board?.players?.find { it.playerId == "player1" }?.victoryPoints)
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
            players = listOf(PlayerInfo("existing", "Old", "#000000", 2))
        )

        // Set initial state with players
        gameDataHandler.updateGameBoard("""{
        "gameboard": {},
        "players": {"existing": {"username": "Old", "color": "#000000", "victoryPoints": 2}}
    }""")

        gameDataHandler.updateGameBoard(jsonString)

        val board = gameDataHandler.gameBoardState.value
        assertNotNull(board)
        assertEquals(1, board?.players?.size) // Should keep existing players
        assertEquals("Old", board?.players?.first()?.username)
    }

    @Test
    fun updateGameBoardHandlesInvalidPlayerData() = runTest {
        val jsonString = """
    {
      "gameboard": {
        "tiles":[{"id":1,"type":"WOOD","value":8,"coordinates":[0.0,0.0]}],
        "settlementPositions":[{"id":1,"building":null,"coordinates":[10.0,10.0]}],
        "roads":[{"id":1,"owner":null,"coordinates":[5.0,5.0],"rotationAngle":0.0}],
        "ringsOfBoard":3,
        "sizeOfHex":50
      },
      "players": {
        "player1": "invalid player data",
        "player2": {
          "username": "ValidUser",
          "color": "#00FF00",
          "victoryPoints": 3
        }
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
        assertEquals(1, board?.players?.size) // Only the valid player should be included
        assertEquals("ValidUser", board?.players?.first()?.username)
    }



    @Test
    fun updateGameBoardHandlesMissingPlayerFields() = runTest {
        val jsonString = """
    {
      "gameboard": {
        "tiles":[{"id":1,"type":"WOOD","value":8,"coordinates":[0.0,0.0]}],
        "settlementPositions":[{"id":1,"building":null,"coordinates":[10.0,10.0]}],
        "roads":[{"id":1,"owner":null,"coordinates":[5.0,5.0],"rotationAngle":0.0}],
        "ringsOfBoard":3,
        "sizeOfHex":50
      },
      "players": {
        "player1": {
          "username": "UserOne"
        },
        "player2": {
          "color": "#00FF00"
        },
        "player3": {}
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
        assertEquals(3, board?.players?.size)

        val player1 = board?.players?.find { it.playerId == "player1" }
        assertEquals("UserOne", player1?.username)
        assertEquals("#8C4E27", player1?.colorHex) // Default color
        assertEquals(0, player1?.victoryPoints) // Default VP

        val player2 = board?.players?.find { it.playerId == "player2" }
        assertEquals("", player2?.username) // Default username
        assertEquals("#00FF00", player2?.colorHex)
        assertEquals(0, player2?.victoryPoints) // Default VP
    }

    @Test
    fun updateGameBoardParsesBoardAndPlayersWithFallbacks() = runTest {
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
          },
          "players": {
            "player1": {
              "username": "Alice",
              "color": "#FF5733",
              "victoryPoints": 5
            },
            "player2": {
              "color": null,
              "victoryPoints": null
            },
            "player3": {}
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
        val players = board?.players
        assertNotNull(players)
        assertEquals(3, players?.size)

        val p1 = players?.find { it.playerId == "player1" }
        assertEquals("Alice", p1?.username)
        assertEquals("#FF5733", p1?.colorHex)
        assertEquals(5, p1?.victoryPoints)

        val p2 = players?.find { it.playerId == "player2" }
        assertEquals("", p2?.username)  // default
        assertEquals("#8C4E27", p2?.colorHex)  // fallback default
        assertEquals(0, p2?.victoryPoints)  // fallback default

        val p3 = players?.find { it.playerId == "player3" }
        assertEquals("", p3?.username)
        assertEquals("#8C4E27", p3?.colorHex)
        assertEquals(0, p3?.victoryPoints)
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