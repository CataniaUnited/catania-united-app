package com.example.cataniaunited.ui

import android.os.Bundle
import android.widget.Toast
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.cataniaunited.logic.HostJoinLogic
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider

class HostAndJoin : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val ctx = this // <- used for finish

            HostAndJoinScreen(
                onBackClick = {
                    ctx.finish() // close the current screen
                },


                onHostSelected = {
                    HostJoinLogic.sendCreateLobby()
                    Toast.makeText(ctx, "Sent CREATE_LOBBY to server", Toast.LENGTH_SHORT).show()
                },


                        onJoinSelected = {
                    val intent = Intent(this, JoinGame::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}
