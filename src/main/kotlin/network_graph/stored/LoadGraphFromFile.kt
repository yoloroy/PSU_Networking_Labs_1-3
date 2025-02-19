package com.yoloroy.psu.networking.labs.network_graph.stored

import java.io.File

fun loadGraphFromFile(file: File): List<List<UInt>> {
    val lines = file.readLines()
    val matrix = MutableList(lines.size) { MutableList(lines.size) { 0U } }

    lines.forEachIndexed { y, line ->
        line.split(",").forEach {
            it.split(":").let { (x, distance) ->
                matrix[y][x.toInt()] = distance.toUInt()
            }
        }
    }

    return matrix
}