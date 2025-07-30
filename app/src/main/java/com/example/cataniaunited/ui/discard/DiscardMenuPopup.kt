package com.example.cataniaunited.ui.discard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.ui.theme.catanClay

@Composable
fun DiscardMenuPopup (
    discardCount : Int,
    resources: Map<TileType, Int>,
    onDiscard: (TileType, Int) -> Unit,
    onSubmit: (Map<TileType, Int>) -> Unit
) {
    val displayOrder = listOf(TileType.WOOD, TileType.CLAY, TileType.SHEEP, TileType.WHEAT, TileType.ORE)

    Dialog(onDismissRequest = { /* no dismiss by outside click */ }) {
        Column(
            modifier = Modifier
                .background(catanClay, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Discard $discardCount cards", color = Color.White)
            Spacer(Modifier.height(4.dp))
            displayOrder.forEach { resource ->

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiscardSelector(
                        resource = resource,
                        count = resources[resource] ?: 0,
                        onDecrement = { onDiscard(resource, -1) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onSubmit(resources) },
                enabled = discardCount == 0
            ) {
                Text("Confirm Discard")
            }
        }
    }
}