package com.yoloroy.psu.networking.labs.app.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import kotlin.random.Random

@Composable
inline fun WindowWithBox(
    noinline onCloseRequest: () -> Unit,
    title: String,
    crossinline content: @Composable (context (FrameWindowScope, BoxScope) () -> Unit)
) = Window(onCloseRequest = onCloseRequest, title = title) {
    Box {
        content(this@Window, this@Box)
    }
}

fun ClosedRange<Float>.lerp(fraction: Float) = fraction * (endInclusive - start) + start

fun ClosedRange<Float>.random() = lerp(Random.nextFloat())

fun ClosedRange<Float>.random(seed: Int) = lerp(Random(seed).nextFloat())

fun Size.randomOffsetInside() = Offset((0f..width).random(), (0f..height).random())
