package com.yoloroy.psu.networking.labs.app.common

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState

@Composable
fun <T : Any> ChangeValueWindow(
    title: String,
    label: String,
    value: T,
    parseOrNull: (String) -> T?,
    update: (T) -> Unit,
    onClose: () -> Unit
) {
    val windowState = rememberWindowState(size = DpSize.Unspecified)
    Window(
        onCloseRequest = onClose,
        title = title,
        state = windowState
    ) {
        var error by remember { mutableStateOf(false) }
        var input by remember { mutableStateOf(value.toString()) }

        OutlinedTextField(
            value = input,
            onValueChange = { newValue ->
                input = newValue
                error = parseOrNull(newValue)?.let(update) == null
            },
            label = { Text(label) },
            isError = error
        )
    }
}
