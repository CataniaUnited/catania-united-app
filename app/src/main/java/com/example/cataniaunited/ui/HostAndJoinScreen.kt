package com.example.cataniaunited.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.R

@Composable
fun HostAndJoinScreen(
    onBackClick: () -> Unit,
    onHostSelected: () -> Unit,
    onJoinSelected: () -> Unit
) {
    val backgroundColor = Color(0xFF9A572E)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        color = backgroundColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Icon Back Button (Top Left)
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF4C542), CircleShape)
                        .border(BorderStroke(1.dp, Color.Black), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Logo (Centered)
                Image(
                    painter = painterResource(id = R.drawable.catan_logo),
                    contentDescription = "Catan Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(100.dp) // make it a bit smaller
                        .padding(bottom = 36.dp)
                )

                // Button Container
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val buttonShape = RoundedCornerShape(40.dp)

                    Button(
                        onClick = onHostSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .shadow(6.dp, buttonShape),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF4C542)
                        ),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("HOST GAME", fontSize = 18.sp, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "OR",
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = onJoinSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .shadow(6.dp, buttonShape),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF4C542)
                        ),
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Text("JOIN GAME", fontSize = 18.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}
