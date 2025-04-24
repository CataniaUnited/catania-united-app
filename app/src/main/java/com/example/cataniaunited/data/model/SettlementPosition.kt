package com.example.cataniaunited.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SettlementPosition(
    val id: Int,
    val building: String?,
    val coordinates: List<Double>
)