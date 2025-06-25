package com.example.cataniaunited.data.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BuildingCostModel(
    val settlement: Map<String, Int>,
    val city: Map<String, Int>,
    val road: Map<String, Int>,
    @SerialName("development_card")
    val developmentCard: Map<String, Int>
)