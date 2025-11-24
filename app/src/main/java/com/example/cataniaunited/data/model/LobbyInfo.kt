package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LobbyInfo (
    val id: String = "",
    val playerCount: Int = 0,
    val hostPlayer: String = ""
)