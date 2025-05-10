package com.example.cataniaunited.ui.game_board.playerinfo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.ui.theme.appTypography
import com.example.cataniaunited.ui.theme.catanClayLight

@Composable
fun PlayerVictoryBar(
    players: List<PlayerInfo>,
    currentPlayerId: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .background(Color.Transparent),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top
    ) {
        players.forEach { player ->
            val borderColor = Color(android.graphics.Color.parseColor(player.colorHex))
            Box(
                modifier = Modifier
                    .border(
                        border = BorderStroke(3.dp, borderColor),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .background(catanClayLight, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = player.username,
                        style = appTypography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(16.dp)
                            .background(borderColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VP ${player.victoryPoints}",
                        style = appTypography.bodyLarge.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPlayerVictoryBar() {
    PlayerVictoryBar(
        players = listOf(
            PlayerInfo("1", "Mia", "#FF0000", 4),
            PlayerInfo("2", "Nassir", "#0000FF", 10),
            PlayerInfo("3", "Jean", "#D4AF37", 9),
            PlayerInfo("4", "Candamir", "#800080", 0)
        ),
        currentPlayerId = "2"
    )
}

