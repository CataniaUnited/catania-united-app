package com.example.cataniaunited.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cataniaunited.logic.HostJoinLogic

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
                    HostJoinLogic.sendJoinLobby(gameCode)
                    Toast.makeText(ctx, "Sent JOIN_LOBBY to server with ID $gameCode", Toast.LENGTH_SHORT).show()


                }
            )
        }
    }
}
