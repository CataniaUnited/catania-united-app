package com.example.cataniaunited.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cataniaunited.data.model.PlayerInfo
import com.example.cataniaunited.ui.theme.catanClayLight
import com.example.cataniaunited.ui.theme.catanRessourceBar
import androidx.compose.foundation.clickable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.cataniaunited.ui.theme.CataniaUnitedTheme



@Composable
fun ReportPlayerListPopup(
    players: List<PlayerInfo>,
    onReport: (PlayerInfo) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    Card(
        colors = CardDefaults.cardColors(containerColor = catanClayLight),
        modifier = modifier
            .padding(top = 4.dp)
            .width(260.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Who do you suspect of cheating?",
                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontSize = 18.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                thickness = 2.dp,
                color = catanRessourceBar.copy(alpha = 0.5f)
            )

            // scroll for more than 3 players
            val showScroll = players.size > 3
            val listHeight = if (showScroll) 132.dp else Dp.Unspecified

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .then(
                        if (showScroll) Modifier.heightIn(max = listHeight) else Modifier
                    )
                    .verticalScroll(rememberScrollState())
            ) {
                players.forEachIndexed { idx, player ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedIndex = idx }
                    ) {
                        Checkbox(
                            checked = selectedIndex == idx,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = catanRessourceBar,
                                uncheckedColor = Color.White,
                                checkmarkColor = Color.White
                            ),
                            enabled = false
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = player.username ?: player.id,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }




            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (selectedIndex in players.indices) {
                        onReport(players[selectedIndex])
                        onDismiss()
                    }
                },
                enabled = selectedIndex in players.indices,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedIndex in players.indices) catanRessourceBar else Color.Gray
                )
            ) {
                Text("Report", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}


@Preview
@Composable
fun PreviewReportPlayerListPopup() {
    val players = listOf(
        PlayerInfo(id = "1", username = "Alice"),
        PlayerInfo(id = "2", username = "Bob"),
        PlayerInfo(id = "3", username = "Charlie"),
        PlayerInfo(id = "4", username = "Diana"),
        PlayerInfo(id = "5", username = "Eva")
    )
    CataniaUnitedTheme {
        ReportPlayerListPopup(
            players = players,
            onReport = {},
            onDismiss = {}
        )
    }
}

