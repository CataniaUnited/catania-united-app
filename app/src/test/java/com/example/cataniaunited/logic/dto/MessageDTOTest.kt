package com.example.cataniaunited.logic.dto

import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MessageDTOTest {

    @Test
    fun testToString(){
        val message = buildJsonObject {
            put("attribute1", "attr1")
            put("number", 3)
            putJsonArray("numbers", {
                add(1)
                add(2)
            })
        }
        val messageDTO = MessageDTO(MessageType.CREATE_LOBBY, "player1", "lobby1", listOf("player1", "player2"), message)
        val expected = """{"type":"CREATE_LOBBY","player":"player1","lobbyId":"lobby1","players":["player1","player2"],"message":{"attribute1":"attr1","number":3,"numbers":[1,2]}}"""
        val actual: String = messageDTO.toString()
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun testFromJson(){
        val json: String = """
            {
                "type": "CONNECTION_SUCCESSFUL",
                "player": "player1",
                "lobbyId": "lobby1",
                "players": ["player1", "player2"],
                 "message": {
                    "obj1": {
                        "number": 1,
                        "username": "TestUser"
                    },
                    "array1": [1,2,3,4]
                 }
             }
        """.trimIndent()

        val messageDTO = MessageDTO.fromJson(json)

        val expectedMessage = buildJsonObject {
            putJsonObject("obj1", {
                put("number", 1)
                put("username", "TestUser")
            })
            putJsonArray("array1", {
                add(1)
                add(2)
                add(3)
                add(4)
            })
        }

        Assertions.assertEquals(MessageType.CONNECTION_SUCCESSFUL, messageDTO.type)
        Assertions.assertEquals("player1", messageDTO.player)
        Assertions.assertEquals("lobby1", messageDTO.lobbyId)
        messageDTO.players?.let { Assertions.assertEquals(2, it.size) }
        Assertions.assertEquals("player1", messageDTO.players?.get(0))
        Assertions.assertEquals("player2", messageDTO.players?.get(1))
        Assertions.assertEquals(expectedMessage, messageDTO.message)
    }

    @Test
    fun fromJsonShouldNotThrowErrorOnUnknownKeys(){
        val json: String = """
            {
                "type": "CONNECTION_SUCCESSFUL",
                "player": "player4",
                "lobbyId": "lobby15",
                "unkownKey1": {
                    "test": "test"
                },
                "abc": "abc"
             }
        """.trimIndent()

        val messageDTO = MessageDTO.fromJson(json)
        Assertions.assertEquals(MessageType.CONNECTION_SUCCESSFUL, messageDTO.type)
        Assertions.assertEquals("player4", messageDTO.player)
        Assertions.assertEquals("lobby15", messageDTO.lobbyId)
    }

}