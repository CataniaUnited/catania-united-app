package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ui.theme.catanGoldLight
import com.example.cataniaunited.ui.theme.success

@Composable
fun PlayerListItem(
    player: PlayerInfo,
    onToggleReadyClick: () -> Unit,
    gameViewModel: GameViewModel = hiltViewModel(),
) {

    val playerId = gameViewModel.playerId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(catanGoldLight)
            .border(1.dp, Color.Black)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            painter = painterResource(id = R.drawable.player_icon128),
            contentDescription = "player icon",
            modifier = Modifier
                .size(48.dp)
                .border(4.dp, Color(player.color.toColorInt()), RectangleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        val usernameText = player.username!! + if (player.id == playerId) " (YOU)" else ""
        Text(
            text = usernameText,
            color = Color.Black,
            modifier = Modifier.weight(1.5f)
        )

        if (player.id == playerId) {
            IconButton(
                onClick = onToggleReadyClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (player.isReady) success else Color.Red,
                        shape = CircleShape
                    )
                    .border(BorderStroke(1.dp, Color.Black), CircleShape)
            ) {
                Icon(
                    imageVector = if (player.isReady) Icons.Outlined.Check else Icons.Default.Block,
                    contentDescription = if (player.isReady) "Ready" else "Not ready",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            Icon(
                imageVector = if (player.isReady) Icons.Default.CheckCircle else Icons.Default.Block,
                contentDescription = if (player.isReady) "Ready" else "Not ready",
                tint = if (player.isReady) success else Color.Red,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}