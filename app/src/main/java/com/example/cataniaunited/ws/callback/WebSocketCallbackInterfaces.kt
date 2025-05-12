package com.example.cataniaunited.ws.callback

import com.example.cataniaunited.data.model.TileType

fun interface OnConnectionSuccess {
    fun onConnectionSuccess(playerId: String)
}

fun interface OnLobbyCreated {
    fun onLobbyCreated(lobbyId: String)
}

fun interface OnGameBoardReceived {
    fun onGameBoardReceived(lobbyId: String, boardJson: String)
}

fun interface OnWebSocketError {
    fun onError(error: Throwable)
}

fun interface OnWebSocketClosed {
    fun onClosed(code: Int, reason: String)
}

fun interface OnDiceResult {
    fun onDiceResult(dice1: Int, dice2: Int)
}

fun interface OnPlayerResourcesReceived {
    fun onPlayerResourcesReceived(resources: Map<TileType, Int>)
}