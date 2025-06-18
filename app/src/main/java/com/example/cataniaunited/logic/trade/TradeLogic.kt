package com.example.cataniaunited.logic.trade

import android.util.Log
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.dto.TradeRequest
import com.example.cataniaunited.logic.player.PlayerSessionManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import javax.inject.Inject

class TradeLogic @Inject constructor(
    private val playerSessionManager: PlayerSessionManager
) {
    fun sendBankTrade(lobbyId: String, tradeRequest: TradeRequest) {
        val playerId = try {
            playerSessionManager.getPlayerId()
        } catch (ise: IllegalStateException) {
            Log.e("TradeLogic", "Cannot send trade, player ID not available", ise)
            return
        }

        val webSocketClient = MainApplication.getInstance().getWebSocketClient()
        if (webSocketClient.isConnected()) {
            val payload = Json.encodeToJsonElement(tradeRequest).jsonObject

            val msg = MessageDTO(
                type = MessageType.TRADE_WITH_BANK,
                player = playerId,
                lobbyId = lobbyId,
                message = payload
            )
            webSocketClient.sendMessage(msg)
            Log.i("TradeLogic", "TRADE_WITH_BANK sent: $msg")
        } else {
            Log.e("TradeLogic", "WebSocket not connected, cannot send trade.")
        }
    }
}