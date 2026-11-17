package com.slouka.app.ui.animations

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val LetterOverlap = 0.18f
private const val PocketFold = 0.25f

@Composable
internal fun VerificationEnvelope(
    modifier: Modifier = Modifier,
    open: Boolean = true,
    letter: @Composable BoxScope.() -> Unit
) {
    val flap = remember { Animatable(0f) }
    val slide = remember { Animatable(0f) }
    val colors = MaterialTheme.colorScheme
    val pocketColor = lerpColor(colors.surfaceContainerHighest, colors.primary, 0.10f)
    val flapColor = lerpColor(pocketColor, colors.onSurface, 0.08f)

    LaunchedEffect(open) {
        if (!open) return@LaunchedEffect
        delay(400.milliseconds)
        flap.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
        slide.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
    }

    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            Canvas(Modifier) {
                drawPath(
                    path = envelopeFold(flap.value),
                    color = pocketColor
                )
            }

            Surface(
                modifier = Modifier.drawWithContent {
                    val pocketTop = pocketTop(size.height, 20.dp.toPx(), flap.value, slide.value)
                    val reveal = (pocketTop + size.height * PocketFold) * slide.value
                    clipRect(bottom = reveal.coerceAtMost(size.height)) {
                        this@drawWithContent.drawContent()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceContainerHigh,
                contentColor = colors.onSurface
            ) {
                Box(
                    modifier = Modifier.animateContentSize(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        alignment = Alignment.TopStart
                    ),
                    content = letter
                )
            }

            Canvas(Modifier) {
                val bottom = size.height
                val center = size.width / 2f
                val corner = minOf(10.dp.toPx(), bottom / 2f, center)
                val fold = bottom * PocketFold
                drawPath(
                    path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(center, fold)
                        lineTo(size.width, 0f)
                        lineTo(size.width, bottom - corner)
                        quadraticTo(size.width, bottom, size.width - corner, bottom)
                        lineTo(corner, bottom)
                        quadraticTo(0f, bottom, 0f, bottom - corner)
                        close()
                    },
                    color = pocketColor
                )
                drawPath(
                    path = Path().apply {
                        moveTo(corner, bottom - corner / 2f)
                        lineTo(center, fold)
                        lineTo(size.width - corner, bottom - corner / 2f)
                        close()
                    },
                    color = colors.onSurface.copy(alpha = 0.06f)
                )
                if (flap.value < 0.5f) {
                    drawPath(envelopeFold(flap.value), flapColor)
                }
            }
        }
    ) { measurables, constraints ->
        val inset = 4.dp.roundToPx()
        val paper = measurables[1].measure(
            constraints.copy(
                minWidth = (constraints.minWidth - inset * 2).coerceAtLeast(0),
                maxWidth = (constraints.maxWidth - inset * 2).coerceAtLeast(0),
                minHeight = 0
            )
        )
        val width = constraints.constrainWidth(paper.width + inset * 2)
        val height = paper.height
        val pocketBounds = Constraints.fixed(width, height)
        val back = measurables[0].measure(pocketBounds)
        val front = measurables[2].measure(pocketBounds)
        val finalPocketTop = height * (1f - LetterOverlap)
        val maxDrop = 20.dp.toPx()

        layout(width, constraints.constrainHeight((finalPocketTop + height).roundToInt())) {
            val pocketTop = pocketTop(height.toFloat(), maxDrop, flap.value, slide.value)
            val paperTop = (pocketTop + height * PocketFold) * (1f - slide.value)

            back.placeRelative(0, pocketTop.roundToInt())
            paper.placeRelative(inset, paperTop.roundToInt())
            front.placeRelative(0, pocketTop.roundToInt())
        }
    }
}

private fun pocketTop(height: Float, maxDrop: Float, flap: Float, slide: Float): Float {
    val drop = minOf(maxDrop, height * 0.14f)
    return height * (1f - LetterOverlap) - drop * (1f - (flap + slide) / 2f)
}

private fun DrawScope.envelopeFold(fraction: Float): Path = Path().apply {
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width / 2f, lerp(size.height * 0.64f, -size.height * 0.64f, fraction))
    close()
}
