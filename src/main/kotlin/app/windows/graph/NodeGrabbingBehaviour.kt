package com.yoloroy.psu.networking.labs.app.windows.graph

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph.MutableNode

@Composable
fun rememberNodeGrabbingBehaviour(
    coordinatesState: MutableState<NodesCoordinates>
): Modifier {
    var coordinates by coordinatesState
    var grabbedNode: MutableNode? by remember { mutableStateOf(null) }

    return Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = onDragStart@ { xy ->
                grabbedNode = coordinates.at(xy) ?: grabbedNode ?: return@onDragStart
            },
            onDrag = onDrag@ { change, _ ->
                val node = grabbedNode ?: return@onDrag
                coordinates = coordinates.withPut(node, change.position)
            },
            onDragEnd = {
                grabbedNode = null
            },
            onDragCancel = {
                grabbedNode = null
            }
        )
    }
}

private fun <K, V> Map<K, V>.withPut(key: K, value: V) = filterKeys { it != key }.plus(key to value)
