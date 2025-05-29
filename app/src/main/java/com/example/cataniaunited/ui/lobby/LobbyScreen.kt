package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    players: List<PlayerInfo>?,
    gameViewModel: GameViewModel = hiltViewModel(),
    onCancelClick: () -> Unit,
    onStartGameClick: () -> Unit,
    onToggleReadyClick: () -> Unit
) {

    val playerId = gameViewModel.playerId
    val actualPlayers: List<PlayerInfo> = players ?: emptyList()
    val playerInfo: PlayerInfo? = actualPlayers.find { it.id == playerId }

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
                text = "Lobby ID: $lobbyId",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                color = catanGold
            )

            Text(
                text = "Players Ready: ${actualPlayers.filter { it.isReady }.size} / ${actualPlayers.size}",
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
                actualPlayers.forEach { player ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.player_icon128),
                                contentDescription = "player icon",
                                modifier = Modifier
                                    .size(80.dp)
                                    .border(8.dp, Color(player.color.toColorInt()), RectangleShape),
                                contentScale = ContentScale.Crop
                            )
                            if (player.isReady) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Ready",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(32.dp)
                                        .background(Color(0xFF008000), CircleShape)
                                )
                            }
                        }
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
                if (playerInfo?.isHost == true) {
                    Button(
                        onClick = onStartGameClick,
                        enabled = actualPlayers.all { it.isReady },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF008000),
                            contentColor = Color.White,
                        ),
                        modifier = Modifier
                            .width(150.dp)
                            .height(40.dp)

                    ) {
                        Text(text = "Start Game")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onToggleReadyClick,
                    modifier = Modifier
                        .width(150.dp)
                        .height(40.dp)
                ) {
                    if (playerInfo?.isReady == true) {
                        Text(text = "Set not ready")
                    } else {
                        Text(text = "Set ready")
                    }
                }
            }
        }

        IconButton(
            onClick = onCancelClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp, start = 16.dp)
                .size(40.dp)
                .background(catanGold, CircleShape)
                .border(BorderStroke(1.dp, Color.Black), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
    }
}