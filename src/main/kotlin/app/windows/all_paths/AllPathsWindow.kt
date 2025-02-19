package com.yoloroy.psu.networking.labs.app.windows.all_paths

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import com.yoloroy.psu.networking.labs.app.windows.all_paths.AllPathsForMethodValue.Calculated
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph.ImmutableNode
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph.Node
import com.yoloroy.psu.networking.labs.algorithms.Path
import com.yoloroy.psu.networking.labs.algorithms.allPathsByDijkstra
import com.yoloroy.psu.networking.labs.algorithms.allPathsByFloydMarshall
import com.yoloroy.psu.networking.labs.app.common.CommonMenuBarItems
import com.yoloroy.psu.networking.labs.app.common.CommonMenuBarItemsParams
import com.yoloroy.psu.networking.labs.app.common.jetbrainsMonoFontFamily
import kotlin.time.Duration
import kotlin.time.measureTimedValue
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

@Composable
fun AllPathsWindow(
    nodes: List<ImmutableNode>,
    menuBarItemsParams: CommonMenuBarItemsParams.WithoutWindow,
    onCloseRequest: () -> Unit,
    graph: NetworkGraph
) = Window(onCloseRequest = onCloseRequest, title = "Все пути") {

    var methods by remember { mutableStateOf(emptyList<AllPathsForMethodValue>(), neverEqualPolicy()) }

    LaunchedEffect(nodes) {
        methods = listOf(
            run {
                val (paths, time) = measureTimedValue { allPathsByDijkstra(graph, nodes) }
                Calculated("Алгоритм Дейкстры (N * (N - 1)) раз", time, paths)
            },
            run {
                val (paths, time) = measureTimedValue { allPathsByFloydMarshall(graph) }
                Calculated("Алгоритм Флойда", time, paths)
            }
        )
    }

    MenuBar {
        CommonMenuBarItems(menuBarItemsParams.withWindow(window))
    }

    Row(modifier = Modifier.fillMaxSize()) {
        for (method in methods) {
            AllPathsForMethod(method, Modifier.weight(1f).fillMaxHeight())
        }
    }
}

@Composable
fun AllPathsForMethod(value: AllPathsForMethodValue, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            Text(
                text = value.methodName,
                modifier = Modifier.padding(16.dp)
            )
        }
        if (value is Calculated) {
            calculatedItems(value)
        }
    }
}

private fun LazyListScope.calculatedItems(value: Calculated) {
    item {
        val timeFormatted = value.time.toString().replace("us", "µs")
        Text(
            text = "Время рассчёта: $timeFormatted",
            fontFamily = jetbrainsMonoFontFamily,
            fontSize = 12.sp
        )
    }
    items(value.paths.toList()) { (headNode, paths) ->
        Text(
            text = "[${headNode.id}]:",
            textDecoration = TextDecoration.Underline,
            fontSize = 12.sp,
            fontFamily = jetbrainsMonoFontFamily,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        for (path in paths) {
            val formattedTransits = path.path.zipWithNext().joinToString("") { (a, b) -> " -|${a[b]}|-> [${b.id}]" }
            Text(
                text = "-> [${path.destination.id}], [${headNode.id}]$formattedTransits, Σ = ${path.distance}",
                fontFamily = jetbrainsMonoFontFamily,
                fontSize = 12.sp
            )
        }
    }
}

sealed class AllPathsForMethodValue(open val methodName: String) {
    data class NotCalculated(override val methodName: String): AllPathsForMethodValue(methodName)
    data class Calculated(
        override val methodName: String,
        val time: Duration,
        val paths: Map<Node, List<Path>>
    ) : AllPathsForMethodValue(methodName)
}
