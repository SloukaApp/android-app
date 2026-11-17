package com.slouka.app.ui.views.decos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val SignatureOffset = 22.dp
private val SignatureWidth = 126.dp

@Composable
internal fun ClubSignatureLine(
    showSignature: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    val lineRequester = remember { BringIntoViewRequester() }
    val finish = rememberUpdatedState(onFinished)
    val guide = MaterialTheme.colorScheme.outline
    val ink = MaterialTheme.colorScheme.primary

    LaunchedEffect(showSignature) {
        if (showSignature) {
            lineRequester.bringIntoView()
            progress.animateTo(1f, tween(540, easing = LinearEasing))
            delay(220.milliseconds)
            finish.value()
        } else {
            progress.snapTo(0f)
        }
    }

    Canvas(
        modifier = modifier
            .width(SignatureOffset + SignatureWidth)
            .height(42.dp)
            .bringIntoViewRequester(lineRequester)
            .drawWithCache {
                val signatureStart = SignatureOffset.toPx()
                val baseline = size.height - 7.dp.toPx()
                val unit = minOf((size.width - signatureStart).coerceAtLeast(0f) / SignatureWidth.value, size.height / 42f)
                fun x(value: Int) = signatureStart + value * unit
                fun y(value: Int) = baseline - value * unit
                val signature = Path().apply {
                    moveTo(x(4), y(2))
                    cubicTo(x(34), y(40), x(50), y(36), x(26), y(17))
                    cubicTo(x(4), y(-1), x(35), y(-3), x(49), y(16))
                    cubicTo(x(61), y(30), x(67), y(19), x(55), y(6))
                    cubicTo(x(53), y(1), x(68), y(7), x(73), y(14))
                    cubicTo(x(84), y(27), x(70), y(-4), x(88), y(5))
                    cubicTo(x(101), y(12), x(108), y(16), x(123), y(13))
                }
                val measure = PathMeasure().apply { setPath(signature, forceClosed = false) }
                val visibleInk = Path()
                val pen = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val bounds = signature.getBounds()
                val linePadding = pen.width / 2f + 2.dp.toPx()
                val lineStart = (bounds.left - linePadding).coerceAtLeast(signatureStart)
                val lineEnd = (bounds.right + linePadding).coerceAtMost(size.width)

                onDrawBehind {
                    val crossTop = baseline - 9.dp.toPx()
                    val crossLeft = 4.dp.toPx()
                    val crossRight = 12.dp.toPx()
                    val guideWidth = 1.dp.toPx()
                    drawLine(guide, Offset(crossLeft, crossTop), Offset(crossRight, baseline), guideWidth)
                    drawLine(guide, Offset(crossRight, crossTop), Offset(crossLeft, baseline), guideWidth)
                    if (lineEnd > lineStart) {
                        drawLine(guide, Offset(lineStart, baseline), Offset(lineEnd, baseline), guideWidth)
                    }

                    visibleInk.rewind()
                    if (measure.getSegment(0f, measure.length * progress.value, visibleInk)) {
                        drawPath(visibleInk, ink, style = pen)
                    }
                }
            }
    ) {}
}
