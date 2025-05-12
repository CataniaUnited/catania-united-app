package com.example.cataniaunited.ui.startingpage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.common.OrDivider
import com.example.cataniaunited.ui.common.StyledButton
import com.example.cataniaunited.ui.theme.catanBorder
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanGold

@Composable
fun StartingScreen(
    onLearnClick: () -> Unit,
    onStartClick: () -> Unit,
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
                    color = catanBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    catanClay.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CATAN UNIVERSE",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 40.sp),
                    color = catanGold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Image(
                    painter = painterResource(id = R.drawable.catania_united_logo),
                    contentDescription = "Catania United Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(80.dp)
                        .padding(bottom = 24.dp)
                )

                StyledButton(text = "START GAME", onClick = onStartClick)

                OrDivider()

                StyledButton(text = "LEARN", onClick = onLearnClick)
            }
        }
    }
}
