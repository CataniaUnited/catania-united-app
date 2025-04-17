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
        MainApplication.getInstance()
            .sendMessage(MessageDTO(MessageType.PLACE_SETTLEMENT, playerId, lobbyId, null, message))
    }

    fun placeRoad(roadId: Int, lobbyId: String) {
        val playerId = MainApplication.getInstance().getPlayerId()
        val message = buildJsonObject {
            put("roadId", roadId)
        }
        MainApplication.getInstance()
            .sendMessage(MessageDTO(MessageType.PLACE_ROAD, playerId, lobbyId, null, message))
    }
}