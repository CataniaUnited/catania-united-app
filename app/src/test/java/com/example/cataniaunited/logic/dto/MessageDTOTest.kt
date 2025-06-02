package com.example.cataniaunited.logic.dto

import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.data.model.TileType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MessageDTOTest {

    @Test
    fun testToString() {
        val player1Info = PlayerInfo(
            id = "player1",
            username = "Alice",
            color = "#RRGGBB",
            isHost = true,
            isReady = true,
            victoryPoints = 5,
            resources = mapOf(TileType.ORE to 2, TileType.WOOD to 1)
        )
        val player2Info = PlayerInfo(
            id = "player2",
            username = "Bob",
            color = "#AABBCC",
            isHost = false,
            isReady = false,
            victoryPoints = 3,
            resources = mapOf(TileType.SHEEP to 1, TileType.CLAY to 3)
        )

        val message = buildJsonObject {
            put("attribute1", "attr1")
            put("number", 3)
            putJsonArray("numbers") {
                add(1)
                add(2)
            }
        }

        val playersMap = mapOf("player1" to player1Info, "player2" to player2Info)
        val messageDTO =
            MessageDTO(MessageType.CREATE_LOBBY, "player1", "lobby1", playersMap, message)

        val expected =
            """{"type":"CREATE_LOBBY","player":"player1","lobbyId":"lobby1","players":{"player1":{"id":"player1","username":"Alice","color":"#RRGGBB","isHost":true,"isReady":true,"victoryPoints":5,"resources":{"ORE":2,"WOOD":1}},"player2":{"id":"player2","username":"Bob","color":"#AABBCC","isHost":false,"isReady":false,"victoryPoints":3,"resources":{"SHEEP":1,"CLAY":3}}},"message":{"attribute1":"attr1","number":3,"numbers":[1,2]}}"""
        val actual: String = messageDTO.toString()

        val expectedJsonElement = MessageDTO.fromJson(expected)
        val actualJsonElement = MessageDTO.fromJson(actual)
        Assertions.assertEquals(expectedJsonElement, actualJsonElement)
    }

    @Test
    fun testFromJson() {
        val json: String = """
            {
                "type": "CONNECTION_SUCCESSFUL",
                "player": "player1",
                "lobbyId": "lobby1",
                "players": {
                    "player1Id": {
                        "id": "player1Id",
                        "username": "TestUser1",
                        "color": "#ABCDEF",
                        "isHost": true,
                        "isReady": true,
                        "victoryPoints": 5,
                        "resources": {"ORE": 2, "WHEAT": 5}
                    },
                    "player2Id": {
                        "id": "player2Id",
                        "username": "TestUser2",
                        "color": "#FEDCBA",
                        "isHost": false,
                        "isReady": false,
                        "victoryPoints": 3,
                        "resources": {"WOOD": 1, "CLAY": 3}
                    }
                },
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
            putJsonObject("obj1") {
                put("number", 1)
                put("username", "TestUser")
            }
            putJsonArray("array1") {
                add(1)
                add(2)
                add(3)
                add(4)
            }
        }

        Assertions.assertEquals(MessageType.CONNECTION_SUCCESSFUL, messageDTO.type)
        Assertions.assertEquals("player1", messageDTO.player)
        Assertions.assertEquals("lobby1", messageDTO.lobbyId)

        Assertions.assertNotNull(messageDTO.players)
        Assertions.assertEquals(2, messageDTO.players?.size)

        val player1Actual = messageDTO.players?.get("player1Id")
        val player2Actual = messageDTO.players?.get("player2Id")

        Assertions.assertNotNull(player1Actual)
        Assertions.assertEquals("player1Id", player1Actual?.id)
        Assertions.assertEquals("TestUser1", player1Actual?.username)
        Assertions.assertEquals("#ABCDEF", player1Actual?.color)
        Assertions.assertTrue(player1Actual?.isHost == true)
        Assertions.assertTrue(player1Actual?.isReady == true)
        Assertions.assertEquals(5, player1Actual?.victoryPoints)
        Assertions.assertEquals(
            mapOf(TileType.ORE to 2, TileType.WHEAT to 5),
            player1Actual?.resources
        ) // Angepasste Erwartung

        Assertions.assertNotNull(player2Actual)
        Assertions.assertEquals("player2Id", player2Actual?.id)
        Assertions.assertEquals("TestUser2", player2Actual?.username)
        Assertions.assertEquals("#FEDCBA", player2Actual?.color)
        Assertions.assertTrue(player2Actual?.isHost == false)
        Assertions.assertTrue(player2Actual?.isReady == false)
        Assertions.assertEquals(3, player2Actual?.victoryPoints)
        Assertions.assertEquals(
            mapOf(TileType.WOOD to 1, TileType.CLAY to 3),
            player2Actual?.resources
        ) // Angepasste Erwartung


        Assertions.assertEquals(expectedMessage, messageDTO.message)
    }

    @Test
    fun fromJsonShouldNotThrowErrorOnUnknownKeys() {
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
        Assertions.assertNull(messageDTO.players)
        Assertions.assertNull(messageDTO.message)
    }

    @Test
    fun testFromJsonWithNullValues() {
        val json: String = """
            {
                "type": "CREATE_LOBBY",
                "player": null,
                "lobbyId": null,
                "players": null,
                "message": null
            }
        """.trimIndent()

        val messageDTO = MessageDTO.fromJson(json)
        Assertions.assertEquals(MessageType.CREATE_LOBBY, messageDTO.type)
        Assertions.assertNull(messageDTO.player)
        Assertions.assertNull(messageDTO.lobbyId)
        Assertions.assertNull(messageDTO.players)
        Assertions.assertNull(messageDTO.message)
    }

    @Test
    fun testToStringWithNullValues() {
        val messageDTO = MessageDTO(MessageType.JOIN_LOBBY)
        val expected = """{"type":"JOIN_LOBBY"}"""

        val actual: String = messageDTO.toString()

        val expectedJsonElement = Json.parseToJsonElement(expected)
        val actualJsonElement = Json.parseToJsonElement(actual)
        Assertions.assertEquals(expectedJsonElement, actualJsonElement)
    }

    @Test
    fun testFromJsonWithEmptyPlayersMap() {
        val json: String = """
            {
                "type": "LOBBY_UPDATED",
                "player": "host",
                "lobbyId": "emptylobby",
                "players": {},
                "message": null
            }
        """.trimIndent()

        val messageDTO = MessageDTO.fromJson(json)
        Assertions.assertEquals(MessageType.LOBBY_UPDATED, messageDTO.type)
        Assertions.assertEquals("host", messageDTO.player)
        Assertions.assertEquals("emptylobby", messageDTO.lobbyId)
        Assertions.assertNotNull(messageDTO.players)
        Assertions.assertTrue(messageDTO.players?.isEmpty() == true)
        Assertions.assertNull(messageDTO.message)
    }

    @Test
    fun testToStringWithEmptyPlayersMap() {
        val messageDTO = MessageDTO(MessageType.LOBBY_UPDATED, players = emptyMap())
        val expected = """{"type":"LOBBY_UPDATED","players":{}}"""

        val actual: String = messageDTO.toString()

        val expectedJsonElement = Json.parseToJsonElement(expected)
        val actualJsonElement = Json.parseToJsonElement(actual)
        Assertions.assertEquals(expectedJsonElement, actualJsonElement)
    }

    @Test
    fun testPlayerInfoDefaultValuesDeserialization() {
        val json = """
            {
                "type": "LOBBY_UPDATED",
                "players": {
                    "p1": {
                        "id": "p1",
                        "username": "DefaultUser"
                    }
                }
            }
        """.trimIndent()

        val messageDTO = MessageDTO.fromJson(json)
        val playerInfo = messageDTO.players?.get("p1")

        Assertions.assertNotNull(playerInfo)
        Assertions.assertEquals("p1", playerInfo?.id)
        Assertions.assertEquals("DefaultUser", playerInfo?.username)
        Assertions.assertEquals("#8C4E27", playerInfo?.color)
        Assertions.assertFalse(playerInfo?.isHost == true)
        Assertions.assertFalse(playerInfo?.isReady == true)
        Assertions.assertEquals(0, playerInfo?.victoryPoints)
        Assertions.assertTrue(playerInfo?.resources?.isEmpty() == true)
    }
}