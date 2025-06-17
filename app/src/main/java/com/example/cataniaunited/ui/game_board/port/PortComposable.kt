package com.example.cataniaunited.ui.game_board.port

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.Port
import com.example.cataniaunited.data.model.TileType
import kotlin.math.PI

private fun Double.toDegrees(): Float = (this * 180.0 / PI).toFloat()

@Composable
fun PortComposable(
    modifier: Modifier = Modifier,
    port: Port,
    size: Dp,
) {
    val portVisuals = port.portVisuals
    val portTransform = portVisuals.portTransform

    val tradeRatioText = "${port.inputResourceAmount}:1"
    val resourceIconSize = size * 0.6f

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer(
                rotationZ = portTransform.rotation.toDegrees()
            )
            .background(Color(0xFFC0C0C0).copy(alpha = 0.7f), CircleShape)
            .padding(size * 0.1f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (port.portType == "SpecificResourcePort" && port.resource != null) {
                Image(
                    painter = painterResource(id = getResourceIconForPort(port.resource)),
                    contentDescription = port.resource.name,
                    modifier = Modifier.size(resourceIconSize)
                )
            } else { // GeneralPort or missing resource
                Image(
                    painter = painterResource(id = R.drawable.general_port_icon),
                    contentDescription = "General Port",
                    modifier = Modifier.size(resourceIconSize)
                )
            }
            Text(
                text = tradeRatioText,
                fontSize = (size.value * 0.25f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}


@Composable
private fun getResourceIconForPort(tileType: TileType): Int {
    return when (tileType) {
        TileType.WOOD -> R.drawable.wood_icon
        TileType.CLAY -> R.drawable.clay_icon
        TileType.SHEEP -> R.drawable.sheep_icon
        TileType.WHEAT -> R.drawable.wheat_icon
        TileType.ORE -> R.drawable.ore_icon
        TileType.DESERT -> R.drawable.desert_tile
    }
}