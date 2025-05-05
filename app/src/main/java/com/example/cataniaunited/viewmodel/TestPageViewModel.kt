package com.example.cataniaunited.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cataniaunited.logic.game.GameBoardLogic

class TestPageViewModel : ViewModel() {
    private val gameBoardLogic : GameBoardLogic = GameBoardLogic()

    fun onPlaceSettlementClick(settlementPositionId: Int, lobbyId: String){
        gameBoardLogic.placeSettlement(settlementPositionId, lobbyId);
    }

    fun onPlaceRoadClick(roadId: Int, lobbyId: String){
        gameBoardLogic.placeRoad(roadId, lobbyId);
    }
}