package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanGold
import androidx.core.graphics.toColorInt

@Composable
fun LobbyScreen(
    players: List<LobbyPlayer>,
    onCancelClick: () -> Unit,
    onStartGameClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(catanClay)
    ){
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "SEARCHING FOR OPPONENTS...",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp),
                color = catanGold
            )

            Text(
                text = "Players: ${players.size} / 4",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ){
                players.forEach { player ->
                    val playerColor = Color(player.colorHex.toColorInt())
                    Column (horizontalAlignment = Alignment.CenterHorizontally){
                        Image(
                            painter = painterResource(id = R.drawable.player_icon128),
                            contentDescription = "player icon",
                            modifier = Modifier
                                .size(80.dp)
                                .border(6.dp, playerColor, RectangleShape)
                                .padding(4.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = player.username,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

            }

            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onStartGameClick,
                    modifier = Modifier
                        .width(120.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Start Game")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp)
                ) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}