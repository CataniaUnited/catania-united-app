package com.example.cataniaunited.ui.game_board.playerinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.ui.theme.appTypography
import androidx.core.graphics.toColorInt


private val catanClayLight = Color(0xFFB76B3C)

@Composable
fun PlayerVictoryBar(
    players: List<PlayerInfo>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(Color.Transparent),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        players.forEach { player ->
            val borderColor = Color(player.color.toColorInt())
            val vpTextColor = if (borderColor.luminance() > 0.5f) Color.Black else Color.White

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(260.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Transparent)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(catanClayLight),
                        contentAlignment = Alignment.Center
                    ) {
                        player.username?.let {
                            Text(
                                text = it,
                                style = appTypography.bodyLarge.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(borderColor)
                    )
                    
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxHeight()
                            .background(borderColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${player.victoryPoints} VP",
                            style = appTypography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = vpTextColor
                        )
                    }
                }
            }
        }
    }
}


