package com.yoloroy.psu.networking.labs.app

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

class StateNode(
    nodesState: MutableState<List<StateNode>>,
    private val node: NetworkGraph.MutableNode
) : NetworkGraph.MutableNode, NetworkGraph.Node by node {

    private var nodes by nodesState
    private val immutable = node.asImmutable() // optimization through caching

    override fun remove() {
        node.remove()
        nodes -= this
    }

    override fun set(other: NetworkGraph.Node, distance: UInt) {
        node[other] = distance
        nodes = nodes
    }

    override fun asImmutable() = immutable

    override val others get() = nodes.filter { it.exists() }.filter { it != this }
    override val neighbours get() = others.filter { try { this[it] != 0U } catch (e: Exception) { false } }
    override val neighboursDistances: Map<NetworkGraph.MutableNode, UInt> get() = neighbours.associateWith { it[this] }
}