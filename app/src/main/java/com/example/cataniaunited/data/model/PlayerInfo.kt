package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerInfo(
    val id: String = "",
    val username: String?,
    val color: String = "#8C4E27",
    val isHost: Boolean = false,
    val isReady: Boolean = false,
    val isActivePlayer: Boolean = false,
    val canRollDice: Boolean = false,
    val isSetupRound: Boolean = false,
    val victoryPoints: Int = 0,
    val resources: Map<TileType, Int> = emptyMap<TileType, Int>()
)
