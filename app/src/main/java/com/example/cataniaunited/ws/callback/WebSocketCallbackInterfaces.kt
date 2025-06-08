package com.example.cataniaunited.ws.callback

import com.example.cataniaunited.data.model.PlayerInfo

fun interface OnConnectionSuccess {
    fun onConnectionSuccess(playerId: String)
}

fun interface OnLobbyCreated {
    fun onLobbyCreated(lobbyId: String, players: Map<String, PlayerInfo>)
}

fun interface OnPlayerJoined {
    fun onPlayerJoined(lobbyId: String, players: Map<String, PlayerInfo>)
}

fun interface OnLobbyUpdated {
    fun onLobbyUpdated(lobbyId: String, players: Map<String, PlayerInfo>)
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

fun interface OnDiceRolling {
    fun onDiceRolling(playerName: String)
}

fun interface OnPlayerResourcesReceived {
    fun onPlayerResourcesReceived(players: Map<String, PlayerInfo>)
}