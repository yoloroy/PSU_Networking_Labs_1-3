package com.yoloroy.psu.networking.labs.network_graph.stored

import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph
import java.io.File

fun saveGraphToFile(file: File, nodes: List<NetworkGraph.ImmutableNode>) {
    val string = nodes.joinToString("\n") { a ->
        a.neighboursDistances.toList().joinToString(",") { (b, distance) ->
            "${b.i}:$distance"
        }
    }

    file.delete()
    file.writeText(string)
}