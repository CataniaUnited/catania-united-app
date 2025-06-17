package com.example.cataniaunited.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Robber(
    val id: Int,
    val position: List<Double>, // [x, y] coordinates
    val currentTileId: Int,
    val canRobberMove: Boolean
)
