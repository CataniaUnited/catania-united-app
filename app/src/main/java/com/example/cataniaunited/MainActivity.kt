package com.example.cataniaunited

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cataniaunited.logic.game.GameBoardLogic
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.test.TestPage
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import com.example.cataniaunited.ui.game_board.board.GameScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MainActivity : ComponentActivity() {
    private val gameBoardLogic = GameBoardLogic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                val application = application as MainApplication // Get app instance

                // --- Listen for Navigation Events ---
                // This handles the navigation *after* the board JSON is received
                LaunchedEffect(Unit) { // Launch only once per composition lifecycle
                    Log.d("MainActivity", "Setting up navigation listener.")
                    application.navigateToGameFlow
                        .onEach { lobbyId ->
                            Log.i("MainActivity", "Received navigation trigger for Lobby: $lobbyId")
                            // Ensure we're on the main thread if required by NavController, though usually okay from lifecycleScope
                            // withContext(Dispatchers.Main) {
                            navController.navigate("game/$lobbyId") {
                                popUpTo("starting") { inclusive = true } // Remove starting screen from backstack
                            }
                            // }
                        }
                        .launchIn(lifecycleScope) // Use the Activity's lifecycle scope
                }

                NavHost(
                    navController = navController,
                    startDestination = "starting"
                ) {
                    composable("starting") {
                        StartingScreen(
                            onLearnClick = { navController.navigate("tutorial") },
                            onStartClick = {
                                val testLobbyId = "lobby_test_123" // TODO: use real LobbyID
                                Log.i("MainActivity", "Start Game clicked. Requesting board for lobby: $testLobbyId")
                                try {
                                    gameBoardLogic.requestNewGameBoard(playerCount = 4, lobbyId = testLobbyId)
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error requesting new game board", e)
                                }
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
                        val lobbyIdArg = backStackEntry.arguments?.getString("lobbyId")
                        if (lobbyIdArg == null) {
                            Log.e("Navigation", "Lobby ID missing in arguments for game route! Navigating back.")
                            LaunchedEffect(Unit) { // Navigate safely after composition
                                navController.navigateUp()
                            }
                        } else {
                            Log.d("Navigation", "Navigating to GameScreen for lobby: $lobbyIdArg")
                            // Clear previous board data *before* loading new screen's ViewModel
                            application.clearGameBoardData()
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