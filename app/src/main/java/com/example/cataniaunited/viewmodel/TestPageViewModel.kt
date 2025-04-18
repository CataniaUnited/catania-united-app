package com.example.cataniaunited.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cataniaunited.logic.game.GameBoard

class TestPageViewModel : ViewModel() {
    private val gameBoard : GameBoard = GameBoard()

    fun onPlaceSettlementClick(settlementPositionId: Int, lobbyId: String){
        gameBoard.placeSettlement(settlementPositionId, lobbyId);
    }

    fun onPlaceRoadClick(roadId: Int, lobbyId: String){
        gameBoard.placeRoad(roadId, lobbyId);
    }
}