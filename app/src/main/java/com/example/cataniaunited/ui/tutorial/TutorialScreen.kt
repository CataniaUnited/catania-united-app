package com.example.cataniaunited.ui.tutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign


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
                .height(400.dp)
                .border(
                    width = 9.dp,
                    color = catanRessourceBar,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    catanGold.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                TutorialHeading("TUTORIAL")
                TutorialParagraph("In Catan, each player aims to become the most successful settler by reaching 10 Victory Points (VPs)." +
                        " You start with 2 points by placing 2 settlements, and you gain more VPs by building additional settlements, upgrading them to cities, or acquiring special cards / achievements. " +
                        "The first player to reach 10 VPs on their turn immediately wins the game.")


        }
        }
    }
}

@Composable
fun TutorialHeading(title: String){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                catanRessourceBar,
                shape = RoundedCornerShape(10.dp)
                )
            .padding(vertical = 8.dp)
    ) {

        Text(
            text = title,
            color = catanGold,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()

        )
    }
}

@Composable
fun TutorialParagraph(text: String){

    Text(
        text = text,
        color = Color.Black,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 15.dp, end = 0.dp, bottom = 10.dp),
        textAlign = TextAlign.Justify

    )

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




