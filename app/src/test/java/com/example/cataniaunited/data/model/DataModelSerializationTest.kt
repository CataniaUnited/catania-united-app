package com.example.cataniaunited.data.model

import com.example.cataniaunited.data.util.jsonParser
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class DataModelSerializationTest {
    private val tileJsonWood =
        """{"id":5,"type":"WOOD","value":6,"coordinates":[-8.660254037844389,-14.999999999999996]}"""
    private val tileJsonWaste =
        """{"id":17,"type":"WASTE","value":0,"coordinates":[-25.980762113533153,14.999999999999996]}"""
    private val settlementJsonNullBuilding =
        """{"id":1,"building": null,"coordinates":[1.1842378929335002E-15,9.999999999999998]}"""
    private val roadJsonNullOwner =
        """{"id":1,"owner":null, "color": null, "coordinates":[4.3301270189221945,7.499999999999998],"rotationAngle":2.6179938779914944}"""

    @Test
    fun deserializeTileWood() {
        val tile = jsonParser.decodeFromString<Tile>(tileJsonWood)
        assertEquals(5, tile.id)
        assertEquals(TileType.WOOD, tile.type)
        assertEquals(6, tile.value)
        assertEquals(-8.66025, tile.coordinates[0], 0.00001)
        assertEquals(-15.0, tile.coordinates[1], 0.00001)
    }

    @Test
    fun deserializeTileWaste() {
        val tile = jsonParser.decodeFromString<Tile>(tileJsonWaste)
        assertEquals(17, tile.id)
        assertEquals(TileType.WASTE, tile.type)
        assertEquals(0, tile.value)
    }

    @Test
    fun serializeTile() {
        val tile = Tile(id = 99, type = TileType.SHEEP, value = 3, coordinates = listOf(1.0, 2.0))
        val expectedJson = """{"id":99,"type":"SHEEP","value":3,"coordinates":[1.0,2.0]}"""
        val actualJson = jsonParser.encodeToString(tile)
        assertEquals(expectedJson, actualJson)
    }

    @Test
    fun deserializeSettlementPositionNullBuildingString() {
        val sp = jsonParser.decodeFromString<SettlementPosition>(settlementJsonNullBuilding)
        assertEquals(1, sp.id)
        assertNull(sp.building)
    }

    @Test
    fun settlementPositionActualNullBuilding() {
        val sp = SettlementPosition(id = 10, building = null, coordinates = listOf(0.0, 0.0))
        val expectedJson = """{"id":10,"building":null,"coordinates":[0.0,0.0]}"""
        val actualJson = jsonParser.encodeToString(sp)
        assertEquals(expectedJson, actualJson)
    }

    @Test
    fun deserializeRoadNullOwner() {
        val road = jsonParser.decodeFromString<Road>(roadJsonNullOwner)
        assertEquals(1, road.id)
        assertNull(road.owner, "Owner should be null") // Use JUnit 5 assertNull
    }

    @Test
    fun serializeRoadNullOwner() {
        val road = Road(
            id = 20,
            owner = null,
            coordinates = listOf(5.0, 5.0),
            rotationAngle = 0.0,
            "#000000"
        )
        val expectedJson =
            """{"id":20,"owner":null,"coordinates":[5.0,5.0],"rotationAngle":0.0,"color":"#000000"}"""
        val actualJson = jsonParser.encodeToString(road)
        assertEquals(expectedJson, actualJson)
    }

    @Test
    fun `deserialize full GameBoardModel from JSON`() {
        val board = try {
            jsonParser.decodeFromString<GameBoardModel>(fullTestBoardJson)
        } catch (e: Exception) {
            fail("Failed to parse full board JSON: ${e.message}")
        }

        assertNotNull(board, "Board should not be null")
        assertEquals(37, board.tiles.size, "Tile count mismatch")
        assertEquals(96, board.settlementPositions.size, "SettlementPosition count mismatch")
        assertEquals(132, board.roads.size, "Road count mismatch")
        assertEquals(4, board.ringsOfBoard, "Rings mismatch")
        assertEquals(6, board.sizeOfHex, "Hex size mismatch")

        assertTrue(board.tiles.isNotEmpty(), "Tiles list should not be empty")
        assertEquals(TileType.ORE, board.tiles[0].type)
        assertTrue(
            board.settlementPositions.all { it.building == null },
            "Building of settlements should be null"
        )
        assertTrue(board.roads.all { it.owner == null }, "Road owner should be null")
    }


    companion object {
        const val fullTestBoardJson = """
        {
    "tiles":
    [
        {
            "id": 1,
            "type": "ORE",
            "value": 11,
            "coordinates":
            [
                0.0,
                0.0
            ]
        },
        {
            "id": 2,
            "type": "CLAY",
            "value": 6,
            "coordinates":
            [
                8.660254037844387,
                14.999999999999996
            ]
        },
        {
            "id": 3,
            "type": "SHEEP",
            "value": 6,
            "coordinates":
            [
                17.32050807568877,
                0.0
            ]
        },
        {
            "id": 4,
            "type": "WOOD",
            "value": 3,
            "coordinates":
            [
                8.660254037844382,
                -14.999999999999996
            ]
        },
        {
            "id": 5,
            "type": "WHEAT",
            "value": 5,
            "coordinates":
            [
                -8.660254037844389,
                -14.999999999999996
            ]
        },
        {
            "id": 6,
            "type": "SHEEP",
            "value": 2,
            "coordinates":
            [
                -17.32050807568877,
                0.0
            ]
        },
        {
            "id": 7,
            "type": "SHEEP",
            "value": 12,
            "coordinates":
            [
                -8.660254037844384,
                14.999999999999996
            ]
        },
        {
            "id": 8,
            "type": "WHEAT",
            "value": 8,
            "coordinates":
            [
                17.320508075688775,
                29.999999999999993
            ]
        },
        {
            "id": 9,
            "type": "CLAY",
            "value": 11,
            "coordinates":
            [
                25.98076211353316,
                14.999999999999996
            ]
        },
        {
            "id": 10,
            "type": "ORE",
            "value": 5,
            "coordinates":
            [
                34.64101615137754,
                0.0
            ]
        },
        {
            "id": 11,
            "type": "CLAY",
            "value": 8,
            "coordinates":
            [
                25.980762113533153,
                -14.999999999999996
            ]
        },
        {
            "id": 12,
            "type": "WHEAT",
            "value": 10,
            "coordinates":
            [
                17.320508075688764,
                -29.999999999999993
            ]
        },
        {
            "id": 13,
            "type": "CLAY",
            "value": 3,
            "coordinates":
            [
                -7.105427357601002E-15,
                -29.999999999999993
            ]
        },
        {
            "id": 14,
            "type": "WHEAT",
            "value": 11,
            "coordinates":
            [
                -17.320508075688778,
                -29.999999999999993
            ]
        },
        {
            "id": 15,
            "type": "SHEEP",
            "value": 4,
            "coordinates":
            [
                -25.98076211353316,
                -14.999999999999996
            ]
        },
        {
            "id": 16,
            "type": "WHEAT",
            "value": 5,
            "coordinates":
            [
                -34.64101615137754,
                0.0
            ]
        },
        {
            "id": 17,
            "type": "WOOD",
            "value": 5,
            "coordinates":
            [
                -25.980762113533153,
                14.999999999999996
            ]
        },
        {
            "id": 18,
            "type": "CLAY",
            "value": 10,
            "coordinates":
            [
                -17.320508075688767,
                29.999999999999993
            ]
        },
        {
            "id": 19,
            "type": "ORE",
            "value": 4,
            "coordinates":
            [
                3.552713678800501E-15,
                29.999999999999993
            ]
        },
        {
            "id": 20,
            "type": "WOOD",
            "value": 2,
            "coordinates":
            [
                25.98076211353316,
                44.999999999999986
            ]
        },
        {
            "id": 21,
            "type": "SHEEP",
            "value": 12,
            "coordinates":
            [
                34.64101615137754,
                29.999999999999993
            ]
        },
        {
            "id": 22,
            "type": "ORE",
            "value": 9,
            "coordinates":
            [
                43.30127018922193,
                14.999999999999996
            ]
        },
        {
            "id": 23,
            "type": "CLAY",
            "value": 3,
            "coordinates":
            [
                51.96152422706631,
                0.0
            ]
        },
        {
            "id": 24,
            "type": "WOOD",
            "value": 3,
            "coordinates":
            [
                43.301270189221924,
                -14.999999999999996
            ]
        },
        {
            "id": 25,
            "type": "WOOD",
            "value": 6,
            "coordinates":
            [
                34.641016151377535,
                -29.999999999999993
            ]
        },
        {
            "id": 26,
            "type": "WASTE",
            "value": 0,
            "coordinates":
            [
                25.980762113533146,
                -44.999999999999986
            ]
        },
        {
            "id": 27,
            "type": "WOOD",
            "value": 8,
            "coordinates":
            [
                8.660254037844375,
                -44.999999999999986
            ]
        },
        {
            "id": 28,
            "type": "WOOD",
            "value": 6,
            "coordinates":
            [
                -8.660254037844396,
                -44.999999999999986
            ]
        },
        {
            "id": 29,
            "type": "ORE",
            "value": 4,
            "coordinates":
            [
                -25.980762113533167,
                -44.999999999999986
            ]
        },
        {
            "id": 30,
            "type": "SHEEP",
            "value": 9,
            "coordinates":
            [
                -34.64101615137755,
                -29.999999999999993
            ]
        },
        {
            "id": 31,
            "type": "WHEAT",
            "value": 12,
            "coordinates":
            [
                -43.30127018922193,
                -14.999999999999996
            ]
        },
        {
            "id": 32,
            "type": "CLAY",
            "value": 2,
            "coordinates":
            [
                -51.96152422706631,
                0.0
            ]
        },
        {
            "id": 33,
            "type": "SHEEP",
            "value": 8,
            "coordinates":
            [
                -43.301270189221924,
                14.999999999999996
            ]
        },
        {
            "id": 34,
            "type": "WHEAT",
            "value": 10,
            "coordinates":
            [
                -34.64101615137754,
                29.999999999999993
            ]
        },
        {
            "id": 35,
            "type": "WHEAT",
            "value": 11,
            "coordinates":
            [
                -25.980762113533153,
                44.999999999999986
            ]
        },
        {
            "id": 36,
            "type": "ORE",
            "value": 10,
            "coordinates":
            [
                -8.660254037844382,
                44.999999999999986
            ]
        },
        {
            "id": 37,
            "type": "ORE",
            "value": 9,
            "coordinates":
            [
                8.660254037844389,
                44.999999999999986
            ]
        }
    ],
    "settlementPositions":
    [
        {
            "id": 1,
            "building": null,
            "coordinates":
            [
                1.1842378929335002E-15,
                9.999999999999998
            ]
        },
        {
            "id": 2,
            "building": null,
            "coordinates":
            [
                8.660254037844387,
                4.999999999999999
            ]
        },
        {
            "id": 3,
            "building": null,
            "coordinates":
            [
                8.660254037844384,
                -4.999999999999999
            ]
        },
        {
            "id": 4,
            "building": null,
            "coordinates":
            [
                -2.3684757858670005E-15,
                -9.999999999999998
            ]
        },
        {
            "id": 5,
            "building": null,
            "coordinates":
            [
                -8.660254037844387,
                -4.999999999999999
            ]
        },
        {
            "id": 6,
            "building": null,
            "coordinates":
            [
                -8.660254037844384,
                4.999999999999999
            ]
        },
        {
            "id": 7,
            "building": null,
            "coordinates":
            [
                2.3684757858670005E-15,
                19.999999999999996
            ]
        },
        {
            "id": 8,
            "building": null,
            "coordinates":
            [
                8.660254037844387,
                24.99999999999999
            ]
        },
        {
            "id": 9,
            "building": null,
            "coordinates":
            [
                17.320508075688775,
                19.999999999999996
            ]
        },
        {
            "id": 10,
            "building": null,
            "coordinates":
            [
                17.320508075688775,
                9.999999999999998
            ]
        },
        {
            "id": 11,
            "building": null,
            "coordinates":
            [
                25.98076211353316,
                4.999999999999999
            ]
        },
        {
            "id": 12,
            "building": null,
            "coordinates":
            [
                25.980762113533157,
                -4.999999999999999
            ]
        },
        {
            "id": 13,
            "building": null,
            "coordinates":
            [
                17.320508075688767,
                -9.999999999999998
            ]
        },
        {
            "id": 14,
            "building": null,
            "coordinates":
            [
                17.320508075688767,
                -19.999999999999996
            ]
        },
        {
            "id": 15,
            "building": null,
            "coordinates":
            [
                8.66025403784438,
                -24.99999999999999
            ]
        },
        {
            "id": 16,
            "building": null,
            "coordinates":
            [
                -4.736951571734001E-15,
                -19.999999999999996
            ]
        },
        {
            "id": 17,
            "building": null,
            "coordinates":
            [
                -8.66025403784439,
                -24.99999999999999
            ]
        },
        {
            "id": 18,
            "building": null,
            "coordinates":
            [
                -17.320508075688775,
                -19.999999999999996
            ]
        },
        {
            "id": 19,
            "building": null,
            "coordinates":
            [
                -17.320508075688775,
                -9.999999999999998
            ]
        },
        {
            "id": 20,
            "building": null,
            "coordinates":
            [
                -25.98076211353316,
                -4.999999999999999
            ]
        },
        {
            "id": 21,
            "building": null,
            "coordinates":
            [
                -25.980762113533157,
                4.999999999999999
            ]
        },
        {
            "id": 22,
            "building": null,
            "coordinates":
            [
                -17.320508075688767,
                9.999999999999998
            ]
        },
        {
            "id": 23,
            "building": null,
            "coordinates":
            [
                -17.320508075688767,
                19.999999999999996
            ]
        },
        {
            "id": 24,
            "building": null,
            "coordinates":
            [
                -8.660254037844384,
                24.99999999999999
            ]
        },
        {
            "id": 25,
            "building": null,
            "coordinates":
            [
                8.660254037844389,
                34.99999999999999
            ]
        },
        {
            "id": 26,
            "building": null,
            "coordinates":
            [
                17.320508075688775,
                39.999999999999986
            ]
        },
        {
            "id": 27,
            "building": null,
            "coordinates":
            [
                25.98076211353316,
                34.99999999999999
            ]
        },
        {
            "id": 28,
            "building": null,
            "coordinates":
            [
                25.98076211353316,
                24.99999999999999
            ]
        },
        {
            "id": 29,
            "building": null,
            "coordinates":
            [
                34.64101615137755,
                19.999999999999996
            ]
        },
        {
            "id": 30,
            "building": null,
            "coordinates":
            [
                34.64101615137755,
                9.999999999999998
            ]
        },
        {
            "id": 31,
            "building": null,
            "coordinates":
            [
                43.30127018922193,
                4.999999999999999
            ]
        },
        {
            "id": 32,
            "building": null,
            "coordinates":
            [
                43.301270189221924,
                -4.999999999999999
            ]
        },
        {
            "id": 33,
            "building": null,
            "coordinates":
            [
                34.641016151377535,
                -9.999999999999998
            ]
        },
        {
            "id": 34,
            "building": null,
            "coordinates":
            [
                34.641016151377535,
                -19.999999999999996
            ]
        },
        {
            "id": 35,
            "building": null,
            "coordinates":
            [
                25.98076211353315,
                -24.99999999999999
            ]
        },
        {
            "id": 36,
            "building": null,
            "coordinates":
            [
                25.98076211353315,
                -34.99999999999999
            ]
        },
        {
            "id": 37,
            "building": null,
            "coordinates":
            [
                17.32050807568876,
                -39.999999999999986
            ]
        },
        {
            "id": 38,
            "building": null,
            "coordinates":
            [
                8.660254037844377,
                -34.99999999999999
            ]
        },
        {
            "id": 39,
            "building": null,
            "coordinates":
            [
                -9.473903143468002E-15,
                -39.999999999999986
            ]
        },
        {
            "id": 40,
            "building": null,
            "coordinates":
            [
                -8.660254037844394,
                -34.99999999999999
            ]
        },
        {
            "id": 41,
            "building": null,
            "coordinates":
            [
                -17.32050807568878,
                -39.999999999999986
            ]
        },
        {
            "id": 42,
            "building": null,
            "coordinates":
            [
                -25.980762113533164,
                -34.99999999999999
            ]
        },
        {
            "id": 43,
            "building": null,
            "coordinates":
            [
                -25.98076211353316,
                -24.99999999999999
            ]
        },
        {
            "id": 44,
            "building": null,
            "coordinates":
            [
                -34.64101615137755,
                -19.999999999999996
            ]
        },
        {
            "id": 45,
            "building": null,
            "coordinates":
            [
                -34.64101615137755,
                -9.999999999999998
            ]
        },
        {
            "id": 46,
            "building": null,
            "coordinates":
            [
                -43.30127018922193,
                -4.999999999999999
            ]
        },
        {
            "id": 47,
            "building": null,
            "coordinates":
            [
                -43.301270189221924,
                4.999999999999999
            ]
        },
        {
            "id": 48,
            "building": null,
            "coordinates":
            [
                -34.641016151377535,
                9.999999999999998
            ]
        },
        {
            "id": 49,
            "building": null,
            "coordinates":
            [
                -34.64101615137754,
                19.999999999999996
            ]
        },
        {
            "id": 50,
            "building": null,
            "coordinates":
            [
                -25.980762113533157,
                24.99999999999999
            ]
        },
        {
            "id": 51,
            "building": null,
            "coordinates":
            [
                -25.98076211353315,
                34.99999999999999
            ]
        },
        {
            "id": 52,
            "building": null,
            "coordinates":
            [
                -17.320508075688767,
                39.999999999999986
            ]
        },
        {
            "id": 53,
            "building": null,
            "coordinates":
            [
                -8.660254037844382,
                34.99999999999999
            ]
        },
        {
            "id": 54,
            "building": null,
            "coordinates":
            [
                3.552713678800501E-15,
                39.999999999999986
            ]
        },
        {
            "id": 55,
            "building": null,
            "coordinates":
            [
                17.320508075688775,
                49.999999999999986
            ]
        },
        {
            "id": 56,
            "building": null,
            "coordinates":
            [
                25.980762113533164,
                54.999999999999986
            ]
        },
        {
            "id": 57,
            "building": null,
            "coordinates":
            [
                34.64101615137754,
                49.999999999999986
            ]
        },
        {
            "id": 58,
            "building": null,
            "coordinates":
            [
                34.64101615137754,
                39.99999999999998
            ]
        },
        {
            "id": 59,
            "building": null,
            "coordinates":
            [
                43.30127018922194,
                34.99999999999998
            ]
        },
        {
            "id": 60,
            "building": null,
            "coordinates":
            [
                43.30127018922193,
                24.99999999999999
            ]
        },
        {
            "id": 61,
            "building": null,
            "coordinates":
            [
                51.96152422706631,
                19.99999999999999
            ]
        },
        {
            "id": 62,
            "building": null,
            "coordinates":
            [
                51.96152422706631,
                9.999999999999996
            ]
        },
        {
            "id": 63,
            "building": null,
            "coordinates":
            [
                60.621778264910695,
                4.999999999999997
            ]
        },
        {
            "id": 64,
            "building": null,
            "coordinates":
            [
                60.621778264910695,
                -4.999999999999999
            ]
        },
        {
            "id": 65,
            "building": null,
            "coordinates":
            [
                51.96152422706632,
                -9.999999999999996
            ]
        },
        {
            "id": 66,
            "building": null,
            "coordinates":
            [
                51.96152422706632,
                -19.99999999999999
            ]
        },
        {
            "id": 67,
            "building": null,
            "coordinates":
            [
                43.30127018922192,
                -24.99999999999999
            ]
        },
        {
            "id": 68,
            "building": null,
            "coordinates":
            [
                43.30127018922192,
                -34.99999999999998
            ]
        },
        {
            "id": 69,
            "building": null,
            "coordinates":
            [
                34.64101615137753,
                -39.99999999999998
            ]
        },
        {
            "id": 70,
            "building": null,
            "coordinates":
            [
                34.64101615137753,
                -49.99999999999998
            ]
        },
        {
            "id": 71,
            "building": null,
            "coordinates":
            [
                25.980762113533146,
                -54.999999999999986
            ]
        },
        {
            "id": 72,
            "building": null,
            "coordinates":
            [
                17.32050807568876,
                -49.999999999999986
            ]
        },
        {
            "id": 73,
            "building": null,
            "coordinates":
            [
                8.660254037844375,
                -54.999999999999986
            ]
        },
        {
            "id": 74,
            "building": null,
            "coordinates":
            [
                -1.1842378929335004E-14,
                -49.999999999999986
            ]
        },
        {
            "id": 75,
            "building": null,
            "coordinates":
            [
                -8.660254037844396,
                -54.999999999999986
            ]
        },
        {
            "id": 76,
            "building": null,
            "coordinates":
            [
                -17.32050807568878,
                -49.999999999999986
            ]
        },
        {
            "id": 77,
            "building": null,
            "coordinates":
            [
                -25.98076211353317,
                -54.999999999999986
            ]
        },
        {
            "id": 78,
            "building": null,
            "coordinates":
            [
                -34.64101615137756,
                -49.999999999999986
            ]
        },
        {
            "id": 79,
            "building": null,
            "coordinates":
            [
                -34.641016151377556,
                -39.99999999999998
            ]
        },
        {
            "id": 80,
            "building": null,
            "coordinates":
            [
                -43.30127018922193,
                -34.99999999999998
            ]
        },
        {
            "id": 81,
            "building": null,
            "coordinates":
            [
                -43.30127018922193,
                -24.99999999999999
            ]
        },
        {
            "id": 82,
            "building": null,
            "coordinates":
            [
                -51.96152422706631,
                -19.99999999999999
            ]
        },
        {
            "id": 83,
            "building": null,
            "coordinates":
            [
                -51.96152422706631,
                -9.999999999999996
            ]
        },
        {
            "id": 84,
            "building": null,
            "coordinates":
            [
                -60.621778264910695,
                -4.999999999999997
            ]
        },
        {
            "id": 85,
            "building": null,
            "coordinates":
            [
                -60.621778264910695,
                4.999999999999999
            ]
        },
        {
            "id": 86,
            "building": null,
            "coordinates":
            [
                -51.96152422706632,
                9.999999999999996
            ]
        },
        {
            "id": 87,
            "building": null,
            "coordinates":
            [
                -51.96152422706632,
                19.99999999999999
            ]
        },
        {
            "id": 88,
            "building": null,
            "coordinates":
            [
                -43.301270189221924,
                24.99999999999999
            ]
        },
        {
            "id": 89,
            "building": null,
            "coordinates":
            [
                -43.301270189221924,
                34.99999999999998
            ]
        },
        {
            "id": 90,
            "building": null,
            "coordinates":
            [
                -34.64101615137754,
                39.99999999999998
            ]
        },
        {
            "id": 91,
            "building": null,
            "coordinates":
            [
                -34.64101615137754,
                49.99999999999998
            ]
        },
        {
            "id": 92,
            "building": null,
            "coordinates":
            [
                -25.980762113533153,
                54.999999999999986
            ]
        },
        {
            "id": 93,
            "building": null,
            "coordinates":
            [
                -17.320508075688767,
                49.999999999999986
            ]
        },
        {
            "id": 94,
            "building": null,
            "coordinates":
            [
                -8.660254037844382,
                54.999999999999986
            ]
        },
        {
            "id": 95,
            "building": null,
            "coordinates":
            [
                3.552713678800501E-15,
                49.999999999999986
            ]
        },
        {
            "id": 96,
            "building": null,
            "coordinates":
            [
                8.660254037844389,
                54.999999999999986
            ]
        }
    ],
    "roads":
    [
        {
            "id": 1,
            "owner": null,
            "color": null,
            "coordinates":
            [
                4.3301270189221945,
                7.499999999999998
            ],
            "rotationAngle": 2.6179938779914944
        },
        {
            "id": 2,
            "owner": null,
            "color": null,
            "coordinates":
            [
                8.660254037844386,
                0.0
            ],
            "rotationAngle": 1.5707963267948963
        },
        {
            "id": 3,
            "owner": null,
            "color": null,
            "coordinates":
            [
                4.330127018922191,
                -7.499999999999998
            ],
            "rotationAngle": 0.5235987755982988
        },
        {
            "id": 4,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -4.3301270189221945,
                -7.499999999999998
            ],
            "rotationAngle": -0.5235987755982988
        },
        {
            "id": 5,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -8.660254037844386,
                0.0
            ],
            "rotationAngle": -1.570796326794897
        },
        {
            "id": 6,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -4.330127018922191,
                7.499999999999998
            ],
            "rotationAngle": -2.6179938779914944
        },
        {
            "id": 7,
            "owner": null,
            "color": null,
            "coordinates":
            [
                1.7763568394002505E-15,
                14.999999999999996
            ],
            "rotationAngle": -1.5707963267948968
        },
        {
            "id": 8,
            "owner": null,
            "color": null,
            "coordinates":
            [
                4.3301270189221945,
                22.499999999999993
            ],
            "rotationAngle": -2.617993877991495
        },
        {
            "id": 9,
            "owner": null,
            "color": null,
            "coordinates":
            [
                12.99038105676658,
                22.499999999999993
            ],
            "rotationAngle": 2.617993877991495
        },
        {
            "id": 10,
            "owner": null,
            "color": null,
            "coordinates":
            [
                17.320508075688775,
                14.999999999999996
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 11,
            "owner": null,
            "color": null,
            "coordinates":
            [
                12.99038105676658,
                7.499999999999998
            ],
            "rotationAngle": -2.6179938779914944
        },
        {
            "id": 12,
            "owner": null,
            "color": null,
            "coordinates":
            [
                21.65063509461097,
                7.499999999999998
            ],
            "rotationAngle": 2.6179938779914944
        },
        {
            "id": 13,
            "owner": null,
            "color": null,
            "coordinates":
            [
                25.98076211353316,
                0.0
            ],
            "rotationAngle": 1.5707963267948963
        },
        {
            "id": 14,
            "owner": null,
            "color": null,
            "coordinates":
            [
                21.650635094610962,
                -7.499999999999998
            ],
            "rotationAngle": 0.5235987755982987
        },
        {
            "id": 15,
            "owner": null,
            "color": null,
            "coordinates":
            [
                12.990381056766577,
                -7.499999999999998
            ],
            "rotationAngle": 2.6179938779914944
        },
        {
            "id": 16,
            "owner": null,
            "color": null,
            "coordinates":
            [
                17.320508075688767,
                -14.999999999999996
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 17,
            "owner": null,
            "color": null,
            "coordinates":
            [
                12.990381056766573,
                -22.499999999999993
            ],
            "rotationAngle": 0.5235987755982981
        },
        {
            "id": 18,
            "owner": null,
            "color": null,
            "coordinates":
            [
                4.330127018922187,
                -22.499999999999993
            ],
            "rotationAngle": -0.5235987755982983
        },
        {
            "id": 19,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -3.552713678800501E-15,
                -14.999999999999996
            ],
            "rotationAngle": 1.5707963267948963
        },
        {
            "id": 20,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -4.330127018922198,
                -22.499999999999993
            ],
            "rotationAngle": 0.5235987755982983
        },
        {
            "id": 21,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -12.990381056766584,
                -22.499999999999993
            ],
            "rotationAngle": -0.5235987755982985
        },
        {
            "id": 22,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -17.320508075688775,
                -14.999999999999996
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 23,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -12.99038105676658,
                -7.499999999999998
            ],
            "rotationAngle": 0.5235987755982987
        },
        {
            "id": 24,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -21.65063509461097,
                -7.499999999999998
            ],
            "rotationAngle": -0.5235987755982988
        },
        {
            "id": 25,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -25.98076211353316,
                0.0
            ],
            "rotationAngle": -1.570796326794897
        },
        {
            "id": 26,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -21.650635094610962,
                7.499999999999998
            ],
            "rotationAngle": -2.6179938779914944
        },
        {
            "id": 27,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -12.990381056766577,
                7.499999999999998
            ],
            "rotationAngle": -0.5235987755982989
        },
        {
            "id": 28,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -17.320508075688767,
                14.999999999999996
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 29,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -12.990381056766577,
                22.499999999999993
            ],
            "rotationAngle": -2.617993877991495
        },
        {
            "id": 30,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -4.330127018922191,
                22.499999999999993
            ],
            "rotationAngle": -0.5235987755982983
        },
        {
            "id": 31,
            "owner": null,
            "color": null,
            "coordinates":
            [
                8.660254037844389,
                29.999999999999993
            ],
            "rotationAngle": -1.570796326794897
        },
        {
            "id": 32,
            "owner": null,
            "color": null,
            "coordinates":
            [
                12.990381056766582,
                37.499999999999986
            ],
            "rotationAngle": -2.617993877991495
        },
        {
            "id": 33,
            "owner": null,
            "color": null,
            "coordinates":
            [
                21.65063509461097,
                37.499999999999986
            ],
            "rotationAngle": 2.617993877991495
        },
        {
            "id": 34,
            "owner": null,
            "color": null,
            "coordinates":
            [
                25.98076211353316,
                29.999999999999993
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 35,
            "owner": null,
            "color": null,
            "coordinates":
            [
                21.65063509461097,
                22.499999999999993
            ],
            "rotationAngle": -2.617993877991495
        },
        {
            "id": 36,
            "owner": null,
            "color": null,
            "coordinates":
            [
                30.310889132455355,
                22.499999999999993
            ],
            "rotationAngle": 2.617993877991495
        },
        {
            "id": 37,
            "owner": null,
            "color": null,
            "coordinates":
            [
                34.64101615137755,
                14.999999999999996
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 38,
            "owner": null,
            "color": null,
            "coordinates":
            [
                30.310889132455355,
                7.499999999999998
            ],
            "rotationAngle": -2.6179938779914944
        },
        {
            "id": 39,
            "owner": null,
            "color": null,
            "coordinates":
            [
                38.97114317029974,
                7.499999999999998
            ],
            "rotationAngle": 2.617993877991494
        },
        {
            "id": 40,
            "owner": null,
            "color": null,
            "coordinates":
            [
                43.301270189221924,
                0.0
            ],
            "rotationAngle": 1.570796326794896
        },
        {
            "id": 41,
            "owner": null,
            "color": null,
            "coordinates":
            [
                38.971143170299726,
                -7.499999999999998
            ],
            "rotationAngle": 0.5235987755982987
        },
        {
            "id": 42,
            "owner": null,
            "color": null,
            "coordinates":
            [
                30.310889132455344,
                -7.499999999999998
            ],
            "rotationAngle": 2.617993877991494
        },
        {
            "id": 43,
            "owner": null,
            "color": null,
            "coordinates":
            [
                34.641016151377535,
                -14.999999999999996
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 44,
            "owner": null,
            "color": null,
            "coordinates":
            [
                30.310889132455344,
                -22.499999999999993
            ],
            "rotationAngle": 0.5235987755982983
        },
        {
            "id": 45,
            "owner": null,
            "color": null,
            "coordinates":
            [
                21.65063509461096,
                -22.499999999999993
            ],
            "rotationAngle": 2.617993877991495
        },
        {
            "id": 46,
            "owner": null,
            "color": null,
            "coordinates":
            [
                25.98076211353315,
                -29.999999999999993
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 47,
            "owner": null,
            "color": null,
            "coordinates":
            [
                21.650635094610955,
                -37.499999999999986
            ],
            "rotationAngle": 0.5235987755982981
        },
        {
            "id": 48,
            "owner": null,
            "color": null,
            "coordinates":
            [
                12.99038105676657,
                -37.499999999999986
            ],
            "rotationAngle": -0.5235987755982985
        },
        {
            "id": 49,
            "owner": null,
            "color": null,
            "coordinates":
            [
                8.660254037844378,
                -29.999999999999993
            ],
            "rotationAngle": 1.5707963267948963
        },
        {
            "id": 50,
            "owner": null,
            "color": null,
            "coordinates":
            [
                4.330127018922184,
                -37.499999999999986
            ],
            "rotationAngle": 0.5235987755982983
        },
        {
            "id": 51,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -4.330127018922202,
                -37.499999999999986
            ],
            "rotationAngle": -0.5235987755982983
        },
        {
            "id": 52,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -8.660254037844393,
                -29.999999999999993
            ],
            "rotationAngle": 1.5707963267948963
        },
        {
            "id": 53,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -12.990381056766587,
                -37.499999999999986
            ],
            "rotationAngle": 0.5235987755982981
        },
        {
            "id": 54,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -21.650635094610973,
                -37.499999999999986
            ],
            "rotationAngle": -0.5235987755982985
        },
        {
            "id": 55,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -25.98076211353316,
                -29.999999999999993
            ],
            "rotationAngle": -1.570796326794897
        },
        {
            "id": 56,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -21.65063509461097,
                -22.499999999999993
            ],
            "rotationAngle": 0.5235987755982983
        },
        {
            "id": 57,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -30.310889132455355,
                -22.499999999999993
            ],
            "rotationAngle": -0.5235987755982981
        },
        {
            "id": 58,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -34.64101615137755,
                -14.999999999999996
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 59,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -30.310889132455355,
                -7.499999999999998
            ],
            "rotationAngle": 0.5235987755982987
        },
        {
            "id": 60,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -38.97114317029974,
                -7.499999999999998
            ],
            "rotationAngle": -0.523598775598299
        },
        {
            "id": 61,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -43.301270189221924,
                0.0
            ],
            "rotationAngle": -1.5707963267948974
        },
        {
            "id": 62,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -38.971143170299726,
                7.499999999999998
            ],
            "rotationAngle": -2.6179938779914944
        },
        {
            "id": 63,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -30.310889132455344,
                7.499999999999998
            ],
            "rotationAngle": -0.5235987755982991
        },
        {
            "id": 64,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -34.64101615137754,
                14.999999999999996
            ],
            "rotationAngle": -1.570796326794896
        },
        {
            "id": 65,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -30.31088913245535,
                22.499999999999993
            ],
            "rotationAngle": -2.617993877991495
        },
        {
            "id": 66,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -21.650635094610962,
                22.499999999999993
            ],
            "rotationAngle": -0.5235987755982981
        },
        {
            "id": 67,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -25.980762113533153,
                29.999999999999993
            ],
            "rotationAngle": -1.5707963267948974
        },
        {
            "id": 68,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -21.65063509461096,
                37.499999999999986
            ],
            "rotationAngle": -2.617993877991495
        },
        {
            "id": 69,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -12.990381056766575,
                37.499999999999986
            ],
            "rotationAngle": 2.617993877991495
        },
        {
            "id": 70,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -8.660254037844382,
                29.999999999999993
            ],
            "rotationAngle": -1.570796326794897
        },
        {
            "id": 71,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -4.330127018922189,
                37.499999999999986
            ],
            "rotationAngle": -2.617993877991495
        },
        {
            "id": 72,
            "owner": null,
            "color": null,
            "coordinates":
            [
                4.330127018922196,
                37.499999999999986
            ],
            "rotationAngle": -0.5235987755982983
        },
        {
            "id": 73,
            "owner": null,
            "color": null,
            "coordinates":
            [
                17.320508075688775,
                44.999999999999986
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 74,
            "owner": null,
            "color": null,
            "coordinates":
            [
                21.65063509461097,
                52.499999999999986
            ],
            "rotationAngle": -2.6179938779914944
        },
        {
            "id": 75,
            "owner": null,
            "color": null,
            "coordinates":
            [
                30.31088913245535,
                52.499999999999986
            ],
            "rotationAngle": 2.617993877991494
        },
        {
            "id": 76,
            "owner": null,
            "color": null,
            "coordinates":
            [
                34.64101615137754,
                44.999999999999986
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 77,
            "owner": null,
            "color": null,
            "coordinates":
            [
                30.31088913245535,
                37.499999999999986
            ],
            "rotationAngle": -2.6179938779914953
        },
        {
            "id": 78,
            "owner": null,
            "color": null,
            "coordinates":
            [
                38.97114317029974,
                37.49999999999998
            ],
            "rotationAngle": 2.617993877991495
        },
        {
            "id": 79,
            "owner": null,
            "color": null,
            "coordinates":
            [
                43.30127018922194,
                29.999999999999986
            ],
            "rotationAngle": 1.570796326794896
        },
        {
            "id": 80,
            "owner": null,
            "color": null,
            "coordinates":
            [
                38.97114317029974,
                22.499999999999993
            ],
            "rotationAngle": -2.617993877991495
        },
        {
            "id": 81,
            "owner": null,
            "color": null,
            "coordinates":
            [
                47.63139720814412,
                22.49999999999999
            ],
            "rotationAngle": 2.617993877991494
        },
        {
            "id": 82,
            "owner": null,
            "color": null,
            "coordinates":
            [
                51.96152422706631,
                14.999999999999993
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 83,
            "owner": null,
            "color": null,
            "coordinates":
            [
                47.63139720814412,
                7.499999999999998
            ],
            "rotationAngle": -2.6179938779914944
        },
        {
            "id": 84,
            "owner": null,
            "color": null,
            "coordinates":
            [
                56.291651245988504,
                7.4999999999999964
            ],
            "rotationAngle": 2.617993877991494
        },
        {
            "id": 85,
            "owner": null,
            "color": null,
            "coordinates":
            [
                60.621778264910695,
                -8.881784197001252E-16
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 86,
            "owner": null,
            "color": null,
            "coordinates":
            [
                56.291651245988504,
                -7.499999999999998
            ],
            "rotationAngle": 0.5235987755982991
        },
        {
            "id": 87,
            "owner": null,
            "color": null,
            "coordinates":
            [
                47.63139720814412,
                -7.499999999999998
            ],
            "rotationAngle": 2.617993877991495
        },
        {
            "id": 88,
            "owner": null,
            "color": null,
            "coordinates":
            [
                51.96152422706632,
                -14.999999999999993
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 89,
            "owner": null,
            "color": null,
            "coordinates":
            [
                47.63139720814412,
                -22.49999999999999
            ],
            "rotationAngle": 0.523598775598298
        },
        {
            "id": 90,
            "owner": null,
            "color": null,
            "coordinates":
            [
                38.971143170299726,
                -22.499999999999993
            ],
            "rotationAngle": 2.617993877991495
        },
        {
            "id": 91,
            "owner": null,
            "color": null,
            "coordinates":
            [
                43.30127018922192,
                -29.999999999999986
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 92,
            "owner": null,
            "color": null,
            "coordinates":
            [
                38.971143170299726,
                -37.49999999999998
            ],
            "rotationAngle": 0.5235987755982987
        },
        {
            "id": 93,
            "owner": null,
            "color": null,
            "coordinates":
            [
                30.310889132455337,
                -37.499999999999986
            ],
            "rotationAngle": 2.6179938779914953
        },
        {
            "id": 94,
            "owner": null,
            "color": null,
            "coordinates":
            [
                34.64101615137753,
                -44.99999999999998
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 95,
            "owner": null,
            "color": null,
            "coordinates":
            [
                30.310889132455337,
                -52.499999999999986
            ],
            "rotationAngle": 0.5235987755982997
        },
        {
            "id": 96,
            "owner": null,
            "color": null,
            "coordinates":
            [
                21.650635094610955,
                -52.499999999999986
            ],
            "rotationAngle": -0.5235987755982989
        },
        {
            "id": 97,
            "owner": null,
            "color": null,
            "coordinates":
            [
                17.32050807568876,
                -44.999999999999986
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 98,
            "owner": null,
            "color": null,
            "coordinates":
            [
                12.990381056766568,
                -52.499999999999986
            ],
            "rotationAngle": 0.5235987755982989
        },
        {
            "id": 99,
            "owner": null,
            "color": null,
            "coordinates":
            [
                4.330127018922181,
                -52.499999999999986
            ],
            "rotationAngle": -0.5235987755982988
        },
        {
            "id": 100,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -1.0658141036401503E-14,
                -44.999999999999986
            ],
            "rotationAngle": 1.5707963267948963
        },
        {
            "id": 101,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -4.330127018922204,
                -52.499999999999986
            ],
            "rotationAngle": 0.523598775598299
        },
        {
            "id": 102,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -12.990381056766589,
                -52.499999999999986
            ],
            "rotationAngle": -0.5235987755982989
        },
        {
            "id": 103,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -17.32050807568878,
                -44.999999999999986
            ],
            "rotationAngle": 1.5707963267948966
        },
        {
            "id": 104,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -21.650635094610976,
                -52.499999999999986
            ],
            "rotationAngle": 0.5235987755982987
        },
        {
            "id": 105,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -30.310889132455365,
                -52.499999999999986
            ],
            "rotationAngle": -0.5235987755982986
        },
        {
            "id": 106,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -34.641016151377556,
                -44.999999999999986
            ],
            "rotationAngle": -1.5707963267948974
        },
        {
            "id": 107,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -30.310889132455358,
                -37.499999999999986
            ],
            "rotationAngle": 0.5235987755982974
        },
        {
            "id": 108,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -38.97114317029974,
                -37.49999999999998
            ],
            "rotationAngle": -0.5235987755982994
        },
        {
            "id": 109,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -43.30127018922193,
                -29.999999999999986
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 110,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -38.97114317029974,
                -22.499999999999993
            ],
            "rotationAngle": 0.5235987755982985
        },
        {
            "id": 111,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -47.63139720814412,
                -22.49999999999999
            ],
            "rotationAngle": -0.5235987755982991
        },
        {
            "id": 112,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -51.96152422706631,
                -14.999999999999993
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 113,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -47.63139720814412,
                -7.499999999999998
            ],
            "rotationAngle": 0.5235987755982988
        },
        {
            "id": 114,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -56.291651245988504,
                -7.4999999999999964
            ],
            "rotationAngle": -0.523598775598299
        },
        {
            "id": 115,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -60.621778264910695,
                8.881784197001252E-16
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 116,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -56.291651245988504,
                7.499999999999998
            ],
            "rotationAngle": -2.617993877991494
        },
        {
            "id": 117,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -47.63139720814412,
                7.499999999999998
            ],
            "rotationAngle": -0.5235987755982981
        },
        {
            "id": 118,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -51.96152422706632,
                14.999999999999993
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 119,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -47.63139720814412,
                22.49999999999999
            ],
            "rotationAngle": -2.617993877991495
        },
        {
            "id": 120,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -38.97114317029973,
                22.499999999999993
            ],
            "rotationAngle": -0.5235987755982985
        },
        {
            "id": 121,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -43.301270189221924,
                29.999999999999986
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 122,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -38.97114317029973,
                37.49999999999998
            ],
            "rotationAngle": -2.617993877991494
        },
        {
            "id": 123,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -30.310889132455344,
                37.499999999999986
            ],
            "rotationAngle": -0.5235987755982974
        },
        {
            "id": 124,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -34.64101615137754,
                44.99999999999998
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 125,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -30.310889132455348,
                52.499999999999986
            ],
            "rotationAngle": -2.617993877991494
        },
        {
            "id": 126,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -21.650635094610962,
                52.499999999999986
            ],
            "rotationAngle": 2.6179938779914944
        },
        {
            "id": 127,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -17.320508075688767,
                44.999999999999986
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 128,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -12.990381056766575,
                52.499999999999986
            ],
            "rotationAngle": -2.6179938779914944
        },
        {
            "id": 129,
            "owner": null,
            "color": null,
            "coordinates":
            [
                -4.330127018922189,
                52.499999999999986
            ],
            "rotationAngle": 2.6179938779914944
        },
        {
            "id": 130,
            "owner": null,
            "color": null,
            "coordinates":
            [
                3.552713678800501E-15,
                44.999999999999986
            ],
            "rotationAngle": -1.5707963267948966
        },
        {
            "id": 131,
            "owner": null,
            "color": null,
            "coordinates":
            [
                4.330127018922196,
                52.499999999999986
            ],
            "rotationAngle": -2.6179938779914944
        },
        {
            "id": 132,
            "owner": null,
            "color": null,
            "coordinates":
            [
                12.990381056766582,
                52.499999999999986
            ],
            "rotationAngle": -0.5235987755982989
        }
    ],
    "ringsOfBoard": 4,
    "sizeOfHex": 6
}
    """
    }
}