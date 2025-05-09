package com.example.cataniaunited.logic.dto

enum class MessageType {
    //Server Messages
    CONNECTION_SUCCESSFUL,
    CLIENT_DISCONNECTED,
    ERROR,
    LOBBY_UPDATED,
    PLAYER_JOINED,
    LOBBY_CREATED,
    GAME_BOARD_JSON,
    DICE_RESULT,

    //Client Messages
    CREATE_LOBBY,
    JOIN_LOBBY,
    SET_USERNAME,
    PLACE_SETTLEMENT,
    PLACE_ROAD,
    CREATE_GAME_BOARD,
    ROLL_DICE
}