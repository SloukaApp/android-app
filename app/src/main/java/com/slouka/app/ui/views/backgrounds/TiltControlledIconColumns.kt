package com.slouka.app.ui.views.backgrounds

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sign

@Composable
fun TiltControlledIconColumns(
    icons: List<ImageVector>,
    modifier: Modifier = Modifier,
    tilt: () -> Offset = { Offset.Zero },
    sensitivity: Float = 2.8f,
    tint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
    iconSize: Dp = 24.dp,
    spacing: Dp = 56.dp,
    selectionMode: IconSelectionMode = IconSelectionMode.PATTERN,
    randomSeed: Int = 0,
    durationMillis: Int = 9000,
    visibilityFraction: () -> Float = { 1f },
    visible: Boolean = true
) {
    require(iconSize > 0.dp && spacing > 0.dp)
    require(durationMillis > 0 && sensitivity.isFinite() && sensitivity >= 0f)
    require(icons.isNotEmpty())
    val period = iconPatternPeriod(icons.size).toFloat()

    IconBackgroundVisibility(visible, modifier, visibilityFraction) { appearance ->
        val currentTilt by rememberUpdatedState(tilt)
        var position by remember { mutableStateOf(Offset.Zero) }

        LaunchedEffect(durationMillis, sensitivity, period) {
            val motion = TiltIconMotion(period)
            var previousFrame = withFrameNanos { it }

            while (isActive) {
                withFrameNanos { frame ->
                    val seconds = ((frame - previousFrame) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    previousFrame = frame
                    val scale = coroutineContext[MotionDurationScale]?.scaleFactor ?: 1f

                    if (scale > 0f) {
                        position = motion.advance(
                            tilt = currentTilt(),
                            seconds = seconds / scale,
                            speed = 2000f / durationMillis,
                            sensitivity = sensitivity
                        )
                    }
                }
            }
        }

        IconColumnPattern(
            icons = icons,
            tint = tint,
            iconSize = iconSize,
            spacing = spacing,
            selectionMode = selectionMode,
            randomSeed = randomSeed,
            appearance = appearance,
            offset = { position * spacing.toPx() }
        )
    }
}

internal class TiltIconMotion(private val period: Float = 2f) {
    private var position = Offset.Zero
    private var velocity = Offset(0f, -1f)

    fun advance(tilt: Offset, seconds: Float, speed: Float, sensitivity: Float): Offset {
        fun steering(value: Float): Float {
            val outsideDeadZone = ((abs(value) - 0.06f) / 0.94f).coerceIn(0f, 1f)
            return sign(value) * outsideDeadZone
        }

        val target = Offset(
            x = steering(tilt.x) * sensitivity,
            y = -1f + steering(tilt.y) * sensitivity
        )
        val limitedTarget = target / maxOf(1f, target.getDistance() / 3f)
        val weight = 1f - exp(-seconds / 0.09f)
        velocity += (limitedTarget - velocity) * weight
        val next = position + velocity * (seconds * speed)

        // Wrap over the whole icon tile so identities do not change at a boundary.
        position = Offset(((next.x % period) + period) % period, ((next.y % period) + period) % period)
        return position
    }
}

enum class IconMotionMode {
    DRIFT,
    PARALLAX,
    TILT_CONTROL
}
