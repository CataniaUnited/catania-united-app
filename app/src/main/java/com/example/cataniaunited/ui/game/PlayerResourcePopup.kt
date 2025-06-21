package com.example.cataniaunited.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.ui.theme.catanClayLight

@Composable
fun PlayerResourcePopup(
    playerId: String,
    players: Map<String, PlayerInfo>,
    modifier: Modifier = Modifier,
    onCheatAttempt: (TileType) -> Unit
) {
    val resources = players[playerId]?.resources ?: emptyMap()
    val displayOrder = listOf(
        TileType.WOOD,
        TileType.CLAY,
        TileType.SHEEP,
        TileType.WHEAT,
        TileType.ORE
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = catanClayLight),
        modifier = modifier
            .padding(top = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayOrder.forEach { type ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(
                            id = when (type) {
                                TileType.WOOD -> R.drawable.ressource_card_wood
                                TileType.CLAY -> R.drawable.ressource_card_clay
                                TileType.WHEAT -> R.drawable.ressource_card_wheat
                                TileType.ORE -> R.drawable.ressource_card_ore
                                TileType.SHEEP -> R.drawable.ressource_card_wool
                                else -> R.drawable.ic_launcher_foreground
                            }
                        ),
                        contentDescription = type.name,
                        modifier = Modifier
                            .size(48.dp)
                            .pointerInput(type) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        onCheatAttempt(type)
                                    }
                                )
                            }
                    )
                    Text(text = resources[type]?.toString() ?: "0")
                }
            }
        }
    }
}
