package com.example.cataniaunited.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class SettlementPosition(
    val id: Int,
    val building: Building?,
    val coordinates: List<Double>
)