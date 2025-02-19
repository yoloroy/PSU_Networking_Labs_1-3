package com.yoloroy.psu.networking.labs.network_graph

class NetworkGraph(val matrix: MutableList<MutableList<UInt>>) {

    private val keys = BiMapIdsIndices(matrix.size)

    val size: Int get() = matrix.size

    companion object {
        fun build(adjacencyMatrix: List<List<UInt>>): Pair<NetworkGraph, List<LocalNode>> {
            val graph = NetworkGraph(
                adjacencyMatrix.mapTo(mutableListOf(), List<UInt>::toMutableList)
            )
            return graph to List(adjacencyMatrix.size) { i -> graph.LocalNode(i) }
        }
    }

    fun nodeByI(i: Int) = LocalNode(keys.id[i]!!)

    operator fun get(idA: Int, idB: Int): UInt = matrix[keys.i[idA]!!][keys.i[idB]!!]

    operator fun set(idA: Int, idB: Int, distance: UInt) {
        matrix[keys.i[idA]!!][keys.i[idB]!!] = distance
        matrix[keys.i[idB]!!][keys.i[idA]!!] = distance
    }

    fun setByIndex(iA: Int, iB: Int, distance: UInt) = set(keys.id[iA]!!, keys.id[iB]!!, distance)

    fun remove(id: Int) {
        val i = keys.i[id]!!

        matrix.removeAt(i)
        for (row in matrix) {
            row.removeAt(i)
        }

        keys.removeById(id)
    }

    operator fun contains(id: Int) = id in keys.i

    fun append(): LocalNode {
        val (id, _) = keys.appendIdAndI()
        for (row in matrix) {
            row += 0U
        }
        matrix += MutableList(matrix[0].size) { 0U }
        return LocalNode(id)
    }

    interface OtherNodesAccessor<T : Node> {
        val others: List<T>
        val neighbours: List<T>
        val neighboursDistances: Map<T, UInt>
    }

    interface Node : Comparable<Node> {
        val id: Int
        val i: Int

        operator fun get(other: Node): UInt

        fun exists(): Boolean

        override fun equals(other: Any?): Boolean

        override fun hashCode(): Int
    }

    interface ImmutableNode : Node, OtherNodesAccessor<ImmutableNode>

    interface MutableNode : Node, OtherNodesAccessor<MutableNode> {
        fun remove()

        operator fun set(other: Node, distance: UInt)

        fun asImmutable(): ImmutableNode
    }

    inner class LocalNode(override val id: Int) : MutableNode {
        override val i get() = keys.i[id]!!

        override val others get() = matrix.indices.filter { it != i }.map { LocalNode(keys.id[it]!!) }

        override val neighbours get() = others.filter { this[it] != 0U }

        override val neighboursDistances: Map<MutableNode, UInt> get() = neighbours.associateWith { this[it] }

        override fun remove() = remove(id)

        override operator fun get(other: Node) = get(id, other.id)

        override operator fun set(other: Node, distance: UInt) = set(id, other.id, distance)

        override fun exists() = (id in this@NetworkGraph)

        override fun compareTo(other: Node) = id.compareTo(other.id)

        override fun equals(other: Any?) = other is Node && this.id == other.id

        override fun hashCode() = id

        override fun asImmutable() = Immutable()

        inner class Immutable : Node by this, ImmutableNode {
            override val others get() = this@LocalNode.others.map(LocalNode::asImmutable)
            override val neighbours get() = this@LocalNode.neighbours.map(LocalNode::asImmutable)
            override val neighboursDistances: Map<ImmutableNode, UInt>
                get() = this@LocalNode.neighboursDistances.mapKeys { it.key.asImmutable() }
        }
    }
}
