package com.slouka.app.ui.views.backgrounds

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DiagonalIconColumns(
    icons: List<ImageVector>,
    modifier: Modifier = Modifier,
    direction: DiagonalIconDirection = DiagonalIconDirection.UP_RIGHT,
    tint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
    iconSize: Dp = 24.dp,
    spacing: Dp = 56.dp,
    selectionMode: IconSelectionMode = IconSelectionMode.PATTERN,
    randomSeed: Int = 0,
    durationMillis: Int = 12000,
    visibilityFraction: () -> Float = { 1f },
    visible: Boolean = true,
    parallaxOffset: () -> Offset = { Offset.Zero }
) {
    require(iconSize > 0.dp && spacing > 0.dp)
    require(durationMillis > 0)
    require(icons.isNotEmpty())
    val period = iconPatternPeriod(icons.size)

    IconBackgroundVisibility(visible, modifier, visibilityFraction) { appearance ->
        val transition = rememberInfiniteTransition(label = "icon_columns")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMillis * icons.size, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "diagonal_offset"
        )

        IconColumnPattern(
            icons = icons,
            tint = tint,
            iconSize = iconSize,
            spacing = spacing,
            selectionMode = selectionMode,
            randomSeed = randomSeed,
            appearance = appearance,
            offset = {
                val distance = progress * spacing.toPx() * period
                Offset(distance * direction.horizontal, distance * direction.vertical) + parallaxOffset()
            }
        )
    }
}

enum class DiagonalIconDirection(val horizontal: Float, val vertical: Float) {
    UP_LEFT(-1f, -1f),
    UP_RIGHT(1f, -1f),
    DOWN_LEFT(-1f, 1f),
    DOWN_RIGHT(1f, 1f)
}
