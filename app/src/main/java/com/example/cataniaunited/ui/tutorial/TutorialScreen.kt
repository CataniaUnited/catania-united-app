package com.example.cataniaunited.ui.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.ui.theme.catanGold
import com.example.cataniaunited.ui.theme.catanRessourceBar
import androidx.compose.ui.graphics.Color


@Composable
fun TutorialScreen() {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.catan_starting_page_background),
            contentDescription = "Tutorial Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .width(430.dp)
                .heightIn(min = 400.dp, max = 600.dp)
                .border(
                    width = 9.dp,
                    color = catanRessourceBar,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    catanGold.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp)
                )
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




