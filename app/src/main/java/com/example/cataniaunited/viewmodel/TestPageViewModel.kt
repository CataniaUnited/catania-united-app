package com.example.cataniaunited.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.game.GameBoard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TestPageViewModel : ViewModel() {
    private val gameBoard : GameBoard = GameBoard()

    // StateFlow to track dice results
    private val _diceResult = MutableStateFlow<Pair<Int, Int>?>(null)
    val diceResult: StateFlow<Pair<Int, Int>?> = _diceResult

    fun onPlaceSettlementClick(settlementPositionId: Int, lobbyId: String) {
        gameBoard.placeSettlement(settlementPositionId, lobbyId)
    }

    fun onPlaceRoadClick(roadId: Int, lobbyId: String) {
        gameBoard.placeRoad(roadId, lobbyId)
    }

    fun rollDice(lobbyId: String) {
        gameBoard.rollDice(lobbyId)
    }

    fun handleDiceResult(dice1: Int, dice2: Int) {
        Log.d("DiceRoll", "Handling dice result: $dice1, $dice2")
        viewModelScope.launch {
            _diceResult.emit(Pair(dice1, dice2))
        }
    }
}