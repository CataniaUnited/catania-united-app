package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TileType {
    ORE,
    SHEEP,
    WHEAT,
    WOOD,
    CLAY,
    DESERT
}