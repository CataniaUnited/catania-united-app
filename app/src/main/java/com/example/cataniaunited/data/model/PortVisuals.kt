package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PortVisuals(
    val portTransform: PortTransform,
    val settlementPosition1Id: Int,
    val settlementPosition2Id: Int,
    val buildingSite1Position: List<Double>, // [x, y]
    val buildingSite2Position: List<Double>  // [x, y]
)