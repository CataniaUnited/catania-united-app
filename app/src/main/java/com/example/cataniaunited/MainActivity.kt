package com.example.cataniaunited

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cataniaunited.logic.game.GameBoardLogic
import com.example.cataniaunited.logic.host_and_join.HostJoinLogic
import com.example.cataniaunited.logic.lobby.LobbyLogic
import com.example.cataniaunited.ui.game.GameScreen
import com.example.cataniaunited.ui.host_and_join.HostAndJoinScreen
import com.example.cataniaunited.ui.host_and_join.JoinGameScreen
import com.example.cataniaunited.ui.lobby.LobbyScreen
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

    @Inject
    lateinit var lobbyLogic: LobbyLogic

    @Inject
    lateinit var application: MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val currentLobbyIdState by application.currentLobbyIdFlow.collectAsState()
                val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsState(
                    initial = null
                )
                val currentRoute = currentBackStackEntry?.destination?.route

                LaunchedEffect(currentLobbyIdState) {
                    Log.d("MainActivity", "Collected Lobby ID State changed: $currentLobbyIdState")
                    if (currentLobbyIdState != null && (currentRoute == null || !currentRoute.startsWith(
                            "lobby/"
                        ))
                    ) {
                        navController.navigate("lobby/${currentLobbyIdState}")
                    }
                }

                LaunchedEffect(currentRoute) {
                    val previousBackStackEntry = navController.previousBackStackEntry
                    val previousRoute = previousBackStackEntry?.destination?.route

                    Log.d(
                        "NavListener",
                        "Current Route: $currentRoute, Previous Route: $previousRoute"
                    )
                    if (
                        currentRoute == "starting"
                        && previousRoute != null
                        && (previousRoute.startsWith("lobby/") || previousRoute.startsWith("game/"))
                    ) {
                        val lobbyId = previousBackStackEntry.arguments?.getString("lobbyId");
                        Log.i(
                            "MainActivity",
                            "Detected navigation from lobby or game to starting screen. Performing lobby cleanup: $lobbyId."
                        )
                        if (lobbyId != null) {
                            application.clearLobbyData()
                            lobbyLogic.leaveLobby(lobbyId)
                            Log.i("MainActivity", "Cleaned up data for lobby: $lobbyId")
                        } else {
                            Log.w(
                                "MainActivity",
                                "Attempted to clean up lobby, but currentLobbyId was null."
                            )
                        }
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
                    Log.d("MainActivity", "Listening to errorFlow from MainApplication...")
                    application.errorFlow.collect { errorMessage ->
                        Log.e("MainActivity", "Error received: $errorMessage")
                        snackbarHostState.showSnackbar(
                            message = errorMessage,
                            withDismissAction = true
                        )
                    }
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        }
                    }
                ) {
                    it
                    NavHost(
                        navController = navController,
                        startDestination = "starting",
                        modifier = Modifier
                            .fillMaxSize()
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
                                },
                                onJoinSelected = { navController.navigate("joingame") }
                            )
                        }
                        composable("joingame") {
                            JoinGameScreen(
                                onBackClick = { navController.navigateUp() },
                                onJoinClick = { lobbyId ->
                                    hostJoinLogic.sendJoinLobby(lobbyId)
                                }
                            )
                        }
                        composable(
                            route = "lobby/{lobbyId}"
                        ) { backStackEntry ->
                            val lobbyId = backStackEntry.arguments?.getString("lobbyId")
                            if (lobbyId == null) {
                                Log.e("Navigation", "Lobby ID missing! Navigating back.")
                                LaunchedEffect(Unit) { navController.navigate("starting") }
                            } else {
                                LobbyScreen(
                                    lobbyId = lobbyId,
                                    players = application.players,
                                    onCancelClick = {
                                        navController.navigate("starting")
                                    },
                                    onStartGameClick = {
                                        Log.i("LobbyScreen", "Starting game for lobby: $lobbyId")
                                        lobbyLogic.startGame(lobbyId = lobbyId)
                                        navController.navigate("game/${lobbyId}")
                                    },
                                    onToggleReadyClick = {
                                        Log.i("LobbyScreen", "Toggle ready state")
                                        lobbyLogic.toggleReady(lobbyId)
                                    },
                                    onChangeUsername = {
                                        Log.i("LobbyScreen", "Changing username: username = $it")
                                        lobbyLogic.setUsername(lobbyId, it)
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
                                Log.d(
                                    "Navigation",
                                    "Navigating to GameScreen for lobby: $lobbyIdArg"
                                )
                                GameScreen(
                                    lobbyId = lobbyIdArg,
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                    )
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

}