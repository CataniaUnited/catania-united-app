package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple lobby screen showing a “Waiting in Lobby…” message,
 * a Start button and a Cancel button.
 */
@Composable
fun LobbyScreen(
    onBackClick: () -> Unit,
    onStartGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Waiting in Lobby…",
            style = MaterialTheme.typography.headlineSmall  // ← replaced h6 with headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onStartGame) {
            Text("Start Game")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackClick) {
            Text("Cancel")
        }
    }
}
