package com.example.cataniaunited.ui.game

import androidx.compose.material3.Button
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.cataniaunited.R

@Composable
fun RobberButton (
    enabled : Boolean,
    onClick: () -> Unit = {}
) {
    Button(
        enabled = enabled,
        onClick = { onClick() },
        modifier = Modifier
            .padding(8.dp)
            .zIndex(1f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.robber_icon),
            contentDescription = "place Robber",
            modifier = Modifier.size(32.dp)
        )
    }
}