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
import androidx.compose.ui.tooling.preview.Preview

class StartingScreen {

    @Composable // UI component
    fun StartingScreen(onLearnClick: () -> Unit, onStartClick: () -> Unit){

        Box(modifier = Modifier
            .fillMaxSize()
            .background(catanClay)){

        }
    }

    @Preview(
        name = "Starting Screen - Landscape",
        widthDp = 640,
        heightDp = 360,
        showBackground = true
    )
    @Composable
    fun StartingScreenLandscapePreview() {
        StartingScreen(
            onLearnClick = {},
            onStartClick = {}
        )
    }
}