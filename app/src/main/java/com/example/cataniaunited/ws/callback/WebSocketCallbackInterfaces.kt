package com.example.cataniaunited.ws.callback

fun interface OnConnectionSuccess {
    fun onConnectionSuccess(playerId: String)
}

fun interface OnLobbyCreated {
    fun onLobbyCreated(lobbyId: String, playerId: String, color: String?)
}

fun interface OnPlayerJoined {
    fun onPlayerJoined(playerId: String, color: String)
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