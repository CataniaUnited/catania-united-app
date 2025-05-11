package com.example.cataniaunited

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cataniaunited.logic.game.GameBoardLogic
import com.example.cataniaunited.ui.game_board.board.GameScreen
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var gameBoardLogic: GameBoardLogic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                val app = application as MainApplication

                // 1) Observe current lobby ID for display
                val currentLobbyId by app.currentLobbyIdFlow.collectAsState()
                LaunchedEffect(currentLobbyId) {
                    Log.d("MainActivity", "Lobby ID changed: $currentLobbyId")
                }

                // 2) When server pushes GAME_STARTED, navigate to GameScreen
                LaunchedEffect(Unit) {
                    app.navigateToGameFlow
                        .onEach { lobbyId ->
                            Log.i("MainActivity", "Navigating to GameScreen for lobby: $lobbyId")
                            navController.navigate("game/$lobbyId") {
                                popUpTo("starting") { inclusive = true }
                            }
                        }
                        .launchIn(lifecycleScope)
                }

                // 3) Nav graph
                NavHost(navController = navController, startDestination = "starting") {
                    composable("starting") {
                        StartingScreen(
                            currentLobbyId     = currentLobbyId,
                            onLearnClick       = { navController.navigate("tutorial") },
                            onCreateLobbyClick = {
                                Log.i("MainActivity", "CREATE_LOBBY clicked")
                                gameBoardLogic.requestCreateLobby()
                            },
                            onStartClick       = {
                                currentLobbyId?.let { lobby ->
                                    Log.i("MainActivity", "START_GAME clicked for $lobby")
                                    gameBoardLogic.requestStartGame(lobby, playerCount = 4)
                                } ?: Log.e("MainActivity","No lobby ID yet, can't start game!")
                            },
                            onTestClick        = { navController.navigate("test") }
                        )
                    }

                    composable("tutorial") {
                        TutorialScreen(onBackClick = { navController.navigateUp() })
                    }

                    composable("game/{lobbyId}") { backStackEntry ->
                        backStackEntry.arguments?.getString("lobbyId")?.let { id ->
                            GameScreen(lobbyId = id)
                        } ?: run {
                            Log.e("MainActivity","Missing lobbyId, popping back")
                            LaunchedEffect(Unit) { navController.navigateUp() }
                        }
                    }
                }
            }
        }
    }
}