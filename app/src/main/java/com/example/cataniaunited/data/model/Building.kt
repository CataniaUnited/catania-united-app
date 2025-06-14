package com.example.cataniaunited.data.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Building(
    val owner: String,
    val color: String,
    val type: String
)
