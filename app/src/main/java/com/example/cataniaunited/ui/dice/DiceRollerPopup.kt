package com.example.cataniaunited.ui.dice

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.cataniaunited.R
import com.example.cataniaunited.logic.game.GameViewModel
import com.example.cataniaunited.ui.theme.catanClay
import kotlinx.coroutines.delay

@Composable
fun DiceRollerPopup(
    viewModel: GameViewModel,
    onClose: () -> Unit
) {
    val diceState by viewModel.diceState.collectAsState()
    if (diceState == null) return

    var currentDice1 by remember { mutableIntStateOf(1) }
    var currentDice2 by remember { mutableIntStateOf(1) }
    var showFinalResult by remember { mutableStateOf(false) }

    val rotation = rememberInfiniteTransition()
    val angle by rotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing)
        )
    )

    LaunchedEffect(diceState) {
        val state = diceState ?: return@LaunchedEffect

        if (state.isRolling) {
            showFinalResult = false
            val startTime = System.currentTimeMillis()

            // Minimum 2 second rolling animation
            while (state.isRolling && System.currentTimeMillis() - startTime < 2000) {
                currentDice1 = (1..6).random()
                currentDice2 = (1..6).random()
                delay(100)
            }
        }

        if (state.showResult) {
            currentDice1 = state.dice1
            currentDice2 = state.dice2
            showFinalResult = true
            delay(2000) // Show result for 2 seconds
            onClose()
        }
    }

    Dialog(onDismissRequest = onClose) {
        Column(
            modifier = Modifier
                .background(catanClay, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (diceState!!.isRolling)
                    "${diceState!!.rollingPlayerUsername ?: "Player"} is rolling..."
                else
                    "${diceState!!.rollingPlayerUsername ?: "Player"} rolled:",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiceImageDisplay(
                    value = currentDice1,
                    rolling = diceState!!.isRolling,
                    angle = angle
                )
                DiceImageDisplay(
                    value = currentDice2,
                    rolling = diceState!!.isRolling,
                    angle = angle
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showFinalResult) {
                Text(
                    text = "Total: ${currentDice1 + currentDice2}",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
fun DiceImageDisplay(value: Int, rolling: Boolean, angle: Float) {
    val imageRes = when (value) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "Dice $value",
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                rotationZ = if (rolling) angle else 0f
            }
    )
}