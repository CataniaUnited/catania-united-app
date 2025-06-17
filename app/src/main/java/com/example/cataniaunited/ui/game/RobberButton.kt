package com.example.cataniaunited.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.R

@Composable
fun RobberButton(
    onRobberClicked: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean
){

    Image(
        painter = painterResource(R.drawable.robber_icon),
        contentDescription = "place robber",
        modifier = Modifier.size(32.dp)
    )
}