package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
            .padding(16.dp)
    ){
        Column (
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "LOBBY",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 40.sp),
                color = catanGold
            )

            Button(onClick = onStartGameClick) {

            }

            Button(onClick = onCancelClick) {
                Text(text = "Cancel")
            }
        }
    }
}