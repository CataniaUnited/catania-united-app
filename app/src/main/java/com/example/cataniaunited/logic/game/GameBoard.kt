package com.example.cataniaunited.logic.game

import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GameBoard {

    fun placeSettlement(settlementPositionId: Int, lobbyId: String) {
        val playerId = MainApplication.getInstance().getPlayerId()
        val message = buildJsonObject {
            put("settlementPositionId", settlementPositionId)
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        webSocketClient.sendMessage(MessageDTO(MessageType.PLACE_SETTLEMENT, playerId, lobbyId, null, message))
    }

    fun placeRoad(roadId: Int, lobbyId: String) {
        val playerId = MainApplication.getInstance().getPlayerId()
        val message = buildJsonObject {
            put("roadId", roadId)
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        webSocketClient.sendMessage(MessageDTO(MessageType.PLACE_ROAD, playerId, lobbyId, null, message))
    }

    fun rollDice(dice1: Int, dice2: Int, lobbyId: String) {
        val playerId = MainApplication.getInstance().getPlayerId()
        val message = buildJsonObject {
            put("dice1", dice1)
            put("dice2", dice2)
            put("Result", dice1+dice2)
        }
        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        webSocketClient.sendMessage(MessageDTO(MessageType.ROLL_DICE, playerId, lobbyId, null, message))
    }
}