package com.yoloroy.psu.networking.labs.algorithms

import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

@Suppress("LocalVariableName")
fun dijkstraPathOrNull(matrix: List<List<UInt>>, a: Int, b: Int): List<Int>? {
    val links = mutableMapOf<Int, Int>()

    val `not checked`: MutableSet<Int> = matrix.indices.toMutableSet().apply { remove(a) }
    val `distances to a`: MutableMap<Int, UInt> = matrix[a].withIndex()
        .filter { it.value != 0U }
        .associateTo(mutableMapOf()) { it.index to it.value }

    while (`not checked`.isNotEmpty()) {
        val i = `not checked`
            .filter { `distances to a`[it] != null }
            .minByOrNull { `distances to a`[it]!! }
            ?: return null
        `not checked`.remove(i)

        for ((node, distance) in matrix[i].withIndex()) {
            if (distance == 0U) {
                continue
            }

            val `candidate distance` = distance + (`distances to a`[i] ?: continue)
            val `distance node to a` = `distances to a`[node]
            if (`distance node to a` == null || `candidate distance` < `distance node to a`) {
                `distances to a`[node] = `candidate distance`
                links[node] = i
            }
        }

        if (i == b) {
            break
        }
    }

    return generateSequence(b) { links[it] }.plus(a).toList().reversed()
}

fun allPathsByDijkstra(graph: NetworkGraph, nodes: List<NetworkGraph.ImmutableNode>): Map<NetworkGraph.Node, List<Path>> = buildMap(nodes.size) {
    for (origin in nodes) {
        this[origin] = origin.others.mapNotNull { destination ->
            val pathInIndices = dijkstraPathOrNull(graph.matrix, origin.i, destination.i) ?: return@mapNotNull null
            val path = pathInIndices.map { i -> graph.nodeByI(i).Immutable() }
            Path(origin, destination, path, path.zipWithNext { a, b -> a[b] }.sum())
        }
    }
}
