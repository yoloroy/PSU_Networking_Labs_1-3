package com.yoloroy.psu.networking.labs.app

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.window.application
import com.yoloroy.psu.networking.labs.app.windows.adjacency_matrix.AdjacencyMatrixWindow
import com.yoloroy.psu.networking.labs.app.common.CommonMenuBarItemsParams
import com.yoloroy.psu.networking.labs.app.windows.graph.GraphWindow
import com.yoloroy.psu.networking.labs.app.windows.all_paths.AllPathsWindow
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

fun app() = application {
    var graphAndNodesLoader by remember {
        mutableStateOf(NetworkGraph.build(
            listOf(
                listOf(0U, 2U, 3U, 0U),
                listOf(2U, 0U, 5U, 6U),
                listOf(3U, 5U, 0U, 0U),
                listOf(0U, 6U, 0U, 0U)
            )
        ))
    }
    val (graph, loadNodes) = graphAndNodesLoader

    val nodesState: MutableState<List<StateNode>> = remember { mutableStateOf(listOf(), neverEqualPolicy()) }
    if (nodesState.value.any { !it.exists() }) {
        nodesState.value = nodesState.value.filter { it.exists() }
        return@application
    }

    val coordinatesState = remember { mutableStateOf(mapOf<NetworkGraph.MutableNode, Offset>()) }

    var openAdjacencyMatrixWindow by remember { mutableStateOf(false) }
    var openAlgorithmsComparisonWindow by remember { mutableStateOf(false) }

    val commonMenuBarItemsParams by remember {
        derivedStateOf {
            CommonMenuBarItemsParams.WithoutWindow(
                nodes = nodesState.value.map { it.asImmutable() },
                load = {
                    NetworkGraph.build(it).let { (g, loadNodes) ->
                        graphAndNodesLoader = (g to loadNodes)
                        coordinatesState.value = emptyMap()
                        nodesState.value = loadNodes.map { StateNode(nodesState, it) }
                    }
                },
                onOpenAdjacencyMatrixWindow = { openAdjacencyMatrixWindow = true },
                onOpenAlgorithmsComparisonWindow = { openAlgorithmsComparisonWindow = true }
            )
        }
    }

    LaunchedEffect(Unit) {
        nodesState.value = loadNodes.map { StateNode(nodesState, it) }
    }

    MaterialTheme {
        GraphWindow(
            graph = graph,
            nodes = nodesState.value,
            coordinatesState = coordinatesState,
            addNode = { nodesState.value += StateNode(nodesState, graph.append()) },
            update = { nodesState.value = nodesState.value },
            onCloseRequest = { exitApplication() },
            menuBarItemsParams = commonMenuBarItemsParams
        )

        if (openAdjacencyMatrixWindow) {
            AdjacencyMatrixWindow(
                update = { nodesState.value = nodesState.value },
                hide = { openAdjacencyMatrixWindow = false },
                menuBarItemsParams = commonMenuBarItemsParams,
                graph = graph
            )
        }
        if (openAlgorithmsComparisonWindow) {
            AllPathsWindow(
                nodes = nodesState.value.map { it.asImmutable() },
                onCloseRequest = { openAlgorithmsComparisonWindow = false },
                menuBarItemsParams = commonMenuBarItemsParams,
                graph = graph
            )
        }
    }
}