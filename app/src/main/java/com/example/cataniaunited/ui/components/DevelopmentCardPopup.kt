package com.example.cataniaunited.ui.components


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun DevelopmentCardPopup(
    cardType: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = {
            Text("You drew a development card!")
        },
        text = {
            Text(
                when (cardType) {
                    "KNIGHT" -> "🛡 Knight"
                    "VICTORY_POINT" -> "🏆 Victory Point"
                    "ROAD_BUILDING" -> "🛣 Road Building"
                    "YEAR_OF_PLENTY" -> "🌾 Year of Plenty"
                    "MONOPOLY" -> "👑 Monopoly"
                    else -> "Unknown Card"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    )
}
