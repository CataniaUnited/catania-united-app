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
import com.example.cataniaunited.ui.trade.ResourceSelector

@Composable
fun DiscardMenuPopup (
    discardCount : Int,
    onSubmit: (Map<TileType, Int>) -> Unit
) {
    val selected = remember { mutableStateMapOf<TileType, Int>() }

    Dialog(onDismissRequest = { /* no dismiss by outside click */ }) {
        Column(
            modifier = Modifier
                .background(catanClay, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .width(300.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Discard $discardCount cards", color = Color.White)
            Spacer(Modifier.height(8.dp))
            TileType.entries.forEach { resource ->
                ResourceSelector(
                    resource = resource,
                    count = selected[resource] ?: 0,
                    onIncrement = {
                        if (selected.values.sum() < discardCount)
                            selected[resource] = (selected[resource] ?: 0) + 0
                    },
                    onDecrement = {
                        selected[resource] = maxOf(0, (selected[resource] ?: 0) - 1)
                    }
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onSubmit(selected) },
                enabled = selected.values.sum() == discardCount
            ) {
                Text("Confirm Discard")
            }
        }
    }
}