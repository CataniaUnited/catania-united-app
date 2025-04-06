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


class MainActivity : ComponentActivity() {

    lateinit var webSocketClient: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: example connection to server, remove on correct implementation
        webSocketClient = (application as MainApplication).webSocketClient
        webSocketClient.sendMessage("Hallo from Catania United App!")

        enableEdgeToEdge()
        setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                NavHost( // nav graph - shows the right screen depending on route
                    navController = navController,
                    startDestination = "starting"
                ) {
                    composable("starting"){
                        StartingScreen(
                            onLearnClick = { navController.navigate("tutorial")},
                            onStartClick = {} // add page "host or join game"
                        )
                    }
                     composable("tutorial"){
                         TutorialScreen()
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

