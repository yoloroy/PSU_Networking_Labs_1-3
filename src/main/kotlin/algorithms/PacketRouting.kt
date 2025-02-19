package com.yoloroy.psu.networking.labs.algorithms

import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph.*

sealed class PacketRouting(val displayName: String) {

    open fun restart() = Unit

    open fun startState(origin: ImmutableNode, destination: ImmutableNode) =
        next(Packet.begin(origin, destination))

    abstract fun next(state: Packet): List<Packet>

    override fun equals(other: Any?) = this === other

    override fun hashCode(): Int = displayName.hashCode()

    class Random : PacketRouting("Случайная маршрутизация") {
        override fun next(state: Packet): List<Packet> {
            return if (state.nextLocation == state.destination) {
                emptyList()
            } else {
                listOf(state.next(state.nextLocation.neighbours.random()))
            }
        }
    }

    class Avalanche : PacketRouting("Лавинная маршрутизация") {
        private val visited = mutableSetOf<ImmutableNode>()

        override fun restart() {
            visited.clear()
        }

        override fun next(state: Packet): List<Packet> {
            visited += state.location
            return state.nextLocation.neighbours.minus(visited).map { state.next(it) }
        }
    }
}

data class Packet(
    val origin: ImmutableNode,
    val location: ImmutableNode,
    val nextLocation: ImmutableNode,
    val destination: ImmutableNode,
    val progressToNext: Float,
    val topologicalDistance: Int,
    val seed: Int = (Int.MIN_VALUE..Int.MAX_VALUE).random()
) {
    companion object {
        fun begin(origin: ImmutableNode, destination: ImmutableNode) = Packet(
            origin = origin,
            location = origin,
            nextLocation = origin,
            destination = destination,
            progressToNext = 1f,
            topologicalDistance = 0
        )
    }

    fun next(nextNextLocation: ImmutableNode) = copy(
        location = nextLocation,
        nextLocation = nextNextLocation,
        progressToNext = 0f,
        topologicalDistance = topologicalDistance + 1
    )
}
