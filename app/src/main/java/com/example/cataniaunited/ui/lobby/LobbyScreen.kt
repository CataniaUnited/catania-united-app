package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanGold

@Composable
fun LobbyScreen(
    lobbyId: String,
    players: List<PlayerInfo>,
    gameViewModel: GameViewModel = hiltViewModel(),
    onCancelClick: () -> Unit,
    onStartGameClick: () -> Unit
) {

    val playerId = gameViewModel.playerId
    val isHost: Boolean = players.any { it.isHost && it.id == playerId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(catanClay)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SEARCHING FOR OPPONENTS...",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                color = catanGold
            )

            Text(
                text = "Players: ${players.size} / 4",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                players.forEach { player ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.player_icon128),
                            contentDescription = "player icon",
                            modifier = Modifier
                                .size(80.dp)
                                .border(8.dp, Color(player.color.toColorInt()), RectangleShape)
                                .padding(4.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        player.username?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isHost) {
                    Button(
                        onClick = onStartGameClick,
                        modifier = Modifier
                            .width(150.dp)
                            .height(40.dp)
                    ) {
                        Text(text = "Start Game")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Cancel")
                }
            }
        }

        // Hier wird die Lobby ID am unteren rechten Rand platziert
        Text(
            text = "Lobby ID: $lobbyId",
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(50.dp)
        )
    }
}