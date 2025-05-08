package com.example.cataniaunited.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cataniaunited.logic.game.GameBoardLogic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TestPageViewModel : ViewModel() {
    private val gameBoardLogic : GameBoardLogic = GameBoardLogic()

    private val _diceResult = MutableStateFlow<Pair<Int, Int>?>(null)
    val diceResult: StateFlow<Pair<Int, Int>?> = _diceResult

    fun onPlaceSettlementClick(settlementPositionId: Int, lobbyId: String){
        gameBoardLogic.placeSettlement(settlementPositionId, lobbyId);
    }

    fun onPlaceRoadClick(roadId: Int, lobbyId: String){
        gameBoardLogic.placeRoad(roadId, lobbyId);
    }

    fun rollDice(lobbyId: String) {
        gameBoardLogic.rollDice(lobbyId)
    }

    fun handleDiceResult(dice1: Int, dice2: Int) {
        Log.d("DiceRoll", "Handling dice result: $dice1, $dice2")
        viewModelScope.launch {
            _diceResult.emit(Pair(dice1, dice2))
        }
    }
}