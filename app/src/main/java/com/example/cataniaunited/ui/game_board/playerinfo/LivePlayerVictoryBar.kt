package com.example.cataniaunited.ui.game_board.playerinfo

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.data.model.PlayerInfo

@Composable
fun LivePlayerVictoryBar(viewModel: GameViewModel, modifier: Modifier = Modifier) {
    val playerState by viewModel.players.collectAsState()
    val vpMap by viewModel.victoryPoints.collectAsState()

    val players: List<PlayerInfo> = remember(playerState, vpMap) {
        playerState.values.toList()
    }

    LaunchedEffect(players) {
        Log.d("VictoryBar", "Loaded players: $players")
    }

    viewModel.playerId

    PlayerVictoryBar(
        players = players,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    )
}
