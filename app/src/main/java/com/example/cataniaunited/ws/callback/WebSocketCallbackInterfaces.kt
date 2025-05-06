package com.example.cataniaunited.ws.callback

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