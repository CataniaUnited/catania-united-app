package com.example.cataniaunited.ui.trade

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanClayDark

@Composable
fun TradeMenuPopup(
    onDismiss: () -> Unit,
    tradeOffer: Pair<Map<TileType, Int>, Map<TileType, Int>>,
    onUpdateOffer: (TileType, Int) -> Unit,
    onUpdateTarget: (TileType, Int) -> Unit,
    onSubmit: () -> Unit
) {
    var tradeStep by remember { mutableIntStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .heightIn(max = 500.dp)
                .background(catanClay, RoundedCornerShape(16.dp))
                .border(2.dp, catanClayDark, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            BankTradeStep(
                tradeOffer = tradeOffer,
                onUpdateOffer = onUpdateOffer,
                onUpdateTarget = onUpdateTarget,
                onSubmit = onSubmit,
                onCancel = { tradeStep = 1 }
            )
        }
    }
}
