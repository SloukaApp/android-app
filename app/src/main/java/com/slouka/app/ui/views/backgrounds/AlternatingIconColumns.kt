package com.slouka.app.ui.views.backgrounds

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.hypot

@Composable
fun AlternatingIconColumns(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    angle: Float = -45f,
    reverseDirection: Boolean = false,
    tint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
    iconSize: Dp = 48.dp,
    spacing: Dp = 56.dp,
    durationMillis: Int = 12000,
    visible: Boolean = true
) {
    require(angle.isFinite())
    require(iconSize > 0.dp && spacing > 0.dp)
    require(durationMillis > 0)

    IconBackgroundVisibility(visible, modifier) { appearance ->
        val painter = rememberVectorPainter(icon)
        val colorFilter = ColorFilter.tint(tint)
        val transition = rememberInfiniteTransition(label = "alternating_icon_columns")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "column_offset"
        )

        Canvas(modifier = Modifier.clipToBounds()) {
            val step = spacing.toPx()
            val fullSize = iconSize.toPx()
            val fraction = appearance.value
            if (fraction <= 0f) return@Canvas
            val iconPx = fullSize * fraction
            val inset = (fullSize - iconPx) / 2f
            // A square covering the diagonal keeps every corner filled at any angle.
            val extent = hypot(size.width, size.height)
            val count = ceil(extent / step).toInt()
            val offset = progress * step * 2f * if (reverseDirection) -1f else 1f

            rotate(degrees = angle) {
                translate(left = center.x - extent / 2f, top = center.y - extent / 2f) {
                    for (column in -2..count + 2) {
                        val evenColumn = column % 2 == 0
                        val stagger = if (evenColumn) 0f else step / 2f
                        val direction = if (evenColumn) -1f else 1f

                        for (row in -2..count + 2) {
                            translate(
                                left = column * step + inset,
                                top = row * step + stagger + offset * direction + inset
                            ) {
                                with(painter) {
                                    draw(size = Size(iconPx, iconPx), alpha = fraction, colorFilter = colorFilter)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
