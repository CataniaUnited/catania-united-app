package com.example.cataniaunited.ui.lobby.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.LobbyInfo
import com.example.cataniaunited.ui.theme.catanGoldLight

@Composable
fun LobbyListItem(
    lobby: LobbyInfo,
    onJoinLobbyClick: (String) -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(catanGoldLight)
            .border(1.dp, Color.Black)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {

        Icon(
            imageVector = Icons.Default.Groups,
            contentDescription = "Lobby Icon",
            tint = Color.Black,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = lobby.id,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = lobby.hostPlayer,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "${lobby.playerCount}",
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f)
        )

        Button(
            onClick = { onJoinLobbyClick(lobby.id) },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF008000),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .border(BorderStroke(1.dp, Color.Black), CircleShape)
                .height(40.dp)
        ) {
            Text(text = "JOIN")
        }
    }
}