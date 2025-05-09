package com.example.cataniaunited.logic.game

sealed class GameEvent {
    data class GameWon(val winnerId: String, val leaderboard: List<PlayerInfo>) : GameEvent()
}

data class PlayerInfo(
    val playerId: String,
    val username: String,
    val colorHex: String,
    val victoryPoints: Int
)