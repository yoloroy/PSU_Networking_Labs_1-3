package com.yoloroy.psu.networking.labs.app.windows.graph

import androidx.compose.runtime.*
import androidx.compose.ui.window.MenuBarScope
import com.yoloroy.psu.networking.labs.algorithms.Packet
import com.yoloroy.psu.networking.labs.algorithms.PacketRouting
import com.yoloroy.psu.networking.labs.app.common.ChangeValueWindow
import com.yoloroy.psu.networking.labs.app.common.random
import com.yoloroy.psu.networking.labs.app.windows.graph.PacketsMode.Channel
import com.yoloroy.psu.networking.labs.app.windows.graph.PacketsMode.Datagram
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph.ImmutableNode
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.measureTimedValue

enum class PacketsMode(val offset: Float) { Channel(0f), Datagram(0.1f) }

@Composable
fun rememberPacketsState(
    origin: ImmutableNode,
    destination: ImmutableNode,
    count: Int,
    repeating: Boolean,
    algorithm: PacketRouting,
    goalDt: Duration,
    dProgress: Float,
    mode: PacketsMode,
    onStep: (pocket: Packet) -> Unit
): List<Packet> {
    val trueCount = when (mode) {
        Datagram -> count
        Channel -> 1
    }

    var i by remember { mutableStateOf(0) }
    var dt by remember(goalDt) { mutableStateOf(goalDt) }
    var packets by remember(origin, destination, algorithm) {
        mutableStateOf(population(trueCount, algorithm, origin, destination, mode.offset))
    }

    LaunchedEffect(i) {
        if (i == trueCount) {
            algorithm.restart()
            packets = populatedList(trueCount) { algorithm.startState(origin, destination) }
            i = 0
        }
    }

    LaunchedEffect(packets, repeating) {
        if (packets.isEmpty() && repeating) {
            packets = populatedList(trueCount) { algorithm.startState(origin, destination) }
            i++
        }
    }

    LaunchedEffect(packets) {
        if (dt > Duration.ZERO) {
            delay(dt)
        }

        val (newPackets, calculatingTime) = measureTimedValue {
            packets
                .map { packet -> packet + dProgress }
                .partition { it.progressToNext >= 1f }
                .let { (stepped, journeying) ->
                    stepped.onEach(onStep).flatMap(algorithm::next) + journeying
                }
                .filter { it.location != destination }
        }

        dt = goalDt - calculatingTime
        packets = newPackets
    }

    return packets
}

@Composable
fun MenuBarScope.PacketsMenu(
    config: PacketsConfig,
    update: (PacketsConfig) -> Unit,
    openPacketsCountDialog: () -> Unit,
    openExperienceDialog: () -> Unit,
) {
    var algorithm by remember { mutableStateOf(config.algorithm) }
    var mode by remember { mutableStateOf(config.mode) }
    var repeating by remember { mutableStateOf(false) }

    LaunchedEffect(algorithm, mode, repeating) {
        update(config.copy(algorithm = algorithm, mode = mode, repeating = repeating))
    }

    Menu("Маршрутизация") {
        Item("Открыть таблицу опыта", onClick = openExperienceDialog)
        Separator()
        Item((if (algorithm == null) " • " else "") + "Отключить маршрутизацию", enabled = algorithm != null) {
            algorithm = null
        }
        Item((if (algorithm is PacketRouting.Random) " • " else "") + "Случайная маршрутизация", enabled = algorithm !is PacketRouting.Random) {
            algorithm = PacketRouting.Random()
        }
        Item((if (algorithm is PacketRouting.Avalanche) " • " else "") + "Лавинная маршрутизация", enabled = algorithm !is PacketRouting.Avalanche) {
            algorithm = PacketRouting.Avalanche()
        }
        Separator()
        Item((if (mode == null) " • " else "") + "Остановить отправку пакетов", enabled = mode != null) {
            mode = null
        }
        Item((if (mode == Datagram) " • " else "") + "Запустить пакеты дейтаграммным методом", enabled = mode != Datagram) {
            mode = Datagram
        }
        Item((if (mode == Channel) " • " else "") + "Запустить пакеты методом виртуального канала", enabled = mode != Channel) {
            mode = Channel
        }
        Separator()
        Item("Количество пускаемых пакетов: ${config.count}", onClick = openPacketsCountDialog)
        Item(if (config.repeating) "Повторяем отправку" else "Не повторяем отправку") {
            repeating = !repeating
        }
    }
}

@Composable
inline fun PacketsCountDialog(
    config: PacketsConfig,
    crossinline update: (PacketsConfig) -> Unit,
    crossinline onClose: () -> Unit
) = ChangeValueWindow(
    title = "Отправляемые пакеты",
    label = "Количество",
    value = config.count,
    parseOrNull = String::toIntOrNull,
    update = { update(config.copy(count = it)) },
    onClose = { onClose() }
)

private fun population(count: Int, algorithm: PacketRouting, origin: ImmutableNode, destination: ImmutableNode, maxOffset: Float) =
    populatedList(count) { algorithm.startState(origin, destination).map { it + (0f..maxOffset).random() } }

data class PacketsConfig(
    val algorithm: PacketRouting?,
    val mode: PacketsMode?,
    val count: Int,
    val repeating: Boolean
) {
    fun takeIfConfigured(): Ready? {
        val algorithm = algorithm ?: return null
        val mode = mode ?: return null
        return Ready(algorithm, mode, count, repeating)
    }

    data class Ready(
        val algorithm: PacketRouting,
        val mode: PacketsMode,
        val count: Int,
        val repeating: Boolean
    )
}

operator fun Packet.plus(dProgress: Float) = copy(progressToNext = progressToNext + dProgress / location[nextLocation].toFloat())

private inline fun <T: Any> populatedList(count: Int, crossinline produce: (i: Int) -> List<T>): List<T> = sequence {
    for (i in 0..<count) {
        yieldAll(produce(i))
    }
}.toList()

