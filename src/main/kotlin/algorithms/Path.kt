package com.yoloroy.psu.networking.labs.algorithms

import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

data class Path(
    val origin: NetworkGraph.Node,
    val destination: NetworkGraph.Node,
    val path: List<NetworkGraph.Node>,
    val distance: UInt
)
