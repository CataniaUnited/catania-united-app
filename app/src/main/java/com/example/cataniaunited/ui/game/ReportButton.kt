package com.example.cataniaunited.ui.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cataniaunited.R
import androidx.compose.foundation.Image
import androidx.compose.ui.tooling.preview.Preview
import com.example.cataniaunited.ui.theme.catanGold

@Composable
fun ReportButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = catanGold,
        border = BorderStroke(3.dp, catanGold),
        shadowElevation = 10.dp,
        modifier = modifier
            .size(40.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(R.drawable.report_icon),
                contentDescription = "Report Player",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewReportButton() {
    ReportButton(onClick = {})
}
