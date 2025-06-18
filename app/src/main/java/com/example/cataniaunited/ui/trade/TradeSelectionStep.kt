package com.example.cataniaunited.ui.trade

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.R


@Composable
fun TradeSelectionStep(onTradeWithBank: () -> Unit, onTradeWithPlayer: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Select Trade Type", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TradeTypeButton(
                iconRes = R.drawable.bank_trade_icon,
                onClick = onTradeWithBank,
                enabled = true,
                contentDescription = "bank_trade"
            )
            TradeTypeButton(
                iconRes = R.drawable.player_trade_icon,
                onClick = onTradeWithPlayer,
                enabled = false,
                contentDescription = "player_trade"
            )
        }
    }
}

