package com.example.cataniaunited.ui.dice

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.cataniaunited.R
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DiceRollerPopup(
    onDiceRolled: () -> Unit,
    onClose: () -> Unit,
    dice1Result: Int?,
    dice2Result: Int?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Local state for animation
    var dice1 by remember { mutableStateOf(1) }
    var dice2 by remember { mutableStateOf(1) }
    var rolling by remember { mutableStateOf(false) }
    var totalValue by remember { mutableStateOf(2) }

    val rotation = rememberInfiniteTransition()
    val angle by rotation.animateFloat(
        initialValue = 0f,
        targetValue = if (rolling) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing)
        )
    )

    // Start rolling when popup opens
    LaunchedEffect(Unit) {
        rolling = true
        // If no server values yet, simulate rolling animation
        if (dice1Result == null || dice2Result == null) {
            onDiceRolled()
        }
    }

    // Handle server results
    LaunchedEffect(dice1Result, dice2Result) {
        if (dice1Result != null && dice2Result != null) {
            // Wait a moment to show rolling animation
            delay(1000)
            dice1 = dice1Result
            dice2 = dice2Result
            totalValue = dice1Result + dice2Result
            rolling = false
            vibratePhone(context)
            delay(2000) // Show results for 2 seconds before closing
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
                text = if (rolling) "Rolling Dice!" else "Dice Result!",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiceImage(dice1, rolling, angle)
                DiceImage(dice2, rolling, angle)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Total: $totalValue", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    rolling = true
                    onDiceRolled()
                },
                enabled = !rolling,
                colors = ButtonDefaults.buttonColors(containerColor = catanGold)
            ) {
                Text("Roll Dice")
            }
        }
    }
}

@Composable
private fun DiceImage(value: Int, rolling: Boolean, angle: Float) {
    val displayValue = if (rolling) {
        remember { (1..6).random() }
    } else {
        value
    }

    Image(
        painter = painterResource(id = getDiceImage(displayValue)),
        contentDescription = "Dice $displayValue",
        modifier = Modifier
            .size(100.dp)
            .graphicsLayer {
                rotationZ = if (rolling) angle else 0f
            }
    )
}

private fun getDiceImage(value: Int): Int {
    return when (value) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
}

private fun vibratePhone(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
}