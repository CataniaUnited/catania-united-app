package com.example.cataniaunited.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.cataniaunited.R

@Composable
fun BuildButton(
    isOpen: Boolean,
    enabled: Boolean,
    onClick: (Boolean) -> Unit = {}
) {
    Button(
        enabled = enabled,
        onClick = { onClick(!isOpen) },
        modifier = Modifier
            .padding(8.dp)
            .zIndex(1f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.city),
                contentDescription = "Roll dice",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}