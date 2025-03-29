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
import com.example.cataniaunited.ws.WebSocketListenerImpl
import com.example.cataniaunited.ws.WebSocketManager
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme

class MainActivity : ComponentActivity() {

    lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: example connection to server, remove on correct implementation
        webSocketManager = (application as MainApplication).webSocketManager
        webSocketManager.sendMessage("Hallo from Catania United App!")

        enableEdgeToEdge()
        setContent {
            CataniaUnitedTheme {
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
        webSocketManager.close()
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