package com.slouka.app.ui.views.decos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal const val FlameCycleMillis = 1800
internal const val FlameExtinguishMillis = 760

@Stable
internal class FlameTransitionState(initialMode: FlameMode) {
    private var settledMode by mutableStateOf(initialMode)
    private var targetMode by mutableStateOf<FlameMode?>(null)
    private var sourceMode by mutableStateOf(initialMode)
    private var startFillAlpha by mutableFloatStateOf(0f)
    private var startCenterAlpha by mutableFloatStateOf(0f)
    private var startOutlineAlpha by mutableFloatStateOf(0f)
    private var animation by mutableStateOf(Animatable(0f))

    var sourcePhase by mutableStateOf<Float?>(null)
        private set

    val isIgniting: Boolean
        get() = targetMode?.let { it != FlameMode.REST } == true

    val isExtinguishing: Boolean
        get() = targetMode == FlameMode.REST

    val needsDynamicPhase: Boolean
        get() = when (targetMode) {
            null -> settledMode == FlameMode.BURNING
            FlameMode.REST -> sourceMode == FlameMode.BURNING
            else -> false
        }

    val progress: Float
        get() = if (targetMode != null) animation.value else 0f

    val cooling: Float
        get() = if (isExtinguishing) easingBetween(0f, 0.72f) else 0f

    val fillAlpha: Float
        get() = when {
            isExtinguishing -> startFillAlpha * (1f - easingBetween(0.10f, 0.70f))
            isIgniting -> startFillAlpha + (1f - startFillAlpha) * easingBetween(0.04f, 0.22f)
            settledMode == FlameMode.REST -> 0f
            else -> 1f
        }

    val centerAlpha: Float
        get() = when {
            isExtinguishing -> startCenterAlpha * (1f - easingBetween(0.37f, 0.93f))
            isIgniting -> startCenterAlpha + (1f - startCenterAlpha) * easingBetween(0f, 0.14f)
            else -> fillAlpha
        }

    val outlineAlpha: Float
        get() = when {
            isExtinguishing -> startOutlineAlpha + (1f - startOutlineAlpha) * easingBetween(0f, 0.55f)
            isIgniting -> startOutlineAlpha * (1f - easingBetween(0.04f, 0.21f))
            settledMode == FlameMode.REST -> 1f
            else -> 0f
        }

    private fun easingBetween(start: Float, end: Float): Float =
        FastOutSlowInEasing.transform(((progress - start) / (end - start)).coerceIn(0f, 1f))

    suspend fun moveTo(mode: FlameMode, animate: Boolean, phase: Float?) {
        val previousMode = targetMode ?: settledMode
        if (animate && mode != previousMode) {
            val previousOutlineAlpha = outlineAlpha
            val previousCenterAlpha = centerAlpha
            startFillAlpha = fillAlpha
            startCenterAlpha = previousCenterAlpha
            startOutlineAlpha = previousOutlineAlpha
            sourceMode = previousMode
            sourcePhase = phase
            val transition = Animatable(0f)
            animation = transition
            targetMode = mode
            val duration = if (mode == FlameMode.REST) FlameExtinguishMillis else 1350
            transition.animateTo(1f, tween(duration, easing = LinearEasing))
        }
        settledMode = mode
        targetMode = null
    }
}
