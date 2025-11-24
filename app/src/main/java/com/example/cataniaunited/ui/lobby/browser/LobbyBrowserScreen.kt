package com.example.cataniaunited.ui.lobby.browser

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled._360
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.data.model.LobbyInfo
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanGold

@Composable
fun LobbyBrowserScreen(
    lobbies: List<LobbyInfo> = emptyList<LobbyInfo>(),
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onJoinLobbyClick: (String) -> Unit,
) {

    LaunchedEffect(Unit) {
        onRefreshClick()
    }

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
                text = "Lobby Browser",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                color = catanGold
            )

            Spacer(modifier = Modifier.height(16.dp))

            LobbyList(
                lobbies = lobbies,
                onJoinLobbyClick = onJoinLobbyClick
            )
        }

        IconButton(
            onClick = onBackClick,
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

        IconButton(
            onClick = onRefreshClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 16.dp)
                .size(40.dp)
                .background(catanGold, CircleShape)
                .border(BorderStroke(1.dp, Color.Black), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
    }
}