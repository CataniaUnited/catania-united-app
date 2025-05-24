package com.example.cataniaunited.logic.dto

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class MessageDTO(
    val type: MessageType,
    val player: String? = null,
    val lobbyId: String? = null,
    val players: List<String>? = null,
    val message: JsonObject? = null
) {

    companion object {
        private val jsonFormat = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        fun fromJson(jsonString: String): MessageDTO {
            return jsonFormat.decodeFromString(jsonString)
        }
    }

    override fun toString(): String {
        return Json.encodeToString(this)
    }
}