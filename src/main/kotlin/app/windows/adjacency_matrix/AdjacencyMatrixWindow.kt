package com.yoloroy.psu.networking.labs.app.windows.adjacency_matrix

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import com.sunnychung.lib.android.composabletable.ux.Table
import com.yoloroy.psu.networking.labs.app.common.CommonMenuBarItems
import com.yoloroy.psu.networking.labs.app.common.CommonMenuBarItemsParams
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

@Composable
fun AdjacencyMatrixWindow(
    menuBarItemsParams: CommonMenuBarItemsParams.WithoutWindow,
    update: () -> Unit,
    hide: () -> Unit,
    graph: NetworkGraph
) = Window(onCloseRequest = hide, title = "Матрица") {
    var xyToChange by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val onSetDistanceCallback = fun(distance: UInt): () -> Unit = {
        val (x, y) = xyToChange!!
        xyToChange = null
        graph.setByIndex(x, y, distance)
        update()
    }

    MenuBar {
        CommonMenuBarItems(menuBarItemsParams.withWindow(window))
    }

    Table(
        rowCount = graph.size + 1,
        columnCount = graph.size + 1,
        modifier = Modifier.fillMaxSize()
    ) { x, y ->
        val header = x == 0 || y == 0
        if (header) {
            val content = when {
                y != 0 -> "[${y - 1}]:"
                x != 0 -> "[${x - 1}]:"
                else -> ""
            }
            Text(content, modifier = Modifier.padding(8.dp))
        }
        else {
            TextButton({ xyToChange = Pair(y - 1, x - 1) }) {
                Text(graph.matrix[y - 1][x - 1].toString())
            }
        }
    }

    val (x, y) = xyToChange ?: return@Window
    Dialog(onDismissRequest = { xyToChange = null }) {
        var value by remember { mutableStateOf(graph.matrix[y][x].toString()) }

        Surface {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = if (value.toUIntOrNull() == null) ({ Text("Введено не число") }) else null,
                isError = value.toUIntOrNull() == null,
                leadingIcon = { Text(" |$x, $y| = ") },
                trailingIcon = {
                    IconButton(
                        onClick = value.toUIntOrNull()?.let { onSetDistanceCallback(it) } ?: {},
                        enabled = value.toUIntOrNull() != null
                    ) {
                        Icon(Icons.Default.Check, "Применить")
                    }
                }
            )
        }
    }
}
