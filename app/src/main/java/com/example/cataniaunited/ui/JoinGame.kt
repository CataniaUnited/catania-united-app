package com.example.cataniaunited.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext

class JoinGame : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val ctx = this

            JoinGameScreen(
                onBackClick = {
                    finish() }, // go back

                onJoinClick = { gameCode ->
                    // TODO: Connect to game logic here
                    Toast.makeText(ctx, "Trying to join game: $gameCode", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
