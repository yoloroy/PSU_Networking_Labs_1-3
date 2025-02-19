@file:Suppress("NAME_SHADOWING")

package com.yoloroy.psu.networking.labs.app.windows.graph

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import com.yoloroy.psu.networking.labs.algorithms.dijkstraPathOrNull
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph.*

@Composable
fun rememberShortestPath(graph: NetworkGraph): ShortestPath {
    val aState = remember { mutableStateOf<ImmutableNode?>(null) }
    val bState = remember { mutableStateOf<ImmutableNode?>(null) }

    var a by remember { aState }
    var b by remember { bState }

    val path: List<ImmutableNode> = run path@{
        val a = a?.takeIf { it.exists() } ?: return@path emptyList()
        val b = b?.takeIf { it.exists() } ?: return@path emptyList()
        dijkstraPathOrNull(graph.matrix, a.i, b.i)?.map { graph.nodeByI(it).asImmutable() } ?: emptyList()
    }

    if (a?.exists() != true) {
        a = null
    }

    if (b?.exists() != true) {
        b = null
    }

    return ShortestPath(path, aState, bState)
}

data class ShortestPath(
    private val path: List<ImmutableNode>,
    private val aState: MutableState<ImmutableNode?>,
    private val bState: MutableState<ImmutableNode?>
): List<Node> by path {
    var a: ImmutableNode? by aState
    var b: ImmutableNode? by bState

    val length = path.ifEmpty { null }?.zipWithNext { a, b -> a[b] }?.sum()
}

@Composable
fun PathDescription(path: ShortestPath, modifier: Modifier = Modifier) {
    if (path.isEmpty()) {
        return
    }

    Box(modifier = modifier.background(Color.White, shape = CutCornerShape(bottomEnd = 16.dp))) {
        Column(modifier = Modifier.padding(12.dp)) {
            val chain ="[${path.first().id}]" + path.zipWithNext().joinToString("") { (a, b) -> " -|${a[b]}|-> [${b.id}]" }
            Text("Путь от [${path.first().id}] до [${path.last().id}] : $chain")
            Text("Суммарный путь = ${path.zipWithNext { a, b -> a[b] }.sum()}")
        }
    }
}
