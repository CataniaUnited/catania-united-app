package com.example.cataniaunited

import androidx.compose.runtime.*
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cataniaunited.logic.game.GameBoardLogic
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import com.example.cataniaunited.ui.game.GameScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var gameBoardLogic: GameBoardLogic

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
                                        gameBoardLogic.requestBoardForLobby(lobbyId = lobbyToStart, playerCount = 16)
                                    } catch (e: Exception) {
                                        Log.e("MainActivity", "Error requesting board for lobby", e)
                                    }
                                } else {
                                    Log.e("MainActivity", "Start Game clicked, but no current Lobby ID state!")
                                }
                            },
                            currentLobbyId = currentLobbyIdState // Pass the collected state
                        )
                    }
                    composable("tutorial") {
                        TutorialScreen(onBackClick = { navController.navigateUp() })
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

}