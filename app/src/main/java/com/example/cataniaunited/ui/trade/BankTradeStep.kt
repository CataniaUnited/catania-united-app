package com.example.cataniaunited.ui.trade

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.ui.theme.catanGoldLight


@Composable
fun BankTradeStep(
    tradeOffer: Pair<Map<TileType, Int>, Map<TileType, Int>>,
    onUpdateOffer: (TileType, Int) -> Unit,
    onUpdateTarget: (TileType, Int) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val (offered, target) = tradeOffer
    val displayOrder = listOf(TileType.WOOD, TileType.CLAY, TileType.SHEEP, TileType.WHEAT, TileType.ORE)

    val isTradeValid = offered.isNotEmpty() && target.isNotEmpty()

    // Root column for sticky header/footer layout
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // --- 1. Header (Sticky Top) ---
        Text("Trade with Bank", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(Modifier.height(16.dp))

        // --- 2. Scrollable Content Area ---
        Column(
            modifier = Modifier
                .weight(1f) // This makes the column fill available space
                .verticalScroll(rememberScrollState()) // This makes the content scrollable
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top // Align to top to prevent stretching
            ) {
                // "You Give" Section
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("You Give", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    displayOrder.forEach { resource ->
                        ResourceSelector(
                            resource = resource,
                            count = offered.getOrDefault(resource, 0),
                            onIncrement = { onUpdateOffer(resource, 1) },
                            onDecrement = { onUpdateOffer(resource, -1) }
                        )
                    }
                }

                // Arrow Separator
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Trade to",
                    tint = catanGoldLight,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(horizontal = 8.dp, vertical = 48.dp) // Add padding to center it vertically a bit
                )

                // "You Get" Section
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("You Get", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    displayOrder.forEach { resource ->
                        ResourceSelector(
                            resource = resource,
                            count = target.getOrDefault(resource, 0),
                            onIncrement = { onUpdateTarget(resource, 1) },
                            onDecrement = { onUpdateTarget(resource, -1) }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // --- 3. Footer (Sticky Bottom) ---
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Back", color = Color.White) }
            Button(onClick = onSubmit, enabled = isTradeValid) {
                Text("Confirm Trade")
            }
        }
    }
}
