package com.example.cataniaunited.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer



@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GameBoardModel(
    val tiles: List<Tile>,
    val settlementPositions: List<SettlementPosition>,
    val roads: List<Road>,
    val ringsOfBoard: Int,
    val sizeOfHex: Int,
    @Serializable(with = PlayerMapToListSerializer::class)
    val players: List<PlayerInfo>? = null
)

object PlayerMapToListSerializer : KSerializer<List<PlayerInfo>?> {
    override val descriptor: SerialDescriptor =
        MapSerializer(String.serializer(), PlayerInfo.serializer()).descriptor

    override fun deserialize(decoder: Decoder): List<PlayerInfo>? {
        val map = decoder.decodeSerializableValue(
            MapSerializer(String.serializer(), PlayerInfo.serializer())
        )
        return map.map { (id, player) ->
            player.copy(playerId = id) // ← injects the map key into playerId
        }
    }

    override fun serialize(encoder: Encoder, value: List<PlayerInfo>?) {
        throw NotImplementedError("Serialization not required.")
    }
}

