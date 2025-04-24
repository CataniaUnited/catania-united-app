package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameBoardModel(
    val tiles: List<Tile>,
    val settlementPositions: List<SettlementPosition>,
    val roads: List<Road>,
    val ringsOfBoard: Int,
    val sizeOfHex: Int
)