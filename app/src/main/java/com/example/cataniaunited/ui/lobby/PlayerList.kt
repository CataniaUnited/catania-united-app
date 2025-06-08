package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.data.model.PlayerInfo

@Composable
fun PlayerList(
    players: List<PlayerInfo>,
    modifier: Modifier = Modifier,
    onToggleReadyClick: () -> Unit,
) {

    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(players) { player ->
            PlayerListItem(
                player = player,
                onToggleReadyClick = onToggleReadyClick,
            )
        }
    }
}