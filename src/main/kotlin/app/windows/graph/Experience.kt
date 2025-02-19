package com.yoloroy.psu.networking.labs.app.windows.graph

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.sunnychung.lib.android.composabletable.ux.Table
import com.yoloroy.psu.networking.labs.algorithms.Experience
import com.yoloroy.psu.networking.labs.algorithms.INF

@Composable
fun ExperienceDialog(experience: Experience, onClose: () -> Unit) {
    val matrix = experience.matrix

    Window(
        onCloseRequest = onClose,
        title = "Опыт",
    ) {
        Table(
            rowCount = matrix.size + 1,
            columnCount = matrix.size + 1
        ) { x, y ->
            val i = y - 1
            val j = x - 1
            val header = x == 0 || y == 0
            if (header) {
                val content = when {
                    y != 0 -> "[$i]:"
                    x != 0 -> "[$j]:"
                    else -> ""
                }
                Text(content, modifier = Modifier.padding(8.dp))
            } else {
                val v = matrix[i][j]
                Text(if (v == INF) "∞" else v.toString())
            }
        }
    }
}
