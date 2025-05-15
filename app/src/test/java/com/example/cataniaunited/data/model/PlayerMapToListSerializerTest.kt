package com.example.cataniaunited.data.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlayerMapToListSerializerTest {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `deserialize should convert map to list with playerId set from map key`() {
        val json = """
            {
                "player123": {
                    "username": "Alice",
                    "colorHex": "#FF0000",
                    "victoryPoints": 3
                },
                "player456": {
                    "username": "Bob",
                    "colorHex": "#0000FF",
                    "victoryPoints": 5
                }
            }
        """.trimIndent()

        val result = jsonParser.decodeFromString(PlayerMapToListSerializer, json)

        assertEquals(2, result!!.size)
        assertEquals("player123", result[0].playerId)
        assertEquals("Alice", result[0].username)
        assertEquals("#FF0000", result[0].colorHex)
        assertEquals(3, result[0].victoryPoints)

        assertEquals("player456", result[1].playerId)
        assertEquals("Bob", result[1].username)
        assertEquals("#0000FF", result[1].colorHex)
        assertEquals(5, result[1].victoryPoints)
    }

    @Test
    fun `deserialize should handle default values when fields are missing`() {
        val json = """
            {
                "player789": {
                    "username": "Charlie"
                }
            }
        """.trimIndent()

        val result = jsonParser.decodeFromString(PlayerMapToListSerializer, json)

        assertEquals(1, result!!.size)
        assertEquals("player789", result[0].playerId)
        assertEquals("Charlie", result[0].username)
        assertEquals("#8C4E27", result[0].colorHex) // default value
        assertEquals(0, result[0].victoryPoints) // default value
    }


    @Test
    fun `deserialize should return empty list for empty object`() {
        val json = "{}"
        val result = jsonParser.decodeFromString(PlayerMapToListSerializer, json)
        assertEquals(emptyList<PlayerInfo>(), result)
    }
}
