package com.example.cataniaunited.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.ui.theme.catanClay


@Composable
fun PlayerResourcesBar(
    modifier: Modifier = Modifier,
    resources: Map<TileType, Int>
) {

    val displayOrder = listOf(
        TileType.WOOD,
        TileType.CLAY,
        TileType.SHEEP,
        TileType.WHEAT,
        TileType.ORE
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(catanClay, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayOrder.forEach { tileType ->
            val count = resources[tileType] ?: 0
            ResourceItem(tileType = tileType, count = count)
        }
    }
}

@Composable
private fun ResourceItem(tileType: TileType, count: Int) {
    val iconRes = getResourceIcon(tileType)

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = tileType.name,
            modifier = Modifier.size(30.dp), // Adjust size as needed
            colorFilter = ColorFilter.tint(Color.Black)

        )
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 18.sp, // Adjust size as needed
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
private fun getResourceIcon(tileType: TileType): Int {
    return when (tileType) {
        TileType.WOOD -> R.drawable.wood_icon
        TileType.CLAY -> R.drawable.clay_icon
        TileType.SHEEP -> R.drawable.sheep_icon
        TileType.WHEAT -> R.drawable.wheat_icon
        TileType.ORE -> R.drawable.ore_icon
        TileType.WASTE -> R.drawable.desert_tile
    }
}