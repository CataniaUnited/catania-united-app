package com.example.cataniaunited.logic.lobby

data class LobbyPlayer (
    val lobbyId: String,
    val playerId: String,
    val username: String? = null,
    val colorHex: String,
    val isHost: Boolean? = false,
    val isReady: Boolean? = false
)
