package com.slouka.app.ui.animations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun UnrollCardAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    rollInset: Dp = 0.dp,
    rollColor: Color = CardDefaults.cardColors().containerColor,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
        modifier = modifier
    ) {
        val progress by transition.animateFloat(
            transitionSpec = {
                tween(
                    durationMillis = if (targetState == EnterExitState.Visible) 600 else 350,
                    easing = FastOutSlowInEasing
                )
            },
            label = "unroll"
        ) { state ->
            if (state == EnterExitState.Visible) 1f else 0f
        }
        val contentLayer = rememberGraphicsLayer()

        Box(
            modifier = Modifier
                .graphicsLayer {
                    clip = true
                    compositingStrategy = if (progress > 0f && progress < 1f) {
                        CompositingStrategy.Offscreen
                    } else {
                        CompositingStrategy.Auto
                    }
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val fraction = progress.coerceIn(0f, 1f)
                    val radius = unrollRadius(placeable.height.toFloat(), fraction, 24.dp.toPx())
                    val currentHeight = (placeable.height * fraction + radius * 2f).roundToInt()

                    layout(placeable.width, currentHeight) {
                        placeable.placeRelative(0, 0)
                    }
                }
                .drawWithContent {
                    val fraction = progress.coerceIn(0f, 1f)

                    if (fraction >= 1f) {
                        drawContent()
                    } else if (fraction > 0f && size.height > 0f) {
                        contentLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        val flatHeight = size.height * fraction
                        val halfPi = (PI / 2).toFloat()
                        val radius = unrollRadius(size.height, fraction, 24.dp.toPx())
                        val currentHeight = flatHeight + radius * 2f
                        val inset = rollInset.toPx().coerceIn(0f, size.width / 2f)
                        val rollSize = Size(size.width - inset * 2f, radius * 2f)
                        val rollCorner = CornerRadius(minOf(4.dp.toPx(), radius), radius)

                        if (radius > 0f) {
                            drawRoundRect(
                                color = rollColor,
                                topLeft = Offset(inset, flatHeight),
                                size = rollSize,
                                cornerRadius = rollCorner
                            )
                        }

                        clipRect(bottom = flatHeight) {
                            drawLayer(contentLayer)
                        }

                        // Project the moving edge onto a quarter cylinder; the rest stays flat.
                        val stripCount = 32
                        repeat(stripCount) { index ->
                            val startAngle = halfPi * index / stripCount
                            val endAngle = halfPi * (index + 1) / stripCount
                            val sourceTop = flatHeight + radius * startAngle
                            val sourceHeight = radius * (endAngle - startAngle)
                            val top = flatHeight + radius * sin(startAngle)
                            val bottom = flatHeight + radius * sin(endAngle)

                            if (sourceHeight > 0f && bottom > top) {
                                clipRect(top = top, bottom = bottom) {
                                    withTransform({
                                        translate(top = top)
                                        scale(
                                            scaleX = 1f,
                                            scaleY = (bottom - top) / sourceHeight,
                                            pivot = Offset.Zero
                                        )
                                        translate(top = -sourceTop)
                                    }) {
                                        drawLayer(contentLayer)
                                    }
                                }
                            }
                        }

                        if (radius > 0f) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    0f to Color.Black.copy(alpha = 0.06f),
                                    0.25f to Color.White.copy(alpha = 0.04f),
                                    0.55f to Color.White.copy(alpha = 0.05f),
                                    0.8f to Color.Black.copy(alpha = 0.05f),
                                    1f to Color.Black.copy(alpha = 0.18f),
                                    startY = flatHeight,
                                    endY = currentHeight
                                ),
                                topLeft = Offset(inset, flatHeight),
                                size = rollSize,
                                cornerRadius = rollCorner,
                                blendMode = BlendMode.SrcAtop
                            )

                            val endWidth = minOf(6.dp.toPx(), radius, rollSize.width)
                            drawOval(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        rollColor,
                                        lerp(rollColor, Color.Black, 0.14f)
                                    ),
                                    startX = size.width - inset - endWidth,
                                    endX = size.width - inset
                                ),
                                topLeft = Offset(size.width - inset - endWidth, flatHeight),
                                size = Size(endWidth, radius * 2f)
                            )
                        }
                    }
                }
        ) {
            content()
        }
    }
}

private fun unrollRadius(height: Float, progress: Float, maxRadius: Float): Float {
    // The cross-section shrinks with the amount of paper left in the roll.
    val remaining = 1f - progress
    val appearance = (progress / 0.06f).coerceIn(0f, 1f)

    return minOf(maxRadius * sqrt(remaining), height * remaining / 2f) * appearance
}
