package com.yoloroy.psu.networking.labs.app.windows.graph

import androidx.compose.ui.geometry.Offset
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

private const val NODE_ACTION_RADIUS = 12f

typealias NodesCoordinates = Map<NetworkGraph.MutableNode, Offset>

fun NodesCoordinates.byId(id: Int) = filterKeys { it.id == id }.toList().single()

fun NodesCoordinates.at(xy: Offset) = this
    .filterValues { (it - xy).getDistance() <= NODE_ACTION_RADIUS }
    .minByOrNull { (_, v) -> (v - xy).getDistance() }?.key
