package com.example.cataniaunited.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.ui.theme.catanGold

@Composable
fun StyledButton(text: String, onClick: () -> Unit) {
    val buttonShape = RoundedCornerShape(30.dp)
    Button(
        onClick = onClick,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(containerColor = catanGold),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .width(300.dp)
            .height(56.dp)
            .shadow(
                elevation = 13.dp,
                shape = buttonShape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}