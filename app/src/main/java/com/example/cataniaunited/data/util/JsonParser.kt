package com.example.cataniaunited.data.util

import android.util.Log
import kotlinx.serialization.json.Json
import com.example.cataniaunited.data.model.GameBoardModel


val jsonParser = Json {
    ignoreUnknownKeys = false // If backend adds fields throw exception
    isLenient = true         // to ignore minor syntax issues (trailing ,,,)
}

fun parseGameBoard(jsonString: String): GameBoardModel? {
    return try {
        jsonParser.decodeFromString<GameBoardModel>(jsonString)
    } catch (e: Exception) {
        Log.e("GameBoardParser", "Error parsing game board JSON", e)
        null
    }
}

