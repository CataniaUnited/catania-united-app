package com.example.cataniaunited

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.cataniaunited.ws.WebSocketClient
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cataniaunited.ui.startingpage.StartingScreen
import com.example.cataniaunited.ui.tutorial.TutorialScreen



class MainActivity : ComponentActivity() {

    lateinit var webSocketClient: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: example connection to server, remove on correct implementation
        webSocketClient = (application as MainApplication).webSocketClient
        webSocketClient.sendMessage("Hallo from Catania United App!")

        enableEdgeToEdge()
        setContent {
            CataniaUnitedTheme {
                val navController = rememberNavController()
                NavHost( // shows the right screen depending on route
                    navController = navController,
                    startDestination = "starting"
                ) {
                    composable("starting"){
                        StartingScreen(
                            onLearnClick = { navController.navigate("tutorial")},
                            onStartClick = {} // add page "host or join game"
                        )
                    }
                     composable("tutorial"){
                         TutorialScreen()
                     }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        webSocketClient.close()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CataniaUnitedTheme {
        Greeting("Android")
    }
}