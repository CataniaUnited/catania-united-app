package com.example.cataniaunited.ui.game_end

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.ui.theme.appTypography
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanClayDark
import com.example.cataniaunited.ui.theme.catanClayLight
import com.example.cataniaunited.ui.theme.catanGold
import com.example.cataniaunited.ui.theme.catanGoldLight
//
@Composable
fun GameEndScreen(
    currentPlayerInfo: PlayerInfo,
    winner: PlayerInfo,
    leaderboard: List<PlayerInfo>,
    onReturnToMenu: () -> Unit,
) {

    val isWinner = currentPlayerInfo.username == winner.username

    Box(
        modifier = Modifier
            .widthIn(max = 800.dp)
            .heightIn(max = 370.dp)
            .border(4.5.dp, catanClayDark, RoundedCornerShape(12.dp))
            .background(catanClayLight, RoundedCornerShape(12.dp)),

        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (isWinner) {
                Text(
                    text = "VICTORY!",
                    style = appTypography.titleLarge,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = catanGoldLight
                )
                Text(
                    text = "Congratulations, ${currentPlayerInfo.username}!",
                    style = appTypography.bodyLarge,
                    color = catanGold
                )
            } else {

                Text(
                    text = "DEFEAT",
                    style = appTypography.titleLarge,
                    fontSize = 35.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                val funnyMessage = when {
                    currentPlayerInfo.victoryPoints >= 8 -> "So close! You were just a step away."
                    currentPlayerInfo.victoryPoints >= 5 -> "A respectable performance."
                    else -> "At least you built a nice long road?"
                }
                Text(
                    text = funnyMessage,
                    style = appTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = catanClayDark
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = R.drawable.podium),
                contentDescription = "Podium",
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(220.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // The rest of your code for the leaderboard remains the same...
            leaderboard.take(3).forEachIndexed { index, player ->
                val rankLabel = when (index) {
                    0 -> "1st"
                    1 -> "2nd"
                    2 -> "3rd"
                    else -> ""
                }

                Row(
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .height(60.dp)
                        .width(300.dp)
                        .border(2.dp, catanGold, RoundedCornerShape(8.dp))
                        .background(catanClayLight),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxHeight()
                            .background(catanGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rankLabel,
                            style = appTypography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(catanClayLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.username!!,
                            style = appTypography.bodyLarge.copy(fontSize = 17.sp),
                            color = catanGoldLight
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(catanGold)
                    )

                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .fillMaxHeight()
                            .background(catanClayLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${player.victoryPoints} VP",
                            style = appTypography.bodyLarge,
                            color = catanGoldLight
                        )
                    }
                }
            }
        }

        Button(
            onClick = onReturnToMenu,
            colors = ButtonDefaults.buttonColors(containerColor = catanGold),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, catanClay),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .shadow(elevation = 5.dp, shape = RoundedCornerShape(12.dp))
        ) {
            Text(
                text = "Return to menu",
                style = appTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        }
    }
}

@Preview(name = "Winner's View", showBackground = true)
@Composable
fun PreviewGameWinScreen_Winner() {
    val winner = PlayerInfo("2", "Nassir", "#0000FF", false, false, victoryPoints = 10)
    GameEndScreen(
        currentPlayerInfo = winner, // Current player IS the winner
        winner = winner,
        leaderboard = listOf(
            winner,
            PlayerInfo("1", "Mia", "#FF0000",false, false, victoryPoints = 9),
            PlayerInfo("3", "Jean", "#D4AF37",false, false, victoryPoints =  7)
        ),
        onReturnToMenu = {},
    )
}

@Preview(name = "Loser's View", showBackground = true)
@Composable
fun PreviewGameWinScreen_Loser() {
    val currentPlayer = PlayerInfo("1", "Mia", "#FF0000",false, false, victoryPoints = 9)
    val winner = PlayerInfo("2", "Nassir", "#0000FF", false, false, victoryPoints = 10)
    GameEndScreen(
        currentPlayerInfo = currentPlayer, // Current player IS NOT the winner
        winner = winner,
        leaderboard = listOf(
            winner,
            currentPlayer,
            PlayerInfo("3", "Jean", "#D4AF37",false, false, victoryPoints =  7)
        ),
        onReturnToMenu = {},
    )
}