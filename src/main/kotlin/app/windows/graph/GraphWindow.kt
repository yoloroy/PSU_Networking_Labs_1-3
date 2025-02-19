package com.yoloroy.psu.networking.labs.app.windows.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import com.yoloroy.psu.networking.labs.algorithms.Experience
import com.yoloroy.psu.networking.labs.app.common.*
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GraphWindow(
    nodes: List<NetworkGraph.MutableNode>,
    coordinatesState: MutableState<NodesCoordinates>,
    menuBarItemsParams: CommonMenuBarItemsParams.WithoutWindow,
    graph: NetworkGraph,
    addNode: () -> Unit,
    update: () -> Unit,
    onCloseRequest: () -> Unit
) = WindowWithBox(onCloseRequest = onCloseRequest, title = "Граф") {

    val PACKET_VECTOR = rememberVectorPainter(Icons.Default.Email) // TODO find a way to make it global constant

    if (!nodes.all { it.exists() }) {
        return@WindowWithBox
    }

    var scale by remember { mutableStateOf(1f) }
    if (scale <= 0f) {
        scale = 0.1f
    }
    var scaleDialogOpen by remember { mutableStateOf(false) }

    var coordinates by coordinatesState
    val path = rememberShortestPath(graph)
    val contextMenu = rememberContextMenuViewModel(coordinatesState)

    var experience by remember(nodes.size) { mutableStateOf(Experience(nodes.size)) }
    var experienceDialogOpen by remember { mutableStateOf(false) }
    var packetsConfig by remember { mutableStateOf(PacketsConfig(null, null, 1, false)) }
    var packetsCountDialogOpen by remember { mutableStateOf(false) }
    val packets = kotlin.run packets@ {
        val a = path.a ?: return@packets null
        val b = path.b ?: return@packets null
        packetsConfig.takeIfConfigured()?.run {
            rememberPacketsState(a, b, count, repeating, algorithm, (1.0 / 30).seconds, 1f / 10, mode) {
                experience = experience.appended(it.origin, it.nextLocation, it.topologicalDistance)
                experience = experience.appended(it.location, it.nextLocation, 1)
            }
        }
    }

    MenuBar {
        CommonMenuBarItems(menuBarItemsParams.withWindow(window))
        PacketsMenu(
            config = packetsConfig,
            update = { packetsConfig = it },
            openPacketsCountDialog = { packetsCountDialogOpen = true },
            openExperienceDialog = { experienceDialogOpen = true }
        )
        Menu("Утилиты") {
            Item("Масштаб: x%.2f".format(scale)) {
                scaleDialogOpen = true
            }
            Item("Значения масштаба отличные от 1.0 приводят к уменьшению производительности!", enabled = false) {
            }
        }
    }

    ContextMenu(
        menu = contextMenu,
        shortestPath = path,
        update = update,
        addNode = addNode
    )

    if (scaleDialogOpen) {
        ChangeValueWindow(
            title = "Масштаб",
            label = "Множитель",
            value = scale,
            parseOrNull = String::toFloatOrNull,
            update = { scale = it },
            onClose = { scaleDialogOpen = false }
        )
    }

    if (packetsCountDialogOpen) {
        PacketsCountDialog(
            config = packetsConfig,
            update = { packetsConfig = it },
            onClose = { packetsCountDialogOpen = false }
        )
    }

    if (experienceDialogOpen) {
        ExperienceDialog(
            experience = experience,
            onClose = { experienceDialogOpen = false }
        )
    }

    PathDescription(path, modifier = Modifier.align(Alignment.TopStart))

    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .then(rememberNodeGrabbingBehaviour(coordinatesState))
            .pointerInput(Unit) {
                detectTapGestures(
                    matcher = PointerMatcher.mouse(PointerButton.Secondary),
                    onTap = contextMenu.openFor
                )
            }
    ) {
        val notPositionedNodes = nodes.filter { it !in coordinates }
        if (notPositionedNodes.isNotEmpty()) {
            coordinates += notPositionedNodes.associateWith { size.randomOffsetInside() }
            return@Canvas
        }

        drawLines(nodes, coordinates, scale)
        drawLines(path, coordinates, scale)
        drawShortestPathResultLine(path, coordinates, textMeasurer, scale)
        drawPoints(nodes, coordinates, path, scale)
        if (packets != null) {
            drawPackets(
                packets,
                coordinates,
                PACKET_VECTOR,
                scale,
                packetsConfig.run { count.takeIf { mode == PacketsMode.Channel } }
            )
        }
        drawLinesDistances(nodes, coordinates, textMeasurer, scale)
        drawPointsNames(nodes, coordinates, textMeasurer, scale)
    }
}
