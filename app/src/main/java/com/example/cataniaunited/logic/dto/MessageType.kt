package com.example.cataniaunited.logic.dto

enum class MessageType {
    //Server Messages
    CONNECTION_SUCCESSFUL,
    CLIENT_DISCONNECTED,
    ERROR,
    LOBBY_UPDATED,
    PLAYER_JOINED,
    START_GAME,
    LOBBY_CREATED,
    GAME_BOARD_JSON,
       // ← already existed on server, add to Android
    GAME_STARTED,

    //Client Messages
    CREATE_LOBBY,
    JOIN_LOBBY,
    SET_USERNAME,
    PLACE_SETTLEMENT,
    PLACE_ROAD,
    CREATE_GAME_BOARD
}