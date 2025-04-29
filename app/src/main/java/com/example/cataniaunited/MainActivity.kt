package com.example.cataniaunited

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collect

import com.example.cataniaunited.MainApplication
import com.example.cataniaunited.logic.dto.MessageDTO
import com.example.cataniaunited.logic.dto.MessageType

import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.tutorial.TutorialScreen
import com.example.cataniaunited.ui.test.TestPage
import com.example.cataniaunited.ui.lobby.LobbyScreen
import com.example.cataniaunited.ui.lobby.LoadingScreen
import com.example.cataniaunited.ui.lobby.GameScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CataniaUnitedTheme {
                val navController = rememberNavController()

                NavHost(
                    navController    = navController,
                    startDestination = "starting"
                ) {
                    // ────── Starting Screen ──────
                    composable("starting") {
                        StartingScreen(
                            onLearnClick = { navController.navigate("tutorial") },
                            onStartClick = { navController.navigate("lobby")    },
                            onTestClick  = { navController.navigate("test")     }
                        )
                    }

                    // ────── Tutorial Screen ──────
                    composable("tutorial") {
                        TutorialScreen(onBackClick = { navController.popBackStack() })
                    }

                    // ───────── Test Page ─────────
                    composable("test") {
                        TestPage()
                    }

                    // ────────── Host Lobby ──────────
                    composable("lobby") {
                        LobbyScreen(
                            onBackClick = { navController.popBackStack() },
                            onStartGame = {
                                // grab your Application singleton
                                val app = application as MainApplication

                                // build a START_GAME message
                                val dto = MessageDTO(
                                    MessageType.START_GAME,
                                    app.getPlayerId(),
                                    app.getLobbyId()
                                )

                                // send it over the WS client
                                app.webSocketClient().sendMessage(dto)

                                // then show the loading spinner
                                navController.navigate("loading")
                            }
                        )
                    }

                    // ────────── Loading Spinner ──────────
                    composable("loading") {
                        LoadingScreen()

                        // listen for GAME_BOARD_JSON and navigate into the game
                        LaunchedEffect(Unit) {
                            val ws = (application as MainApplication).webSocketClient()
                            ws.messageFlow.collect { msg ->
                                if (msg.type == MessageType.GAME_BOARD_JSON) {
                                    navController.navigate("game") {
                                        popUpTo("loading") { inclusive = true }
                                    }
                                }
                            }
                        }
                    }

                    // ─────────── Actual Game ───────────
                    composable("game") {
                        GameScreen()
                    }
                }
            }
        }
    }
}
