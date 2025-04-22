package com.example.cataniaunited

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cataniaunited.ui.lobby.LobbyScreen
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.test.TestPage
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.tutorial.TutorialScreen


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                NavHost( // nav graph - shows the right screen depending on route
                    navController = navController,
                    startDestination = "starting"
                ) {
                    composable("starting") {
                        StartingScreen(
                            onLearnClick = { navController.navigate("tutorial") },
                            onStartClick = {}, // add page "host or join game"
                            onTestClick = { navController.navigate("test") }
                        )
                    }
                    composable("tutorial") {
                        TutorialScreen(onBackClick = { navController.navigateUp() })
                    }
                    composable("lobby"){
                        LobbyScreen(
                            onCancelClick = { /*TODO*/ },
                            onStartGameClick = { /*TODO*/ },
                        )
                    }
                    composable("test") {
                        TestPage()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }
}

