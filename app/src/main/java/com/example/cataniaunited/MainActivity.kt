package com.example.cataniaunited

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import com.example.cataniaunited.ui.HostAndJoinScreen
import com.example.cataniaunited.ui.JoinGameScreen

class MainActivity : ComponentActivity() {

    lateinit var webSocketClient: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webSocketClient = (application as MainApplication).webSocketClient
        webSocketClient.sendMessage("Hallo from Catania United App!")

        enableEdgeToEdge()
        setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "starting"
                ) {
                    composable("starting") {
                        StartingScreen(
                            onLearnClick = { navController.navigate("tutorial") },
                            onStartClick = { navController.navigate("hostandjoin") }
                        )
                    }
                    composable("tutorial") {
                        TutorialScreen(
                            onBackClick = { navController.navigateUp() }
                        )
                    }
                    composable("hostandjoin") {
                        HostAndJoinScreen(
                            onBackClick = { navController.navigateUp() },
                            onHostSelected = { navController.navigate("hostgame") },
                            onJoinSelected = { navController.navigate("joingame") }
                        )
                    }
                    composable("joingame") {
                        JoinGameScreen(
                            onBackClick = { navController.navigateUp() },
                            onJoinClick = { /* Do something when user joins, or just leave empty for now */ }
                        )
                    }

                    composable("hostgame") {
                        HostAndJoinScreen(
                            onBackClick = { navController.navigateUp() },
                            onHostSelected = {},
                            onJoinSelected = {}
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        webSocketClient.close()
    }
}
