package com.example.cataniaunited.ui.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme


@Composable
fun TutorialScreen() {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.catan_starting_page_background),
            contentDescription = "Tutorial Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(
    name = "Tutorial Screen - Landscape",
    widthDp = 891,
    heightDp = 411,
    showBackground = true
)

@Composable
fun TutorialScreenPreview() {
    CataniaUnitedTheme(darkTheme = false, dynamicColor = false) {
        TutorialScreen()
    }
}




