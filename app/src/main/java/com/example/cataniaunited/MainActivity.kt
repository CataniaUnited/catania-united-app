package com.example.cataniaunited

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.test.TestPage
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import com.example.cataniaunited.ui.game_borad.GameScreen


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
                            onStartClick = { navController.navigate("game") },
                            onTestClick = { navController.navigate("test") }
                        )
                    }
                    composable("tutorial") {
                        TutorialScreen(onBackClick = { navController.navigateUp() })
                    }
                    composable("game") {
                        GameScreen()
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

