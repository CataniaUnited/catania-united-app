package com.example.cataniaunited.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.cataniaunited.ui.theme.catanGold

@Composable
fun PlayerResourcePopup(
    resources: Map<TileType, Int>,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = catanGold),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your Resources", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayerResourcePopupPreview() {
    val sampleResources = mapOf(
        TileType.WOOD to 3,
        TileType.CLAY to 2,
        TileType.WHEAT to 1,
        TileType.ORE to 0,
        TileType.SHEEP to 4
    )

    PlayerResourcePopup(
        resources = sampleResources,
        onDismiss = {}
    )
}
