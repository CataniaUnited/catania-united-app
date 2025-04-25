package com.example.cataniaunited

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.test.TestPage
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import com.example.cataniaunited.ui.game_board.board.GameScreen


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
                            onStartClick = {
                                val testLobbyId = "lobby_test_123"// TODO Replace Placeholder with real logic on fetching game logic
                                navController.navigate("game/$testLobbyId")
                            },
                            onTestClick = { navController.navigate("test") }
                        )
                    }
                    composable("tutorial") {
                        TutorialScreen(onBackClick = { navController.navigateUp() })
                    }

                    composable(
                        route = "game/{lobbyId}",
                        arguments = listOf(navArgument("lobbyId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        // Retrieve lobby ID
                        val lobbyIdArg = backStackEntry.arguments?.getString("lobbyId")
                        if (lobbyIdArg == null) {
                            // Handle error: Lobby ID missing, maybe navigate back or show error
                            Log.e("Navigation", "Lobby ID missing in arguments for game route!")
                            navController.navigateUp() // Go back if ID is missing
                        } else {
                            // Pass the retrieved lobbyId to the GameScreen
                            GameScreen(lobbyId = lobbyIdArg)
                        }
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