package com.example.cataniaunited.ui.game_board.robber

import androidx.compose.foundation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.Robber

@Composable
fun RobberComposable(
    modifier: Modifier = Modifier,
    onRobberClick: () -> Unit = {}
){
    Image(
        painter = painterResource(R.drawable.robber_icon),
        contentDescription = "robber",
        modifier = modifier
            .clickable { onRobberClick() }
    )
}