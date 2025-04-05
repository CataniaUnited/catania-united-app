package com.example.cataniaunited.ui.startingpage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme

class StartingScreen {

    @Composable // UI component
    fun StartingScreen(onLearnClick: () -> Unit, onStartClick: () -> Unit){

        Box(modifier = Modifier
            .fillMaxSize()
            .background(catanClay)){

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "CATAN UNIVERSE",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 40.sp),
                    color = catanGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Button(
                    onClick = onLearnClick, // calls onLearnClick when pressed
                    colors = ButtonDefaults.buttonColors(containerColor = catanGold),
                    modifier = Modifier
                        .width(300.dp)
                        .height(56.dp)
                ){
                    Text(
                        text = "LEARN",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }

                Text(
                    text = "OR",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = 15.dp)
                )

                

            }

        }
    }

    @Preview(
        name = "Starting Screen - Landscape",
        widthDp = 891,
        heightDp = 411,
        showBackground = true
    )
    @Composable
    fun StartingScreenPreview() {
        CataniaUnitedTheme(darkTheme = false, dynamicColor = false){
            StartingScreen(
                onLearnClick = {},
                onStartClick = {}
            )
        }
    }
}