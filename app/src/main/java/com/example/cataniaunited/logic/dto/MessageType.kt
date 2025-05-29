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
    GAME_WON,
    PLAYER_RESOURCES,

    //Client Messages
    CREATE_LOBBY,
    JOIN_LOBBY,
    SET_USERNAME,
    SET_ACTIVE_PLAYER, //-> TODO: Remove after implementation of player order
    PLACE_SETTLEMENT,
    UPGRADE_SETTLEMENT,
    PLACE_ROAD,
    CREATE_GAME_BOARD,
    GET_GAME_BOARD,
    BUY_DEVELOPMENT_CARD,
    ROLL_DICE,
    SET_READY
}