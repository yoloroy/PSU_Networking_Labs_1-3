package com.yoloroy.psu.networking.labs.network_graph

class BiMapIdsIndices(initialSize: Int) {
    private val ids = MutableList(initialSize) { it }

    val i: Map<Int, Int> get() = (ids zip ids.indices).associate { it }
    val id: Map<Int, Int> get() = (ids.indices zip ids).associate { it }

    /**
     * @return Pair of `id to index`
     */
    fun appendIdAndI(): Pair<Int, Int> {
        val nextId = (ids.maxOrNull() ?: 0) + 1
        ids += nextId
        return nextId to ids.lastIndex
    }

    fun removeById(id: Int) {
        ids.remove(id)
        println(ids.withIndex().toList())
    }
}
