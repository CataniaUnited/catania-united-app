package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanGold

@Composable
fun LobbyScreen(onCancelClick: () -> Unit, onStartGameClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(catanClay)
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
        ){
            Text(
                text = "SEARCHING FOR OPPONENTS...",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                color = catanGold
            )
        }

        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Button(onClick = onStartGameClick,
                modifier = Modifier
                    .width(120.dp)
                    .height(60.dp)
            ) {
                Text(text = "Start Game")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = onCancelClick,
                modifier = Modifier
                    .width(100.dp)
                    .height(50.dp)
            ) {
                Text(text = "Cancel")
            }
        }
    }
}