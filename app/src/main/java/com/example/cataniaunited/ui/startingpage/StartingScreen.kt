package com.example.cataniaunited.ui.startingpage

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanGold
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.theme.catanRessourceBar


@Composable // UI component
fun StartingScreen(
    onLearnClick: () -> Unit,
    onStartClick: () -> Unit,
    onTestClick: () -> Unit,
    onCreateLobbyClick: () -> Unit,
    currentLobbyId: String?
) {

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.catan_starting_page_background),
            contentDescription = "Starting Page Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(430.dp)
                .height(400.dp)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CATAN UNIVERSE",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 40.sp),
                color = catanGold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            val buttonShape = RoundedCornerShape(30.dp)

            Button(
                onClick = onCreateLobbyClick,
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
                    text = "CREATE LOBBY",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Text(
                text = "OR",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 15.dp, bottom = 15.dp)
            )

            Button(
                onClick = onStartClick,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = catanGold,
                    disabledContainerColor = Color.Gray
                ),
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
                    text = "START GAME",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }


            Button(
                onClick = onLearnClick,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = catanGold),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier // Original modifier
                    .width(300.dp)
                    .height(56.dp)
                    .shadow(
                        elevation = 13.dp,
                        shape = buttonShape,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
                    .padding(top = 15.dp)
            ) {
                Text(
                    text = "LEARN",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Button(
                onClick = onTestClick,
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
                    .padding(top = 15.dp)
            ) {
                Text(
                    text = "TEST PAGE",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}