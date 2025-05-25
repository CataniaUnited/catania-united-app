package com.example.cataniaunited

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cataniaunited.ui.lobby.LobbyScreen
import com.example.cataniaunited.logic.host_and_join.HostJoinLogic
import com.example.cataniaunited.logic.game.GameBoardLogic
import com.example.cataniaunited.ui.host_and_join.HostAndJoinScreen
import com.example.cataniaunited.ui.host_and_join.JoinGameScreen
import com.example.cataniaunited.ui.game.GameScreen
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var gameBoardLogic: GameBoardLogic
    @Inject
    lateinit var hostJoinLogic: HostJoinLogic

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                val application = application as MainApplication

                val currentLobbyIdState by application.currentLobbyIdFlow.collectAsState()
                val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsState(initial = null)
                val currentRoute = currentBackStackEntry?.destination?.route

                LaunchedEffect(currentLobbyIdState) {
                    Log.d("MainActivity", "Collected Lobby ID State changed: $currentLobbyIdState")
                    if(currentLobbyIdState != null){
                        if(currentRoute == "hostandjoin") {
                            gameBoardLogic.requestBoardForLobby(lobbyId = currentLobbyIdState!!)
                        }
                        navController.navigate("lobby/${currentLobbyIdState}")
                    }
                }

                LaunchedEffect(Unit) {
                    application.navigateToLobbyFlow
                        .onEach { lobbyId ->
                            navController.navigate("lobby/$lobbyId") {
                                popUpTo("starting") { inclusive = true }
                            }
                        }
                        .launchIn(lifecycleScope)
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
                            onStartClick = { navController.navigate("hostandjoin") },
                        )
                    }
                    composable("tutorial") {
                        TutorialScreen(onBackClick = { navController.navigateUp() })
                    }
                    composable("hostandjoin") {
                        HostAndJoinScreen(
                            onBackClick = { navController.navigateUp() },
                            onHostSelected = {
                                hostJoinLogic.sendCreateLobby()
                                //navController.navigate("lobby/")
                            },
                            onJoinSelected = { navController.navigate("joingame") }
                        )
                    }
                    composable("joingame") {
                        JoinGameScreen(
                            onBackClick = { navController.navigateUp() },
                            onJoinClick = { lobbyId ->
                                hostJoinLogic.sendJoinLobby(lobbyId)
                                navController.navigate("lobby/$lobbyId")
                            }
                        )
                    }
                    composable(
                        route = "lobby/{lobbyId}"
                    ) { backStackEntry ->
                        val lobbyId = backStackEntry.arguments?.getString("lobbyId")
                        if (lobbyId == null) {
                            Log.e("Navigation", "Lobby ID missing! Navigating back.")
                            LaunchedEffect(Unit) { navController.navigateUp() }
                        }
                        else {
                            LobbyScreen(
                                lobbyId = lobbyId,
                                players = application.players,
                                onCancelClick = {navController.navigate("joingame")},
                                onStartGameClick = {
                                    Log.i("LobbyScreen", "Starting game for lobby: $lobbyId")
                                    gameBoardLogic.requestBoardForLobby(lobbyId = lobbyId, isCreate = false)
                                    navController.navigate("game/${lobbyId}")
                                }
                            )
                        }
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
                            GameScreen(
                                lobbyId = lobbyIdArg,
                                navController = navController)
                        }
                    }
                }
            }
        }
    }

}