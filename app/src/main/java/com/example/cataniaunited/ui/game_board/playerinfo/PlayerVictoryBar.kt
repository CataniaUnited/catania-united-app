package com.example.cataniaunited.ui.game_board.playerinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.ui.theme.appTypography
import com.example.cataniaunited.ui.theme.catanClayLight

@Composable
fun PlayerVictoryBar(
    players: List<PlayerInfo>,
    selectedPlayerId: String?,
    modifier: Modifier = Modifier,
    onPlayerClicked: (PlayerInfo, Int) -> Unit,
    onPlayerOffsetChanged: (Float) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        players.forEachIndexed { index, player ->
            val borderColor = Color(player.color.toColorInt())
            val vpTextColor = if (borderColor.luminance() > 0.5f) Color.Black else Color.White

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(260.dp)
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                        .clickable {
                            onPlayerClicked(player, index)
                        }
                        .onGloballyPositioned { coords ->
                            if (selectedPlayerId == player.id) {
                                onPlayerOffsetChanged(coords.positionInRoot().x)
                            }
                        }
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(catanClayLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                if (player.isActivePlayer) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                player.username?.let {
                                    Text(
                                        text = it,
                                        style = appTypography.bodyLarge.copy(
                                            fontSize = 16.sp
                                        ),
                                        color = Color.White
                                    )
                                }
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
                                style = appTypography.bodyLarge.copy(fontSize = 16.sp),
                                color = vpTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}
