package com.example.cataniaunited.ui.lobby

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun UsernameInputDialog(
    username: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var changedUsername by remember { mutableStateOf(username) }
    var showUsernameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        title = { Text("Change username") },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = changedUsername,
                    onValueChange = {
                        changedUsername = it
                        showUsernameError = false
                    },
                    label = { Text("Please enter username") },
                    singleLine = true,
                    isError = showUsernameError,
                    supportingText = {
                        if (showUsernameError) {
                            Text(
                                "New username may not be empty.",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (changedUsername.isNotBlank()) {
                        onConfirm(changedUsername)
                    } else {
                        showUsernameError = true
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
            ) {
                Text("Cancel", color = Color.Black)
            }
        },
        modifier = Modifier
            .wrapContentWidth()
            .wrapContentHeight()
    )
}