package com.example.cataniaunited.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType
import com.example.cataniaunited.logic.game.GameBoard
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


class TestPageViewModel : ViewModel() {
    private val gameBoard : GameBoard = GameBoard()

    fun onPlaceSettlementClick(settlementPositionId: Int, lobbyId: String){
        gameBoard.placeSettlement(settlementPositionId, lobbyId);
    }

    fun onPlaceRoadClick(roadId: Int, lobbyId: String){
        gameBoard.placeRoad(roadId, lobbyId);
    }

    fun onDiceRoll(dice1: Int, dice2: Int, lobbyId: String) {
        gameBoard.rollDice(dice1,dice2,lobbyId)
    }
}