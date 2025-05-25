package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerInfo(
    val playerId: String = "",
    val username: String?,
    val colorHex: String = "#8C4E27",
    val isHost: Boolean = false,
    val isReady: Boolean = false,
    val victoryPoints: Int = 0
)
