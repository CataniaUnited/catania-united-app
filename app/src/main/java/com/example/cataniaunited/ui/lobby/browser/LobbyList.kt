package com.example.cataniaunited.ui.lobby.browser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.LobbyInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

@Composable
fun LobbyList(
    lobbies: List<LobbyInfo>,
    modifier: Modifier = Modifier,
    onJoinLobbyClick: (String) -> Unit,
) {

    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        stickyHeader {

            if(lobbies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 0.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {

                    Spacer(modifier = Modifier.size(48.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Lobby ID",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.5f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Host",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.5f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Player Count",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1.5f)
                    )

                    Spacer(modifier = Modifier.height(40.dp).width(ButtonDefaults.MinWidth))
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Currently no open lobbies",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if(lobbies.isNotEmpty()){
            items(lobbies) { lobby ->
                LobbyListItem(
                    lobby = lobby,
                    onJoinLobbyClick = onJoinLobbyClick
                )
            }
        }

    }
}