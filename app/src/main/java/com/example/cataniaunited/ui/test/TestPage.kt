package com.example.cataniaunited.ui.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.cataniaunited.viewmodel.TestPageViewModel

@Composable
fun TestPage(testPageViewModel: TestPageViewModel = TestPageViewModel()) {

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.catan_starting_page_background),
            contentDescription = "Starting Page Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        val buttonShape = RoundedCornerShape(30.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { testPageViewModel.onPlaceSettlementClick(1, "lobby1") },
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = catanGold),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 13.dp,
                        shape = buttonShape,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
            ) {
                Text(
                    text = "TEST PLACE SETTLEMENT",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Button(
                onClick = { testPageViewModel.onPlaceRoadClick(1, "lobby1") },
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = catanGold),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 13.dp,
                        shape = buttonShape,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
            ) {
                Text(
                    text = "TEST PLACE ROAD",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Button(
                onClick = { },
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = catanGold),
                border = BorderStroke(1.dp, Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 13.dp,
                        shape = buttonShape,
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
            ) {
                Text(
                    text = "ROLL DICE",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}