package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Port(
    val inputResourceAmount: Int,
    val portVisuals: PortVisuals,
    val portType: String, // "GeneralPort" or "SpecificResourcePort"
    val resource: TileType? = null // Only Present if Port is a SpecificResourcePort
)