package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
    onToggleReadyClick: () -> Unit,
    onChangeUsername: (username: String) -> Unit,
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lobby ID: $lobbyId",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                color = catanGold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val title: String =
                if (actualPlayers.size > 1) "Players Ready: ${actualPlayers.filter { it.isReady }.size} / ${actualPlayers.size}"
                else "Need at least 2 players to start"
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (playerInfo?.isHost == true) {
                Button(
                    onClick = onStartGameClick,
                    enabled = actualPlayers.size > 1 && actualPlayers.all { it.isReady },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF008000),
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .border(BorderStroke(1.dp, Color.Black), CircleShape)
                        .width(150.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Start Game")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PlayerList(
                players = actualPlayers,
                onToggleReadyClick = onToggleReadyClick,
                onChangeUsername = onChangeUsername
            )
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