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
    private val buildingJson =
        """{"owner":"PlayerA","color":"Red","type":"Settlement"}"""
    private val settlementJsonWithBuilding =
        """{"id":2,"building": {"owner":"PlayerB","color":"Blue","type":"City"},"coordinates":[20.0,30.0]}"""

    private val portTransformJson = """{"x":10.5,"y":20.5,"rotation":1.57}"""
    private val portVisualsJson = """
        {
            "portTransform": {"x":10.5,"y":20.5,"rotation":1.57},
            "settlementPosition1Id":1,
            "settlementPosition2Id":2,
            "buildingSite1Position":[10.0, 20.0],
            "buildingSite2Position":[12.0, 22.0]
        }
    """
    private val generalPortJson = """
        {
            "inputResourceAmount":3,
            "portVisuals": {
                "portTransform": {"x":10.5,"y":20.5,"rotation":1.57},
                "settlementPosition1Id":1,
                "settlementPosition2Id":2,
                "buildingSite1Position":[10.0, 20.0],
                "buildingSite2Position":[12.0, 22.0]
            },
            "portType":"GeneralPort",
            "resource":null
        }
    """
    private val specificPortJson = """
        {
            "inputResourceAmount":2,
            "portVisuals": {
                "portTransform": {"x":12.5,"y":22.5,"rotation":0.78},
                "settlementPosition1Id":3,
                "settlementPosition2Id":4,
                "buildingSite1Position":[12.0, 22.0],
                "buildingSite2Position":[14.0, 24.0]
            },
            "portType":"SpecificResourcePort",
            "resource":"WOOD"
        }
    """


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
    fun deserializeSettlementPositionWithBuilding() {
        val sp = jsonParser.decodeFromString<SettlementPosition>(settlementJsonWithBuilding)
        assertEquals(2, sp.id)
        assertNotNull(sp.building)
        assertEquals("PlayerB", sp.building?.owner)
        assertEquals("Blue", sp.building?.color)
        assertEquals("City", sp.building?.type)
        assertEquals(20.0, sp.coordinates[0], 0.00001)
        assertEquals(30.0, sp.coordinates[1], 0.00001)
    }

    @Test
    fun deserializeRoadNullOwner() {
        val road = jsonParser.decodeFromString<Road>(roadJsonNullOwner)
        assertEquals(1, road.id)
        assertNull(road.owner, "Owner should be null")
    }

    @Test
    fun serializeRoadNullOwner() {
        val road = Road(
            id = 20,
            owner = null,
            coordinates = listOf(5.0, 5.0),
            rotationAngle = 0.0,
            color = "#000000"
        )
        val expectedJson =
            """{"id":20,"owner":null,"coordinates":[5.0,5.0],"rotationAngle":0.0,"color":"#000000"}"""
        val actualJson = jsonParser.encodeToString(road)
        assertEquals(expectedJson, actualJson)
    }

    @Test
    fun deserializeBuilding() {
        val building = jsonParser.decodeFromString<Building>(buildingJson)
        assertNotNull(building)
        assertEquals("PlayerA", building.owner)
        assertEquals("Red", building.color)
        assertEquals("Settlement", building.type)
    }

    @Test
    fun serializeBuilding() {
        val building = Building(owner = "PlayerC", color = "Green", type = "City")
        val expectedJson = """{"owner":"PlayerC","color":"Green","type":"City"}"""
        val actualJson = jsonParser.encodeToString(building)
        assertEquals(expectedJson, actualJson)
    }


    @Test
    fun deserializePortTransform() {
        val pt = jsonParser.decodeFromString<PortTransform>(portTransformJson)
        assertEquals(10.5, pt.x, 0.001)
        assertEquals(20.5, pt.y, 0.001)
        assertEquals(1.57, pt.rotation, 0.001)
    }

    @Test
    fun serializePortTransform() {
        val pt = PortTransform(x = 1.0, y = 2.0, rotation = 0.5)
        val expectedJson = """{"x":1.0,"y":2.0,"rotation":0.5}"""
        assertEquals(expectedJson, jsonParser.encodeToString(pt))
    }

    @Test
    fun deserializePortVisuals() {
        val pv = jsonParser.decodeFromString<PortVisuals>(portVisualsJson)
        assertNotNull(pv.portTransform)
        assertEquals(10.5, pv.portTransform.x, 0.001)
        assertEquals(1, pv.settlementPosition1Id)
        assertEquals(2, pv.settlementPosition2Id)
        assertEquals(10.0, pv.buildingSite1Position[0], 0.001)
        assertEquals(22.0, pv.buildingSite2Position[1], 0.001)
    }

    @Test
    fun deserializeGeneralPort() {
        val port = jsonParser.decodeFromString<Port>(generalPortJson)
        assertEquals(3, port.inputResourceAmount)
        assertEquals("GeneralPort", port.portType)
        assertNull(port.resource)
        assertNotNull(port.portVisuals)
        assertEquals(10.5, port.portVisuals.portTransform.x, 0.001)
    }

    @Test
    fun serializeGeneralPort() {
        val visuals = PortVisuals(
            portTransform = PortTransform(x=1.0, y=2.0, rotation=0.1),
            settlementPosition1Id = 10,
            settlementPosition2Id = 11,
            buildingSite1Position = listOf(1.1, 2.1),
            buildingSite2Position = listOf(1.2, 2.2)
        )
        val port = Port(inputResourceAmount = 3, portVisuals = visuals, portType = "GeneralPort", resource = null)
        val expectedJson = """{"inputResourceAmount":3,"portVisuals":{"portTransform":{"x":1.0,"y":2.0,"rotation":0.1},"settlementPosition1Id":10,"settlementPosition2Id":11,"buildingSite1Position":[1.1,2.1],"buildingSite2Position":[1.2,2.2]},"portType":"GeneralPort"}"""
        assertEquals(expectedJson, jsonParser.encodeToString(port))
    }


    @Test
    fun deserializeSpecificPort() {
        val port = jsonParser.decodeFromString<Port>(specificPortJson)
        assertEquals(2, port.inputResourceAmount)
        assertEquals("SpecificResourcePort", port.portType)
        assertEquals(TileType.WOOD, port.resource)
        assertNotNull(port.portVisuals)
        assertEquals(12.5, port.portVisuals.portTransform.x, 0.001)
    }

    @Test
    fun serializeSpecificPort() {
        val visuals = PortVisuals(
            portTransform = PortTransform(x=3.0, y=4.0, rotation=0.2),
            settlementPosition1Id = 12,
            settlementPosition2Id = 13,
            buildingSite1Position = listOf(3.1, 4.1),
            buildingSite2Position = listOf(3.2, 4.2)
        )
        val port = Port(inputResourceAmount = 2, portVisuals = visuals, portType = "SpecificResourcePort", resource = TileType.SHEEP)
        val expectedJson = """{"inputResourceAmount":2,"portVisuals":{"portTransform":{"x":3.0,"y":4.0,"rotation":0.2},"settlementPosition1Id":12,"settlementPosition2Id":13,"buildingSite1Position":[3.1,4.1],"buildingSite2Position":[3.2,4.2]},"portType":"SpecificResourcePort","resource":"SHEEP"}"""
        assertEquals(expectedJson, jsonParser.encodeToString(port))
    }

    @Test
    fun `deserialize full GameBoardModel from JSON`() {
        val board = try {
            jsonParser.decodeFromString<GameBoardModel>(fullTestBoardJson)
        } catch (e: Exception) {
            fail("Failed to parse full board JSON: ${e.message}")
        }

        assertNotNull(board, "Board should not be null")
        assertEquals(19, board.tiles.size, "Tile count mismatch")
        assertEquals(54, board.settlementPositions.size, "SettlementPosition count mismatch")
        assertEquals(72, board.roads.size, "Road count mismatch")
        assertEquals(9, board.ports.size, "Port count mismatch")
        assertEquals(3, board.ringsOfBoard, "Rings mismatch")
        assertEquals(6, board.sizeOfHex, "Hex size mismatch")

        assertTrue(board.tiles.isNotEmpty(), "Tiles list should not be empty")
        assertEquals(TileType.SHEEP, board.tiles[0].type)
        assertTrue(
            board.settlementPositions.all { it.building == null },
            "Building of settlements should be null"
        )
        assertTrue(board.roads.all { it.owner == null }, "Road owner should be null")
        assertEquals(3, board.ports[0].inputResourceAmount)
        assertEquals("GeneralPort", board.ports[0].portType)
        assertEquals(TileType.WHEAT, board.ports[1].resource)

    }


    companion object {
        const val fullTestBoardJson = """
            {
   "tiles":[
      {
         "id":1,
         "type":"SHEEP",
         "value":4,
         "coordinates":[
            0.0,
            0.0
         ]
      },
      {
         "id":2,
         "type":"SHEEP",
         "value":8,
         "coordinates":[
            8.660254037844387,
            14.999999999999996
         ]
      },
      {
         "id":3,
         "type":"WHEAT",
         "value":5,
         "coordinates":[
            17.32050807568877,
            0.0
         ]
      },
      {
         "id":4,
         "type":"ORE",
         "value":6,
         "coordinates":[
            8.660254037844382,
            -14.999999999999996
         ]
      },
      {
         "id":5,
         "type":"WHEAT",
         "value":4,
         "coordinates":[
            -8.660254037844389,
            -14.999999999999996
         ]
      },
      {
         "id":6,
         "type":"WOOD",
         "value":9,
         "coordinates":[
            -17.32050807568877,
            0.0
         ]
      },
      {
         "id":7,
         "type":"WOOD",
         "value":3,
         "coordinates":[
            -8.660254037844384,
            14.999999999999996
         ]
      },
      {
         "id":8,
         "type":"ORE",
         "value":11,
         "coordinates":[
            17.320508075688775,
            29.999999999999993
         ]
      },
      {
         "id":9,
         "type":"WOOD",
         "value":2,
         "coordinates":[
            25.98076211353316,
            14.999999999999996
         ]
      },
      {
         "id":10,
         "type":"WHEAT",
         "value":12,
         "coordinates":[
            34.64101615137754,
            0.0
         ]
      },
      {
         "id":11,
         "type":"CLAY",
         "value":10,
         "coordinates":[
            25.980762113533153,
            -14.999999999999996
         ]
      },
      {
         "id":12,
         "type":"WOOD",
         "value":11,
         "coordinates":[
            17.320508075688764,
            -29.999999999999993
         ]
      },
      {
         "id":13,
         "type":"CLAY",
         "value":10,
         "coordinates":[
            -7.105427357601002E-15,
            -29.999999999999993
         ]
      },
      {
         "id":14,
         "type":"SHEEP",
         "value":5,
         "coordinates":[
            -17.320508075688778,
            -29.999999999999993
         ]
      },
      {
         "id":15,
         "type":"ORE",
         "value":8,
         "coordinates":[
            -25.98076211353316,
            -14.999999999999996
         ]
      },
      {
         "id":16,
         "type":"SHEEP",
         "value":12,
         "coordinates":[
            -34.64101615137754,
            0.0
         ]
      },
      {
         "id":17,
         "type":"WASTE",
         "value":0,
         "coordinates":[
            -25.980762113533153,
            14.999999999999996
         ]
      },
      {
         "id":18,
         "type":"CLAY",
         "value":6,
         "coordinates":[
            -17.320508075688767,
            29.999999999999993
         ]
      },
      {
         "id":19,
         "type":"WHEAT",
         "value":3,
         "coordinates":[
            3.552713678800501E-15,
            29.999999999999993
         ]
      }
   ],
   "settlementPositions":[
      {
         "id":1,
         "building":null,
         "coordinates":[
            1.1842378929335002E-15,
            9.999999999999998
         ]
      },
      {
         "id":2,
         "building":null,
         "coordinates":[
            8.660254037844387,
            4.999999999999999
         ]
      },
      {
         "id":3,
         "building":null,
         "coordinates":[
            8.660254037844384,
            -4.999999999999999
         ]
      },
      {
         "id":4,
         "building":null,
         "coordinates":[
            -2.3684757858670005E-15,
            -9.999999999999998
         ]
      },
      {
         "id":5,
         "building":null,
         "coordinates":[
            -8.660254037844387,
            -4.999999999999999
         ]
      },
      {
         "id":6,
         "building":null,
         "coordinates":[
            -8.660254037844384,
            4.999999999999999
         ]
      },
      {
         "id":7,
         "building":null,
         "coordinates":[
            2.3684757858670005E-15,
            19.999999999999996
         ]
      },
      {
         "id":8,
         "building":null,
         "coordinates":[
            8.660254037844387,
            24.99999999999999
         ]
      },
      {
         "id":9,
         "building":null,
         "coordinates":[
            17.320508075688775,
            19.999999999999996
         ]
      },
      {
         "id":10,
         "building":null,
         "coordinates":[
            17.320508075688775,
            9.999999999999998
         ]
      },
      {
         "id":11,
         "building":null,
         "coordinates":[
            25.98076211353316,
            4.999999999999999
         ]
      },
      {
         "id":12,
         "building":null,
         "coordinates":[
            25.980762113533157,
            -4.999999999999999
         ]
      },
      {
         "id":13,
         "building":null,
         "coordinates":[
            17.320508075688767,
            -9.999999999999998
         ]
      },
      {
         "id":14,
         "building":null,
         "coordinates":[
            17.320508075688767,
            -19.999999999999996
         ]
      },
      {
         "id":15,
         "building":null,
         "coordinates":[
            8.66025403784438,
            -24.99999999999999
         ]
      },
      {
         "id":16,
         "building":null,
         "coordinates":[
            -4.736951571734001E-15,
            -19.999999999999996
         ]
      },
      {
         "id":17,
         "building":null,
         "coordinates":[
            -8.66025403784439,
            -24.99999999999999
         ]
      },
      {
         "id":18,
         "building":null,
         "coordinates":[
            -17.320508075688775,
            -19.999999999999996
         ]
      },
      {
         "id":19,
         "building":null,
         "coordinates":[
            -17.320508075688775,
            -9.999999999999998
         ]
      },
      {
         "id":20,
         "building":null,
         "coordinates":[
            -25.98076211353316,
            -4.999999999999999
         ]
      },
      {
         "id":21,
         "building":null,
         "coordinates":[
            -25.980762113533157,
            4.999999999999999
         ]
      },
      {
         "id":22,
         "building":null,
         "coordinates":[
            -17.320508075688767,
            9.999999999999998
         ]
      },
      {
         "id":23,
         "building":null,
         "coordinates":[
            -17.320508075688767,
            19.999999999999996
         ]
      },
      {
         "id":24,
         "building":null,
         "coordinates":[
            -8.660254037844384,
            24.99999999999999
         ]
      },
      {
         "id":25,
         "building":null,
         "coordinates":[
            8.66025403784439,
            35.0
         ]
      },
      {
         "id":26,
         "building":null,
         "coordinates":[
            17.320508075688778,
            40.000000000000014
         ]
      },
      {
         "id":27,
         "building":null,
         "coordinates":[
            25.980762113533164,
            35.00000000000001
         ]
      },
      {
         "id":28,
         "building":null,
         "coordinates":[
            25.980762113533164,
            24.99999999999999
         ]
      },
      {
         "id":29,
         "building":null,
         "coordinates":[
            34.64101615137754,
            19.99999999999999
         ]
      },
      {
         "id":30,
         "building":null,
         "coordinates":[
            34.64101615137754,
            9.999999999999996
         ]
      },
      {
         "id":31,
         "building":null,
         "coordinates":[
            43.301270189221924,
            4.999999999999997
         ]
      },
      {
         "id":32,
         "building":null,
         "coordinates":[
            43.301270189221924,
            -4.999999999999999
         ]
      },
      {
         "id":33,
         "building":null,
         "coordinates":[
            34.64101615137754,
            -9.999999999999996
         ]
      },
      {
         "id":34,
         "building":null,
         "coordinates":[
            34.641016151377535,
            -19.99999999999999
         ]
      },
      {
         "id":35,
         "building":null,
         "coordinates":[
            25.98076211353315,
            -24.99999999999999
         ]
      },
      {
         "id":36,
         "building":null,
         "coordinates":[
            25.980762113533142,
            -34.999999999999986
         ]
      },
      {
         "id":37,
         "building":null,
         "coordinates":[
            17.320508075688757,
            -39.99999999999999
         ]
      },
      {
         "id":38,
         "building":null,
         "coordinates":[
            8.660254037844377,
            -35.0
         ]
      },
      {
         "id":39,
         "building":null,
         "coordinates":[
            -1.0658141036401503E-14,
            -40.00000000000001
         ]
      },
      {
         "id":40,
         "building":null,
         "coordinates":[
            -8.660254037844394,
            -35.0
         ]
      },
      {
         "id":41,
         "building":null,
         "coordinates":[
            -17.320508075688785,
            -40.000000000000014
         ]
      },
      {
         "id":42,
         "building":null,
         "coordinates":[
            -25.980762113533174,
            -35.00000000000001
         ]
      },
      {
         "id":43,
         "building":null,
         "coordinates":[
            -25.980762113533164,
            -24.99999999999999
         ]
      },
      {
         "id":44,
         "building":null,
         "coordinates":[
            -34.64101615137754,
            -19.99999999999999
         ]
      },
      {
         "id":45,
         "building":null,
         "coordinates":[
            -34.64101615137754,
            -9.999999999999996
         ]
      },
      {
         "id":46,
         "building":null,
         "coordinates":[
            -43.301270189221924,
            -4.999999999999997
         ]
      },
      {
         "id":47,
         "building":null,
         "coordinates":[
            -43.301270189221924,
            4.999999999999999
         ]
      },
      {
         "id":48,
         "building":null,
         "coordinates":[
            -34.64101615137754,
            9.999999999999996
         ]
      },
      {
         "id":49,
         "building":null,
         "coordinates":[
            -34.64101615137755,
            19.99999999999999
         ]
      },
      {
         "id":50,
         "building":null,
         "coordinates":[
            -25.980762113533157,
            24.99999999999999
         ]
      },
      {
         "id":51,
         "building":null,
         "coordinates":[
            -25.980762113533157,
            34.999999999999986
         ]
      },
      {
         "id":52,
         "building":null,
         "coordinates":[
            -17.320508075688767,
            39.99999999999999
         ]
      },
      {
         "id":53,
         "building":null,
         "coordinates":[
            -8.66025403784438,
            35.0
         ]
      },
      {
         "id":54,
         "building":null,
         "coordinates":[
            7.105427357601002E-15,
            40.00000000000001
         ]
      }
   ],
   "roads":[
      {
         "id":1,
         "owner":null,
         "color":null,
         "coordinates":[
            4.3301270189221945,
            7.499999999999998
         ],
         "rotationAngle":2.6179938779914944
      },
      {
         "id":2,
         "owner":null,
         "color":null,
         "coordinates":[
            8.660254037844386,
            0.0
         ],
         "rotationAngle":1.5707963267948963
      },
      {
         "id":3,
         "owner":null,
         "color":null,
         "coordinates":[
            4.330127018922191,
            -7.499999999999998
         ],
         "rotationAngle":0.5235987755982988
      },
      {
         "id":4,
         "owner":null,
         "color":null,
         "coordinates":[
            -4.3301270189221945,
            -7.499999999999998
         ],
         "rotationAngle":-0.5235987755982988
      },
      {
         "id":5,
         "owner":null,
         "color":null,
         "coordinates":[
            -8.660254037844386,
            0.0
         ],
         "rotationAngle":-1.570796326794897
      },
      {
         "id":6,
         "owner":null,
         "color":null,
         "coordinates":[
            -4.330127018922191,
            7.499999999999998
         ],
         "rotationAngle":-2.6179938779914944
      },
      {
         "id":7,
         "owner":null,
         "color":null,
         "coordinates":[
            1.7763568394002505E-15,
            14.999999999999996
         ],
         "rotationAngle":-1.5707963267948968
      },
      {
         "id":8,
         "owner":null,
         "color":null,
         "coordinates":[
            4.3301270189221945,
            22.499999999999993
         ],
         "rotationAngle":-2.617993877991495
      },
      {
         "id":9,
         "owner":null,
         "color":null,
         "coordinates":[
            12.99038105676658,
            22.499999999999993
         ],
         "rotationAngle":2.617993877991495
      },
      {
         "id":10,
         "owner":null,
         "color":null,
         "coordinates":[
            17.320508075688775,
            14.999999999999996
         ],
         "rotationAngle":1.5707963267948966
      },
      {
         "id":11,
         "owner":null,
         "color":null,
         "coordinates":[
            12.99038105676658,
            7.499999999999998
         ],
         "rotationAngle":-2.6179938779914944
      },
      {
         "id":12,
         "owner":null,
         "color":null,
         "coordinates":[
            21.65063509461097,
            7.499999999999998
         ],
         "rotationAngle":2.6179938779914944
      },
      {
         "id":13,
         "owner":null,
         "color":null,
         "coordinates":[
            25.98076211353316,
            0.0
         ],
         "rotationAngle":1.5707963267948963
      },
      {
         "id":14,
         "owner":null,
         "color":null,
         "coordinates":[
            21.650635094610962,
            -7.499999999999998
         ],
         "rotationAngle":0.5235987755982987
      },
      {
         "id":15,
         "owner":null,
         "color":null,
         "coordinates":[
            12.990381056766577,
            -7.499999999999998
         ],
         "rotationAngle":2.6179938779914944
      },
      {
         "id":16,
         "owner":null,
         "color":null,
         "coordinates":[
            17.320508075688767,
            -14.999999999999996
         ],
         "rotationAngle":1.5707963267948966
      },
      {
         "id":17,
         "owner":null,
         "color":null,
         "coordinates":[
            12.990381056766573,
            -22.499999999999993
         ],
         "rotationAngle":0.5235987755982981
      },
      {
         "id":18,
         "owner":null,
         "color":null,
         "coordinates":[
            4.330127018922187,
            -22.499999999999993
         ],
         "rotationAngle":-0.5235987755982983
      },
      {
         "id":19,
         "owner":null,
         "color":null,
         "coordinates":[
            -3.552713678800501E-15,
            -14.999999999999996
         ],
         "rotationAngle":1.5707963267948963
      },
      {
         "id":20,
         "owner":null,
         "color":null,
         "coordinates":[
            -4.330127018922198,
            -22.499999999999993
         ],
         "rotationAngle":0.5235987755982983
      },
      {
         "id":21,
         "owner":null,
         "color":null,
         "coordinates":[
            -12.990381056766584,
            -22.499999999999993
         ],
         "rotationAngle":-0.5235987755982985
      },
      {
         "id":22,
         "owner":null,
         "color":null,
         "coordinates":[
            -17.320508075688775,
            -14.999999999999996
         ],
         "rotationAngle":-1.5707963267948966
      },
      {
         "id":23,
         "owner":null,
         "color":null,
         "coordinates":[
            -12.99038105676658,
            -7.499999999999998
         ],
         "rotationAngle":0.5235987755982987
      },
      {
         "id":24,
         "owner":null,
         "color":null,
         "coordinates":[
            -21.65063509461097,
            -7.499999999999998
         ],
         "rotationAngle":-0.5235987755982988
      },
      {
         "id":25,
         "owner":null,
         "color":null,
         "coordinates":[
            -25.98076211353316,
            0.0
         ],
         "rotationAngle":-1.570796326794897
      },
      {
         "id":26,
         "owner":null,
         "color":null,
         "coordinates":[
            -21.650635094610962,
            7.499999999999998
         ],
         "rotationAngle":-2.6179938779914944
      },
      {
         "id":27,
         "owner":null,
         "color":null,
         "coordinates":[
            -12.990381056766577,
            7.499999999999998
         ],
         "rotationAngle":-0.5235987755982989
      },
      {
         "id":28,
         "owner":null,
         "color":null,
         "coordinates":[
            -17.320508075688767,
            14.999999999999996
         ],
         "rotationAngle":-1.5707963267948966
      },
      {
         "id":29,
         "owner":null,
         "color":null,
         "coordinates":[
            -12.990381056766577,
            22.499999999999993
         ],
         "rotationAngle":-2.617993877991495
      },
      {
         "id":30,
         "owner":null,
         "color":null,
         "coordinates":[
            -4.330127018922191,
            22.499999999999993
         ],
         "rotationAngle":-0.5235987755982983
      },
      {
         "id":31,
         "owner":null,
         "color":null,
         "coordinates":[
            8.660254037844389,
            29.999999999999993
         ],
         "rotationAngle":-1.570796326794897
      },
      {
         "id":32,
         "owner":null,
         "color":null,
         "coordinates":[
            12.990381056766584,
            37.50000000000001
         ],
         "rotationAngle":-2.617993877991493
      },
      {
         "id":33,
         "owner":null,
         "color":null,
         "coordinates":[
            21.65063509461097,
            37.500000000000014
         ],
         "rotationAngle":2.617993877991494
      },
      {
         "id":34,
         "owner":null,
         "color":null,
         "coordinates":[
            25.980762113533164,
            30.0
         ],
         "rotationAngle":1.5707963267948966
      },
      {
         "id":35,
         "owner":null,
         "color":null,
         "coordinates":[
            21.65063509461097,
            22.499999999999993
         ],
         "rotationAngle":-2.617993877991495
      },
      {
         "id":36,
         "owner":null,
         "color":null,
         "coordinates":[
            30.31088913245535,
            22.49999999999999
         ],
         "rotationAngle":2.617993877991494
      },
      {
         "id":37,
         "owner":null,
         "color":null,
         "coordinates":[
            34.64101615137754,
            14.999999999999993
         ],
         "rotationAngle":1.5707963267948966
      },
      {
         "id":38,
         "owner":null,
         "color":null,
         "coordinates":[
            30.31088913245535,
            7.499999999999998
         ],
         "rotationAngle":-2.6179938779914944
      },
      {
         "id":39,
         "owner":null,
         "color":null,
         "coordinates":[
            38.97114317029973,
            7.4999999999999964
         ],
         "rotationAngle":2.617993877991494
      },
      {
         "id":40,
         "owner":null,
         "color":null,
         "coordinates":[
            43.301270189221924,
            -8.881784197001252E-16
         ],
         "rotationAngle":1.5707963267948966
      },
      {
         "id":41,
         "owner":null,
         "color":null,
         "coordinates":[
            38.97114317029973,
            -7.499999999999998
         ],
         "rotationAngle":0.5235987755982988
      },
      {
         "id":42,
         "owner":null,
         "color":null,
         "coordinates":[
            30.31088913245535,
            -7.499999999999998
         ],
         "rotationAngle":2.6179938779914944
      },
      {
         "id":43,
         "owner":null,
         "color":null,
         "coordinates":[
            34.64101615137754,
            -14.999999999999993
         ],
         "rotationAngle":1.570796326794896
      },
      {
         "id":44,
         "owner":null,
         "color":null,
         "coordinates":[
            30.310889132455344,
            -22.49999999999999
         ],
         "rotationAngle":0.5235987755982989
      },
      {
         "id":45,
         "owner":null,
         "color":null,
         "coordinates":[
            21.65063509461096,
            -22.499999999999993
         ],
         "rotationAngle":2.617993877991495
      },
      {
         "id":46,
         "owner":null,
         "color":null,
         "coordinates":[
            25.980762113533146,
            -29.999999999999986
         ],
         "rotationAngle":1.570796326794896
      },
      {
         "id":47,
         "owner":null,
         "color":null,
         "coordinates":[
            21.650635094610948,
            -37.499999999999986
         ],
         "rotationAngle":0.5235987755982995
      },
      {
         "id":48,
         "owner":null,
         "color":null,
         "coordinates":[
            12.990381056766566,
            -37.5
         ],
         "rotationAngle":-0.5235987755982986
      },
      {
         "id":49,
         "owner":null,
         "color":null,
         "coordinates":[
            8.660254037844378,
            -29.999999999999993
         ],
         "rotationAngle":1.5707963267948963
      },
      {
         "id":50,
         "owner":null,
         "color":null,
         "coordinates":[
            4.330127018922183,
            -37.5
         ],
         "rotationAngle":0.5235987755982994
      },
      {
         "id":51,
         "owner":null,
         "color":null,
         "coordinates":[
            -4.3301270189222025,
            -37.5
         ],
         "rotationAngle":-0.5235987755982996
      },
      {
         "id":52,
         "owner":null,
         "color":null,
         "coordinates":[
            -8.660254037844393,
            -29.999999999999993
         ],
         "rotationAngle":1.5707963267948963
      },
      {
         "id":53,
         "owner":null,
         "color":null,
         "coordinates":[
            -12.99038105676659,
            -37.50000000000001
         ],
         "rotationAngle":0.5235987755982998
      },
      {
         "id":54,
         "owner":null,
         "color":null,
         "coordinates":[
            -21.65063509461098,
            -37.500000000000014
         ],
         "rotationAngle":-0.5235987755982994
      },
      {
         "id":55,
         "owner":null,
         "color":null,
         "coordinates":[
            -25.980762113533167,
            -30.0
         ],
         "rotationAngle":-1.570796326794898
      },
      {
         "id":56,
         "owner":null,
         "color":null,
         "coordinates":[
            -21.65063509461097,
            -22.499999999999993
         ],
         "rotationAngle":0.5235987755982981
      },
      {
         "id":57,
         "owner":null,
         "color":null,
         "coordinates":[
            -30.31088913245535,
            -22.49999999999999
         ],
         "rotationAngle":-0.5235987755982993
      },
      {
         "id":58,
         "owner":null,
         "color":null,
         "coordinates":[
            -34.64101615137754,
            -14.999999999999993
         ],
         "rotationAngle":-1.5707963267948966
      },
      {
         "id":59,
         "owner":null,
         "color":null,
         "coordinates":[
            -30.31088913245535,
            -7.499999999999998
         ],
         "rotationAngle":0.5235987755982988
      },
      {
         "id":60,
         "owner":null,
         "color":null,
         "coordinates":[
            -38.97114317029973,
            -7.4999999999999964
         ],
         "rotationAngle":-0.523598775598299
      },
      {
         "id":61,
         "owner":null,
         "color":null,
         "coordinates":[
            -43.301270189221924,
            8.881784197001252E-16
         ],
         "rotationAngle":-1.5707963267948966
      },
      {
         "id":62,
         "owner":null,
         "color":null,
         "coordinates":[
            -38.97114317029973,
            7.499999999999998
         ],
         "rotationAngle":-2.6179938779914944
      },
      {
         "id":63,
         "owner":null,
         "color":null,
         "coordinates":[
            -30.31088913245535,
            7.499999999999998
         ],
         "rotationAngle":-0.5235987755982987
      },
      {
         "id":64,
         "owner":null,
         "color":null,
         "coordinates":[
            -34.64101615137754,
            14.999999999999993
         ],
         "rotationAngle":-1.570796326794896
      },
      {
         "id":65,
         "owner":null,
         "color":null,
         "coordinates":[
            -30.31088913245535,
            22.49999999999999
         ],
         "rotationAngle":-2.617993877991495
      },
      {
         "id":66,
         "owner":null,
         "color":null,
         "coordinates":[
            -21.650635094610962,
            22.499999999999993
         ],
         "rotationAngle":-0.5235987755982981
      },
      {
         "id":67,
         "owner":null,
         "color":null,
         "coordinates":[
            -25.980762113533157,
            29.999999999999986
         ],
         "rotationAngle":-1.5707963267948966
      },
      {
         "id":68,
         "owner":null,
         "color":null,
         "coordinates":[
            -21.650635094610962,
            37.499999999999986
         ],
         "rotationAngle":-2.617993877991494
      },
      {
         "id":69,
         "owner":null,
         "color":null,
         "coordinates":[
            -12.990381056766573,
            37.5
         ],
         "rotationAngle":2.617993877991495
      },
      {
         "id":70,
         "owner":null,
         "color":null,
         "coordinates":[
            -8.660254037844382,
            29.999999999999993
         ],
         "rotationAngle":-1.570796326794897
      },
      {
         "id":71,
         "owner":null,
         "color":null,
         "coordinates":[
            -4.3301270189221865,
            37.5
         ],
         "rotationAngle":-2.617993877991494
      },
      {
         "id":72,
         "owner":null,
         "color":null,
         "coordinates":[
            4.330127018922199,
            37.5
         ],
         "rotationAngle":-0.5235987755982996
      }
   ],
   "ports":[
      {
         "inputResourceAmount":3,
         "portVisuals":{
            "portTransform":{
               "x":7.990381056766573,
               "y":46.16025403784439,
               "rotation":0.5235987755983001
            },
            "settlementPosition1Id":25,
            "settlementPosition2Id":26,
            "buildingSite1Position":[
               8.66025403784439,
               35.0
            ],
            "buildingSite2Position":[
               17.320508075688778,
               40.000000000000014
            ]
         },
         "portType":"GeneralPort"
      },
      {
         "inputResourceAmount":2,
         "portVisuals":{
            "portTransform":{
               "x":35.31088913245536,
               "y":31.160254037844375,
               "rotation":-0.5235987755982993
            },
            "settlementPosition1Id":28,
            "settlementPosition2Id":29,
            "buildingSite1Position":[
               25.980762113533164,
               24.99999999999999
            ],
            "buildingSite2Position":[
               34.64101615137754,
               19.99999999999999
            ]
         },
         "portType":"SpecificResourcePort",
         "resource":"WHEAT"
      },
      {
         "inputResourceAmount":3,
         "portVisuals":{
            "portTransform":{
               "x":53.301270189221924,
               "y":-8.881784197001252E-16,
               "rotation":-1.5707963267948966
            },
            "settlementPosition1Id":31,
            "settlementPosition2Id":32,
            "buildingSite1Position":[
               43.301270189221924,
               4.999999999999997
            ],
            "buildingSite2Position":[
               43.301270189221924,
               -4.999999999999999
            ]
         },
         "portType":"GeneralPort"
      },
      {
         "inputResourceAmount":2,
         "portVisuals":{
            "portTransform":{
               "x":35.310889132455344,
               "y":-31.160254037844375,
               "rotation":-2.6179938779914944
            },
            "settlementPosition1Id":34,
            "settlementPosition2Id":35,
            "buildingSite1Position":[
               34.641016151377535,
               -19.99999999999999
            ],
            "buildingSite2Position":[
               25.98076211353315,
               -24.99999999999999
            ]
         },
         "portType":"SpecificResourcePort",
         "resource":"WOOD"
      },
      {
         "inputResourceAmount":3,
         "portVisuals":{
            "portTransform":{
               "x":7.9903810567665685,
               "y":-46.16025403784439,
               "rotation":2.617993877991495
            },
            "settlementPosition1Id":37,
            "settlementPosition2Id":38,
            "buildingSite1Position":[
               17.320508075688757,
               -39.99999999999999
            ],
            "buildingSite2Position":[
               8.660254037844377,
               -35.0
            ]
         },
         "portType":"GeneralPort"
      },
      {
         "inputResourceAmount":3,
         "portVisuals":{
            "portTransform":{
               "x":-7.990381056766582,
               "y":-46.16025403784439,
               "rotation":-2.6179938779914935
            },
            "settlementPosition1Id":40,
            "settlementPosition2Id":41,
            "buildingSite1Position":[
               -8.660254037844394,
               -35.0
            ],
            "buildingSite2Position":[
               -17.320508075688785,
               -40.000000000000014
            ]
         },
         "portType":"GeneralPort"
      },
      {
         "inputResourceAmount":2,
         "portVisuals":{
            "portTransform":{
               "x":-35.31088913245536,
               "y":-31.160254037844375,
               "rotation":2.617993877991494
            },
            "settlementPosition1Id":43,
            "settlementPosition2Id":44,
            "buildingSite1Position":[
               -25.980762113533164,
               -24.99999999999999
            ],
            "buildingSite2Position":[
               -34.64101615137754,
               -19.99999999999999
            ]
         },
         "portType":"SpecificResourcePort",
         "resource":"CLAY"
      },
      {
         "inputResourceAmount":2,
         "portVisuals":{
            "portTransform":{
               "x":-53.301270189221924,
               "y":8.881784197001252E-16,
               "rotation":1.5707963267948966
            },
            "settlementPosition1Id":46,
            "settlementPosition2Id":47,
            "buildingSite1Position":[
               -43.301270189221924,
               -4.999999999999997
            ],
            "buildingSite2Position":[
               -43.301270189221924,
               4.999999999999999
            ]
         },
         "portType":"SpecificResourcePort",
         "resource":"SHEEP"
      },
      {
         "inputResourceAmount":2,
         "portVisuals":{
            "portTransform":{
               "x":-35.31088913245535,
               "y":31.16025403784438,
               "rotation":0.5235987755982986
            },
            "settlementPosition1Id":49,
            "settlementPosition2Id":50,
            "buildingSite1Position":[
               -34.64101615137755,
               19.99999999999999
            ],
            "buildingSite2Position":[
               -25.980762113533157,
               24.99999999999999
            ]
         },
         "portType":"SpecificResourcePort",
         "resource":"ORE"
      }
   ],
   "ringsOfBoard":3,
   "sizeOfHex":6
}
    """
    }
}