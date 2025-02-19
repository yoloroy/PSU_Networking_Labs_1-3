package com.yoloroy.psu.networking.labs.algorithms

import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

fun floydMarshallAllPaths(matrix: List<List<UInt>>): Pair<List<List<UInt>>, List<List<Int>>> {
    val distances = matrix.map { it.toMutableList() }
    val routes = matrix.mapTo(mutableListOf()) { it.mapTo(mutableListOf()) { -1 } }

    for (z in distances.indices) {
        for (y in distances.indices) {
            for (x in distances.indices) {
                if (distances[y][z] == 0U || distances[z][x] == 0U) {
                    continue
                }

                val distance = distances[y][z] + distances[z][x]
                if (distances[y][x] > distance || distances[y][x] == 0U) {
                    distances[y][x] = distance
                    routes[y][x] = z
                }
            }
        }
    }

    return distances to routes
}

fun allPathsByFloydMarshall(graph: NetworkGraph): Map<NetworkGraph.Node, List<Path>> = buildMap(graph.matrix.size) {
    val (distances, links) = floydMarshallAllPaths(graph.matrix)
    val nodes = graph.matrix.indices.map { i -> graph.nodeByI(i) }

    // TODO turn into something like `return generateSequence(b) { links[it] }.plus(a).toList().reversed()`
    fun retrievePath(originI: Int, destinationI: Int): List<Int>? {
        if (distances[originI][destinationI] == 0U) {
            return null
        }

        var destI = destinationI
        return buildList {
            add(originI)
            do {
                add(destI)
                destI = links[originI][destI]
            } while (destI != -1 && originI != destI)
        }.reversed()
    }

    for (originI in graph.matrix.indices) {
        this[nodes[originI]] = (graph.matrix.indices - originI).mapNotNull { destinationI ->
            val path = retrievePath(originI, destinationI) ?: return@mapNotNull null
            println(originI)
            println(destinationI)
            println(path)
            Path(nodes[originI], nodes[destinationI], path.map(nodes::get), distances[originI][destinationI])
        }
    }
}
