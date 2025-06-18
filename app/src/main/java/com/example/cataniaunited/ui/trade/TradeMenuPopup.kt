package com.example.cataniaunited.ui.trade

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cataniaunited.R
import com.example.cataniaunited.data.model.TileType
import com.example.cataniaunited.ui.theme.catanClay
import com.example.cataniaunited.ui.theme.catanClayDark
import com.example.cataniaunited.ui.theme.catanGold
import com.example.cataniaunited.ui.theme.catanGoldLight

@Composable
fun TradeMenuPopup(
    onDismiss: () -> Unit,
    tradeOffer: Pair<Map<TileType, Int>, Map<TileType, Int>>,
    onUpdateOffer: (TileType, Int) -> Unit,
    onUpdateTarget: (TileType, Int) -> Unit,
    onSubmit: () -> Unit
) {
    var tradeStep by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .heightIn(max = 500.dp) // Set a max height to contain the dialog
                .background(catanClay, RoundedCornerShape(16.dp))
                .border(2.dp, catanClayDark, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            when (tradeStep) {
                1 -> TradeSelectionStep(
                    onTradeWithBank = { tradeStep = 2 },
                    onTradeWithPlayer = { /* Disabled for now */ }
                )
                2 -> BankTradeStep(
                    tradeOffer = tradeOffer,
                    onUpdateOffer = onUpdateOffer,
                    onUpdateTarget = onUpdateTarget,
                    onSubmit = onSubmit,
                    onBack = { tradeStep = 1 }
                )
            }
        }
    }
}
