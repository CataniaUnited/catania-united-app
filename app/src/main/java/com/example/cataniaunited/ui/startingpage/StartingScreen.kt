package com.example.cataniaunited.ui.startingpage

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.common.BackgroundScreen
import com.example.cataniaunited.ui.common.CentralCard
import com.example.cataniaunited.ui.common.OrDivider
import com.example.cataniaunited.ui.common.StyledButton


@Composable
fun StartingScreen(
    onLearnClick: () -> Unit,
    onStartClick: () -> Unit,
) {
    BackgroundScreen {
        CentralCard {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.catania_united_logo),
                    contentDescription = "Catania United Logo",
                    contentScale = ContentScale.Fit
                )

                StyledButton(text = "START GAME", onClick = onStartClick)

                OrDivider()

                StyledButton(text = "LEARN", onClick = onLearnClick)
            }
        }
    }
}