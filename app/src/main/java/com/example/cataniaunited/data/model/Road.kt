package com.example.cataniaunited.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Road(
    val id: Int,
    val owner: String?,
    val coordinates: List<Double>, // [x, y] - center of the road segment
    val rotationAngle: Double, // I forgot if in radians or degrees
    val color: String?
)