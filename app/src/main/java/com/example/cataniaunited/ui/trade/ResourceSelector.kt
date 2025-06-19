package com.example.cataniaunited.ui.trade

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.TileType

@Composable
fun ResourceSelector(
    resource: TileType,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val iconRes = when (resource) {
        TileType.WOOD -> R.drawable.wood_icon
        TileType.CLAY -> R.drawable.clay_icon
        TileType.SHEEP -> R.drawable.sheep_icon
        TileType.WHEAT -> R.drawable.wheat_icon
        TileType.ORE -> R.drawable.ore_icon
        else -> R.drawable.desert_tile
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Decrement Button
        IconButton(onClick = onDecrement, enabled = count > 0) {
            Icon(
                Icons.Default.RemoveCircle,
                "Decrement",
                tint = if (count > 0) Color.White else Color.Gray
            )
        }

        // Icon and Count
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(id = iconRes), contentDescription = resource.name, Modifier.size(28.dp))
            Text(
                "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(30.dp)
            )
        }

        // Increment Button
        IconButton(onClick = onIncrement) {
            Icon(Icons.Default.AddCircle, "Increment", tint = Color.White)
        }
    }
}