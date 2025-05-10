package com.example.cataniaunited.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class GameBoardModel(
    val tiles: List<Tile>,
    val settlementPositions: List<SettlementPosition>,
    val roads: List<Road>,
    val ringsOfBoard: Int,
    val sizeOfHex: Int,
    val players: List<PlayerInfo>
)