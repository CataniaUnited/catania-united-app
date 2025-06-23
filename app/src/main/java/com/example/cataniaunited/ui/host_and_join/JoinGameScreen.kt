package com.example.cataniaunited.ui.host_and_join

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanGold
import com.example.cataniaunited.ui.theme.catanRessourceBar

@Composable
fun JoinGameScreen(
    onBackClick: () -> Unit,
    onJoinClick: (String) -> Unit
) {
    var gameCode by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.catan_starting_page_background),
            contentDescription = "Join Page Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(430.dp)
                .height(340.dp)
                .border(
                    width = 9.dp,
                    color = catanRessourceBar,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    catanClay.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .offset(y = (-40).dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "ENTER GAME ID",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            OutlinedTextField(
                value = gameCode,
                onValueChange = { gameCode = it },
                placeholder = { Text("Game Code") },
                singleLine = true,
                shape = RoundedCornerShape(40.dp),
                modifier = Modifier
                    .width(320.dp)
                    .height(60.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = catanGold,
                    unfocusedBorderColor = catanGold,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            val buttonShape = RoundedCornerShape(30.dp)

            Button(
                onClick = { onJoinClick(gameCode) },
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = catanGold),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .width(300.dp)
                    .height(56.dp)
                    .shadow(
                        elevation = 13.dp,
                        shape = buttonShape,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
            ) {
                Text(
                    text = "JOIN",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 32.dp, start = 16.dp)
                .size(40.dp)
                .background(catanGold, CircleShape)
                .border(BorderStroke(1.dp, Color.Black), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
    }
}
