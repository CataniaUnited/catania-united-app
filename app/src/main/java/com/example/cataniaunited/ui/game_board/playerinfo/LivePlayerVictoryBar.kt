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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.logic.game.GameViewModel

@Composable
fun LivePlayerVictoryBar(
    gameViewModel: GameViewModel = hiltViewModel(),
    selectedPlayerId: String?,
    onPlayerClicked: (PlayerInfo, Int) -> Unit,
    onPlayerOffsetChanged: (Float) -> Unit
) {
    val playerState by gameViewModel.players.collectAsState()
    val vpMap by gameViewModel.victoryPoints.collectAsState()

    val players: List<PlayerInfo> = remember(playerState, vpMap) {
        playerState.values.toList()
    }

    LaunchedEffect(players) {
        Log.d("VictoryBar", "Loaded players: $players")
    }

    PlayerVictoryBar(
        players = players,
        selectedPlayerId = selectedPlayerId,
        onPlayerClicked = onPlayerClicked,
        onPlayerOffsetChanged = onPlayerOffsetChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    )


}
