package com.example.cataniaunited.ui.host_and_join

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanGold
import com.example.cataniaunited.ui.theme.catanRessourceBar

@Composable
fun HostAndJoinScreen(
    onBackClick: () -> Unit,
    onHostSelected: () -> Unit,
    onJoinSelected: () -> Unit
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

            Image(
                painter = painterResource(id = R.drawable.catania_united_logo),
                contentDescription = "Catania United Logo",
                contentScale = ContentScale.Fit
            )

            val buttonShape = RoundedCornerShape(30.dp)

            Button(
                onClick = onHostSelected,
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
                    text = "HOST GAME",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Text(
                text = "OR",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            Button(
                onClick = onJoinSelected,
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
                    text = "JOIN GAME",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
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
