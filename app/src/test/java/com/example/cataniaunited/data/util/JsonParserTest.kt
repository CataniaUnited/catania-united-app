package com.example.cataniaunited.data.util

import com.example.cataniaunited.data.model.DataModelSerializationTest
import com.example.cataniaunited.data.model.TileType

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class JsonParserTest {
    private val validFullBoardJson = DataModelSerializationTest.fullTestBoardJson

    private val invalidJson_Malformed =
        """{"tiles": [ { "id":1, "type":"ORE" ]}"""
    private val invalidJson_WrongType = """{"tiles": "not a list"}"""
    private val jsonWithUnknownKey = """
    {
       "tiles":[ { "id":1, "type":"ORE", "value":8, "coordinates":[ 0.0, 0.0 ] } ],
       "settlementPositions":[],
       "roads":[],
       "ports": [],
       "ringsOfBoard":3,
       "sizeOfHex":6,
       "anExtraUnknownField": "some value"
    }
    """


    @Test
    fun parseGameBoardValidJSONReturnsGameBoardModel() {
        val result = parseGameBoard(validFullBoardJson)

        assertNotNull(result, "Result should not be null for valid JSON")
        assertEquals(3, result?.ringsOfBoard)
        assertEquals(6, result?.sizeOfHex)
        assertTrue(result?.tiles?.isNotEmpty() ?: false)
        assertEquals(TileType.SHEEP, result?.tiles?.get(0)?.type)
        assertNotNull(result?.ports)
        assertEquals(9, result?.ports?.size)
    }

    @Test
    fun parseGameBoardMalformedJSONReturnsNull() {
        val result = parseGameBoard(invalidJson_Malformed)
        assertNull(result, "Result should be null for malformed JSON")
    }

    @Test
    fun parseGameBoardWrongDataTypeJSONReturnsNull() {
        val result = parseGameBoard(invalidJson_WrongType)
        assertNull(result, "Result should be null for wrong data type JSON")
    }


    @Test
    fun parseGameBoardEmptyJSONReturnsNull() {
        val result = parseGameBoard("")
        assertNull(result, "Result should be null for empty JSON")
    }

    @Test
    fun parseGameBoardJSONLiteralNullReturnsNull() {
        val result = parseGameBoard("null")
        assertNull(result, "Result should be null for JSON 'null'")
    }
}