// app/src/main/java/com/example/cataniaunited/logic/game/GameBoard.kt
package com.example.cataniaunited.logic.game

import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GameBoard {

    /**
     * Tell the server you want to place a settlement at [settlementPositionId]
     * in lobby [lobbyId].
     */
    fun placeSettlement(settlementPositionId: Int, lobbyId: String) {
        // 1) grab your Application singleton
        val app = MainApplication.instance

        // 2) pull out the playerId
        val playerId = app.getPlayerId()

        // 3) build the JSON payload
        val payload = buildJsonObject {
            put("settlementPositionId", settlementPositionId)
        }

        // 4) send it over your WS client
        app
            .webSocketClient()
            .sendMessage(
                MessageDTO(
                    type     = MessageType.PLACE_SETTLEMENT,
                    player = playerId,
                    lobbyId  = lobbyId,
                    message  = payload
                )
            )
    }

    /**
     * check sonar cloud
     * Tell the server you want to place a road at [roadId]
     * in lobby [lobbyId].
     */
    fun placeRoad(roadId: Int, lobbyId: String) {
        val app       = MainApplication.instance
        val playerId  = app.getPlayerId()
        val payload   = buildJsonObject { put("roadId", roadId) }

        app
            .webSocketClient()
            .sendMessage(
                MessageDTO(
                    type     = MessageType.PLACE_ROAD,
                    player = playerId,
                    lobbyId  = lobbyId,
                    message  = payload
                )
            )
    }
}
