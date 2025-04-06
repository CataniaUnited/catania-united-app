package com.example.cataniaunited.ui.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme

class TutorialScreen {

    @Composable
    fun TutorialScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Tutorial Screen")
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
            TutorialScreen()
        }
    }


}