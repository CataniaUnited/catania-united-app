package com.example.cataniaunited

import androidx.compose.runtime.*
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cataniaunited.ui.lobby.LobbyScreen
import com.example.cataniaunited.logic.game.GameBoardLogic
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import com.example.cataniaunited.ui.game_board.board.GameScreen
import com.example.cataniaunited.ui.test.TestPage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val gameBoardLogic = GameBoardLogic()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                val application = application as MainApplication

                val currentLobbyIdState by application.currentLobbyIdFlow.collectAsState()

                LaunchedEffect(currentLobbyIdState) {
                    Log.d("MainActivity", "Collected Lobby ID State changed: $currentLobbyIdState")
                }



                LaunchedEffect(Unit) {
                    Log.d("MainActivity", "Setting up navigation listener.")
                    application.navigateToGameFlow
                        .onEach { lobbyId ->
                            Log.i("MainActivity", "Received navigation trigger for Lobby: $lobbyId")
                            navController.navigate("game/$lobbyId") {
                                popUpTo("starting") { inclusive = true }
                            }
                        }
                        .launchIn(lifecycleScope)
                }

                NavHost(
                    navController = navController,
                    startDestination = "starting"
                ) {
                    composable("starting") {
                        StartingScreen(
                            onLearnClick = { navController.navigate("tutorial") },
                            onCreateLobbyClick = {
                                Log.i("MainActivity", "Create Lobby button clicked.")
                                navController.navigate("lobby")
                                try {
                                    gameBoardLogic.requestCreateLobby()
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "Error requesting lobby creation", e)
                                }
                            },
                            onStartClick = {
                                val lobbyToStart = currentLobbyIdState // Use collected state
                                if (lobbyToStart != null) {
                                    Log.i("MainActivity", "Start Game button clicked for known lobby: $lobbyToStart")
                                    try {
                                        gameBoardLogic.requestBoardForLobby(lobbyId = lobbyToStart, playerCount = 4)
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Error requesting board for lobby", e)
                                    }
                                } else {
                                    Log.e("MainActivity", "Start Game clicked, but no current Lobby ID state!")
                                }
                            },
                            onTestClick = { navController.navigate("test") },
                            currentLobbyId = currentLobbyIdState // Pass the collected state
                        )
                    }
                    composable("tutorial") {
                        TutorialScreen(onBackClick = { navController.navigateUp() })
                    }
                    composable("test") {
                        TestPage()
                    }
                    composable("lobby") {
                        val app = LocalContext.current.applicationContext as MainApplication
                        LobbyScreen(
                            players = app.playersInLobby,
                            onCancelClick = {navController.navigate("starting")},
                            onStartGameClick = {
                                val lobbyId = currentLobbyIdState
                                if (lobbyId != null) {
                                    Log.i("LobbyScreen", "Starting game for lobby: $lobbyId")
                                    try {
                                        gameBoardLogic.requestBoardForLobby(lobbyId = lobbyId, playerCount = 4)
                                    } catch (e: Exception) {
                                        Log.e("LobbyScreen", "Error requesting board for lobby", e)
                                    }
                                }
                           }
                        )
                    }

                    composable(
                        route = "game/{lobbyId}",

                    ) { backStackEntry ->
                        val lobbyIdArg = backStackEntry.arguments?.getString("lobbyId")
                        if (lobbyIdArg == null) {
                            Log.e("Navigation", "Lobby ID missing! Navigating back.")
                            LaunchedEffect(Unit) { navController.navigateUp() }
                        } else {
                            Log.d("Navigation", "Navigating to GameScreen for lobby: $lobbyIdArg")
                            GameScreen(lobbyId = lobbyIdArg)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }
}