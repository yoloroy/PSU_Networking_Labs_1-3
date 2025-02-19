package com.yoloroy.psu.networking.labs.app.windows.graph

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.yoloroy.psu.networking.labs.algorithms.Packet
import com.yoloroy.psu.networking.labs.app.common.random
import com.yoloroy.psu.networking.labs.network_graph.NetworkGraph

private const val NODE_RADIUS = 6f
private val PACKET_SIZE = Size(24f, 24f)

fun DrawScope.drawPointsNames(
    nodes: List<NetworkGraph.MutableNode>,
    coordinates: NodesCoordinates,
    textMeasurer: TextMeasurer,
    scale: Float
) {
    for ((node, xy) in coordinates.filterKeys { it in nodes }) {
        drawOutlinedText(textMeasurer, "#${node.id}", xy, scale)
    }
}

fun DrawScope.drawLinesDistances(
    nodes: List<NetworkGraph.MutableNode>,
    coordinates: NodesCoordinates,
    textMeasurer: TextMeasurer,
    scale: Float
) {
    for ((a, axy) in nodes.map { it to coordinates[it]!! }) {
        for ((b, distance) in a.neighboursDistances.filterKeys { it.id > a.id }) {
            val bxy = coordinates[b]!!
            val edgeCenter = (axy + bxy) / 2f

            drawOutlinedText(textMeasurer, "$distance", edgeCenter, scale)
        }
    }
}

fun DrawScope.drawPoints(
    nodes: List<NetworkGraph.MutableNode>,
    coordinates: NodesCoordinates,
    path: ShortestPath,
    scale: Float
) {
    for ((node, xy) in nodes.map { it to coordinates[it]!! }) {
        val color = when (node) {
            path.a -> Color.Red
            path.b -> Color.Blue
            else -> Color.Black
        }
        drawCircle(color, NODE_RADIUS * scale, xy)
    }
}

fun DrawScope.drawLines(
    nodes: List<NetworkGraph.MutableNode>,
    coordinates: NodesCoordinates,
    scale: Float
) {
    for ((a, axy) in nodes.map { it to coordinates[it]!! }) {
        for (b in a.neighbours.filter { it.id > a.id }) {
            val bxy = coordinates[b]!!
            drawLine(Color.Black, axy, bxy, strokeWidth = 2f * scale)
        }
    }
}

fun DrawScope.drawPackets(
    packets: List<Packet>,
    coordinates: NodesCoordinates,
    PACKET_VECTOR: VectorPainter,
    scale: Float,
    packetMultiplier: Int? = null
) {
    packetMultiplier?.let { multiplier ->
        drawPackets(
            packets = packets.flatMap { packet ->
                List(multiplier) { i -> packet.copy(seed = packet.seed + i) }
            },
            coordinates = coordinates,
            PACKET_VECTOR = PACKET_VECTOR,
            scale = scale
        )
        return
    }

    for (packet in packets) {
        val (_, axy) = coordinates.byId(packet.location.id)
        val (_, bxy) = coordinates.byId(packet.nextLocation.id)
        val xy = lerp(axy, bxy, packet.progressToNext) + randomOffset(5f * scale, packet.seed)

        translate(left = xy.x, top = xy.y) {
            with (PACKET_VECTOR) {
                draw(
                    size = PACKET_SIZE * scale,
                    colorFilter = ColorFilter.tint(
                        Color(
                            (0f..1f).random(packet.seed + 1),
                            (0f..1f).random(packet.seed + 2),
                            (0f..1f).random(packet.seed + 3)
                        )
                    )
                )
            }
        }
    }
}

fun DrawScope.drawLines(path: ShortestPath, coordinates: NodesCoordinates, scale: Float) {
    for ((a, b) in path.map { coordinates[it]!! }.zipWithNext()) {
        drawLine(Brush.linearGradient(listOf(Color.Red, Color.Blue), a, b), a, b, 3f * scale)
    }
}

fun DrawScope.drawShortestPathResultLine(
    path: ShortestPath,
    coordinates: NodesCoordinates,
    textMeasurer: TextMeasurer,
    scale: Float
) {
    if (path.isEmpty()) {
        return
    }

    val a = path.first()
    val b = path.last()
    val axy = coordinates[a]!!
    val bxy = coordinates[b]!!
    val edgeCenter = (axy + bxy) / 2f
    drawLine(
        color = Color.Gray,
        start = axy,
        end = bxy,
        strokeWidth = 2f * scale,
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(8f, 8f)
        )
    )
    drawOutlinedText(textMeasurer, "${path.length}", edgeCenter, scale)
}

fun DrawScope.drawOutlinedText(
    textMeasurer: TextMeasurer,
    text: String,
    offset: Offset,
    scale: Float
) {
    try {
        val outlineOffsets = (((1 * scale).toInt()..(3 * scale).toInt()) + ((-3 * scale).toInt()..(-1 * scale).toInt())).run {
            flatMap { x ->
                map { y ->
                    Offset(x.toFloat(), y.toFloat())
                }
            }
        }

        for (outlineOffset in outlineOffsets) {
            drawText(
                textMeasurer = textMeasurer,
                text = text,
                topLeft = offset + outlineOffset,
                style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp * scale
                ),
            )
        }

        drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = offset,
            style = TextStyle(
                color = Color.Black,
                fontSize = 12.sp * scale
            )
        )
    }
    catch (_: IllegalArgumentException) {
        // text cannot be draw outside the window, also we do not need it to render outside
    }
}

private fun randomOffset(maxRadius: Float, seed: Int) = Offset(((-maxRadius)..(+maxRadius)).random(seed + 1), ((-maxRadius)..(+maxRadius)).random(seed + 2))
