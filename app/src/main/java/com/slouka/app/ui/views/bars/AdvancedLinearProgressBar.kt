package com.slouka.app.ui.views.bars

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val LoadingCycleMillis = 1750
private val LoadingEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

// Material 3's indeterminate linear timings, in milliseconds.
private val LoadingLines = listOf(
    LoadingLine(0, 250, 1000),
    LoadingLine(650, 900, 850)
)

private data class LoadingLine(val headDelay: Int, val tailDelay: Int, val duration: Int) {
    val endTime: Int get() = tailDelay + duration

    fun head(time: Float): Float = position(time, headDelay)
    fun tail(time: Float): Float = position(time, tailDelay)

    private fun position(time: Float, delay: Int): Float =
        LoadingEasing.transform(((time - delay) / duration).coerceIn(0f, 1f))
}

@Composable
fun AdvancedLinearProgressBar(
    steps: IntArray,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    onIndeterminateChanged: (Boolean) -> Unit = {},
) {
    val loadingSegments = remember(steps.size) {
        List(steps.size) { mutableStateOf(false) }
    }

    val hasRunningSegments = loadingSegments.any { it.value }
    val isIndeterminate = hasRunningSegments || (isVisible && steps.any { it < 0 })
    LaunchedEffect(isIndeterminate) {
        onIndeterminateChanged(isIndeterminate)
    }

    AnimatedVisibility(
        visible = isVisible || hasRunningSegments,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            steps.forEachIndexed { index, step ->
                val loading = loadingSegments[index]
                ProgressSegment(
                    step = step,
                    isVisible = isVisible,
                    isLoading = loading.value,
                    onLoadingChanged = { loading.value = it },
                    modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressSegment(
    step: Int,
    isVisible: Boolean,
    isLoading: Boolean,
    onLoadingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val phase = remember { Animatable(0f) }
    var finishingLines by remember { mutableStateOf<List<LoadingLine>?>(null) }
    val shouldLoad = isVisible && step < 0
    val showLoading = shouldLoad || isLoading
    val progress by animateFloatAsState(
        targetValue = if (showLoading) 0f else step.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 500),
        label = "segment_progress"
    )

    LaunchedEffect(shouldLoad) {
        if (shouldLoad) {
            if (finishingLines != null) phase.snapTo(0f)
            finishingLines = null
            onLoadingChanged(true)
            // A zero-duration animation scale must not turn the repeat into a busy loop.
            while (true) {
                phase.finishAt(LoadingCycleMillis)
                withFrameNanos { }
                phase.snapTo(0f)
            }
        } else if (isLoading) {
            val elapsed = phase.value
            val active = LoadingLines.filter { elapsed > it.headDelay && elapsed < it.endTime }
            finishingLines = active
            active.maxOfOrNull { it.endTime }?.let { phase.finishAt(it) }
            onLoadingChanged(false)
            phase.snapTo(0f)
        }
    }

    val color = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    if (showLoading) {
        val gapSize = ProgressIndicatorDefaults.LinearIndicatorTrackGapSize
        val strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
        Canvas(modifier.progressSemantics()) {
            if (size.width <= 0f || size.height <= 0f) return@Canvas

            val time = phase.value
            val gap = (gapSize.toPx() + if (strokeCap == StrokeCap.Butt || size.height > size.width) {
                0f
            } else {
                size.height
            }) / size.width
            var trackStart = 0f

            // The second line follows the first, so draw the track from start to end.
            for (line in (finishingLines ?: LoadingLines).asReversed()) {
                val start = line.tail(time)
                val end = line.head(time)
                if (end <= start) continue

                drawSegment(trackStart, start - gap, trackColor, strokeCap)
                drawSegment(start, end, color, strokeCap)
                trackStart = end + gap
            }
            drawSegment(trackStart, 1f, trackColor, strokeCap)
        }
    } else {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            drawStopIndicator = {}
        )
    }
}

private suspend fun Animatable<Float, *>.finishAt(timeMillis: Int) {
    val remaining = (timeMillis - value).roundToInt()
    if (remaining > 0) {
        animateTo(timeMillis.toFloat(), tween(remaining, easing = LinearEasing))
    } else {
        snapTo(timeMillis.toFloat())
    }
}

private fun DrawScope.drawSegment(start: Float, end: Float, color: Color, strokeCap: StrokeCap) {
    if (end <= start) return

    val rtl = layoutDirection == LayoutDirection.Rtl
    val left = (if (rtl) 1f - end else start) * size.width
    val right = (if (rtl) 1f - start else end) * size.width
    val centerY = size.height / 2f
    val cap = if (size.height > size.width) StrokeCap.Butt else strokeCap
    val inset = if (cap == StrokeCap.Butt) 0f else centerY

    drawLine(
        color = color,
        start = Offset(left.coerceIn(inset, size.width - inset), centerY),
        end = Offset(right.coerceIn(inset, size.width - inset), centerY),
        strokeWidth = size.height,
        cap = cap
    )
}

@Preview(showBackground = true)
@Composable
fun AdvancedLinearProgressBarPreview() {
    AdvancedLinearProgressBar(
        steps = IntArray(3) { -1 }
    )
}
