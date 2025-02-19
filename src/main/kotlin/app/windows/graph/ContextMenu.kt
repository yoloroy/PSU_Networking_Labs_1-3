package com.yoloroy.psu.networking.labs.app.windows.graph

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.yoloroy.psu.networking.labs.app.windows.graph.ContextMenuState.*
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

@Composable
fun rememberContextMenuViewModel(coordinatesState: State<NodesCoordinates>): ContextMenuViewModel {
    val coordinates by coordinatesState
    var state: ContextMenuState by remember { mutableStateOf(None) }

    return ContextMenuViewModel(
        state = state,
        openFor = { xy -> state = coordinates.at(xy)?.let { ForNode(it, xy) } ?: Common(xy) },
        hide = { state = None }
    )
}

data class ContextMenuViewModel(
    val state: ContextMenuState,
    val openFor: (xy: Offset) -> Unit,
    val hide: () -> Unit
)

sealed interface ContextMenuState {
    val status: DropdownMenuState.Status

    data object None : ContextMenuState {
        override val status = DropdownMenuState.Status.Closed
    }

    open class Common(offset: Offset) : ContextMenuState {
        override val status = DropdownMenuState.Status.Open(offset)
    }

    class ForNode(val node: NetworkGraph.MutableNode, offset: Offset) : Common(offset)
}

@Composable
fun ContextMenu(
    menu: ContextMenuViewModel,
    shortestPath: ShortestPath,
    update: () -> Unit,
    addNode: () -> Unit,
) {
    val state = remember(menu) { menu.state }
    val dropdownMenuState = remember(state) { DropdownMenuState(state.status) }

    val alsoHideMenu = fun(block: () -> Unit): () -> Unit = {
        block()
        menu.hide()
    }

    DropdownMenu(dropdownMenuState, onDismissRequest = { menu.hide() }) {
        (state as? ForNode)?.node?.let { node ->
            ContextMenuForNodeItems(node, shortestPath, update, alsoHideMenu)
        }
        Divider()
        (state as? Common)?.let {
            ContextMenuCommonItems(addNode)
        }
    }
}

@Composable
private fun ContextMenuForNodeItems(
    node: NetworkGraph.MutableNode,
    shortestPath: ShortestPath,
    update: () -> Unit,
    alsoHideMenu: (() -> Unit) -> () -> Unit
) {
    var toggle by remember { mutableStateOf(true) }

    IconToggleButton(
        checked = toggle,
        onCheckedChange = { toggle = it }
    ) {
        Icon(if (toggle) Icons.Default.Close else Icons.Default.ArrowDropDown, "dropdown")
    }

    if (!toggle) {
        return
    }

    DropdownMenuItem(alsoHideMenu { shortestPath.a = node.asImmutable() }) {
        Text("Установить как точку А")
    }
    DropdownMenuItem(alsoHideMenu { shortestPath.b = node.asImmutable() }) {
        Text("Установить как точку B")
    }
    DropdownMenuItem(alsoHideMenu { node.remove() }) {
        Text("Удалить узел")
    }
    if (node.others.isNotEmpty()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            ChooseNeighbourItem(node, update, alsoHideMenu)
        }
    }
}

@Composable
private fun ContextMenuCommonItems(addNode: () -> Unit) {
    DropdownMenuItem(addNode) {
        Text("Добавить узел")
    }
}

@Composable
private fun ChooseNeighbourItem(
    node: NetworkGraph.MutableNode,
    update: () -> Unit,
    alsoHideMenu: (() -> Unit) -> () -> Unit
) {
    var chosenNeighbour by remember {
        mutableStateOf(node.neighbours.firstOrNull() ?: node.others[0])
    }
    var value by remember(chosenNeighbour) { mutableStateOf(node[chosenNeighbour].toString()) }

    Text("Cвязь c ")
    NeighbourSpinner(node, chosenNeighbour) { chosenNeighbour = it }
    Text("=")
    OutlinedTextField(
        value = value,
        onValueChange = { value = it },
        label = if (value.toUIntOrNull() == null) ({ Text("Введено не число") }) else null,
        isError = value.toUIntOrNull() == null,
        trailingIcon = {
            IconButton(
                onClick = alsoHideMenu {
                    node[chosenNeighbour] = value.toUInt()
                    update()
                },
                enabled = value.toUIntOrNull() != null
            ) {
                Icon(Icons.Default.Check, "Применить")
            }
        }
    )
}

@Composable
private fun NeighbourSpinner(
    node: NetworkGraph.MutableNode,
    chosenNeighbour: NetworkGraph.MutableNode,
    chooseNeighbour: (NetworkGraph.MutableNode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TextButton(onClick = { expanded = true }) {
        Text("#${chosenNeighbour.id}")
    }
    DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
        for (neighbour in node.others) {
            DropdownMenuItem(onClick = { chooseNeighbour(neighbour) }) {
                Text("#${neighbour.id}")
            }
        }
    }
}
