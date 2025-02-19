package com.yoloroy.psu.networking.labs.algorithms

import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph
import kotlin.math.min

private const val UNIVERSAL_SPECIAL_NOT_NIL_INT = -1
const val INF = UNIVERSAL_SPECIAL_NOT_NIL_INT

@JvmInline
value class Experience(val matrix: List<List<Int>>) {

    constructor(size: Int) : this(List(size) { i -> List(size) { j -> if (i == j) 0 else INF } })

    fun appended(a: NetworkGraph.ImmutableNode, b: NetworkGraph.ImmutableNode, distance: Int): Experience {
        val matrix = matrix.mapTo(mutableListOf()) { it.toMutableList() }
        if (matrix[b.i][a.i] != INF) {
            matrix[b.i][a.i] = min(matrix[b.i][a.i], distance)
        } else {
            matrix[b.i][a.i] = distance
        }
        return Experience(matrix)
    }

    private operator fun <T> List<T>.set(index: Int, value: T) = mapIndexed { i, v -> if (i == index) value else v }
}