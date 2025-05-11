package com.example.cataniaunited.ui.lobby

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.MainApplication

/**
 * The main game screen. Once your server has generated the board,
 * you can collect it (via your WebSocketClient in MainApplication)
 * and render it here.
 */
@Composable
fun GameScreen(lobbyId: String) {
    // retrieve your Application instance
    val app = LocalContext.current.applicationContext as MainApplication

    // hold onto the latest JSON for *this* lobby
    var boardJson by remember { mutableStateOf<String?>(null) }

    // whenever mainApp.latestBoardJson changes, pick it up
    LaunchedEffect(app.latestBoardJson) {
        // only accept boards for our lobby
        if (app.currentLobbyIdFlow.value == lobbyId) {
            boardJson = app.latestBoardJson
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (boardJson == null) {
            // still waiting for the server…
            CircularProgressIndicator()
        } else {
            // once it arrives, show it (swap this for your real board UI)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Lobby: $lobbyId",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Board JSON:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = boardJson!!,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
