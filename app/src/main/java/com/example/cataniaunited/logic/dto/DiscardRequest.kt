package com.example.cataniaunited.logic.dto

import com.example.cataniaunited.data.model.TileType
import kotlinx.serialization.Serializable

@Serializable
data class DiscardRequest(
    val discardResources: Map<TileType, Int>
)
