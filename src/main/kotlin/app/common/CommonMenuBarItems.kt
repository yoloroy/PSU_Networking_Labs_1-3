package com.yoloroy.psu.networking.labs.app.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.MenuBarScope
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph
import com.yoloroy.psu.networking.labs.network_graph.stored.STORED_GRAPH_EXTENSION
import com.yoloroy.psu.networking.labs.network_graph.stored.loadGraphFromFile
import com.yoloroy.psu.networking.labs.network_graph.stored.saveGraphToFile

data class CommonMenuBarItemsParams(
    val window: ComposeWindow,
    val nodes: List<NetworkGraph.ImmutableNode>,
    val load: (List<List<UInt>>) -> Unit,
    val onOpenAdjacencyMatrixWindow: (() -> Unit)? = null,
    val onOpenAlgorithmsComparisonWindow: (() -> Unit)? = null
) {
    data class WithoutWindow(
        val nodes: List<NetworkGraph.ImmutableNode>,
        val load: (List<List<UInt>>) -> Unit,
        val onOpenAdjacencyMatrixWindow: (() -> Unit)? = null,
        val onOpenAlgorithmsComparisonWindow: (() -> Unit)? = null
    ) {
        fun withWindow(window: ComposeWindow) =
            CommonMenuBarItemsParams(window, nodes, load, onOpenAdjacencyMatrixWindow, onOpenAlgorithmsComparisonWindow)
    }
}

@Composable
fun MenuBarScope.CommonMenuBarItems(params: CommonMenuBarItemsParams) =
    CommonMenuBarItems(params.window, params.nodes, params.load, params.onOpenAdjacencyMatrixWindow, params.onOpenAlgorithmsComparisonWindow)

@Composable
fun MenuBarScope.CommonMenuBarItems(
    window: ComposeWindow,
    nodes: List<NetworkGraph.ImmutableNode>,
    load: (List<List<UInt>>) -> Unit,
    onOpenAdjacencyMatrixWindow: (() -> Unit)? = null,
    onOpenAlgorithmsComparisonWindow: (() -> Unit)? = null
) {
    val onSave = fun() {
        val file = openSaveFileDialog(window, "Сохранить граф", listOf(STORED_GRAPH_EXTENSION), false).firstOrNull()
        if (file != null) {
            saveGraphToFile(file, nodes)
        }
    }

    val onLoad = fun() {
        val file = openLoadFileDialog(window, "Загрузить граф", listOf(STORED_GRAPH_EXTENSION), false).firstOrNull()
        if (file != null) {
            load(loadGraphFromFile(file))
        }
    }

    Menu("Файл") {
        Item("Сохранить", onClick = onSave)
        Item("Загрузить", onClick = onLoad)
    }
    Menu("Инструменты") {
        onOpenAdjacencyMatrixWindow?.let {
            Item("Открыть матрицу смежности", onClick = it)
        }
        onOpenAlgorithmsComparisonWindow?.let {
            Item("Открыть окно сравнения алгоритмов нахождения путей", onClick = it)
        }
    }
}
