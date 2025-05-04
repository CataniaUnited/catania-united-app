package com.example.cataniaunited.logic.lobby

import com.example.cataniaunited.ui.theme.PlayerColors

object LobbyColorManager {

    private val assignments: MutableMap<String, MutableMap<String, PlayerColors>> = mutableMapOf()

    fun assignColor(lobbyId: String, username: String): PlayerColors? {
        val lobbyMap = assignments.getOrPut(lobbyId) { mutableMapOf() }

        lobbyMap[username]?.let { return it }

        val assignedColors = lobbyMap.values.toSet()
        val availableColors = PlayerColors.entries.filter{it !in assignedColors}.shuffled()

        val selectedColor = availableColors.firstOrNull() ?: return null

        lobbyMap[username] = selectedColor
        return selectedColor
    }

    fun getColor(lobbyId: String, username: String): PlayerColors? {
        return assignments[lobbyId]?.get(username)
    }

    fun removePlayer(lobbyId: String, username: String) {
        assignments[lobbyId]?.remove(username)
    }

    fun clearLobby(lobbyId: String) {
        assignments.remove(lobbyId)
    }

    fun clearAllColors() {
        assignments.clear()
    }
}