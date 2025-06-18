package com.example.cataniaunited.logic.dto

import com.example.cataniaunited.data.model.TileType
import kotlinx.serialization.Serializable
import java.util.Map

@Serializable
data class TradeRequest(
    val offeredResources: Map<TileType, Int>,
    val targetResources: Map<TileType, Int>
)