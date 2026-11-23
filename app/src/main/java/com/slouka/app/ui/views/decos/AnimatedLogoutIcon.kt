package com.slouka.app.ui.views.decos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.slouka.app.ui.theme.SloukaTheme

private const val WaitingDistance = 3.5f
private const val ExitDistance = 18f

@Composable
internal fun AnimatedLogoutIcon(
    isLoggingOut: Boolean,
    modifier: Modifier = Modifier,
    logoutSucceeded: Boolean = false,
    onDepartureFinished: () -> Unit = {}
) {
    val departure = remember { Animatable(if (isLoggingOut) WaitingDistance else 0f) }
    val finishDeparture by rememberUpdatedState(onDepartureFinished)
    LaunchedEffect(isLoggingOut, logoutSucceeded) {
        when {
            logoutSucceeded -> {
                departure.animateTo(ExitDistance, tween(220))
                finishDeparture()
            }
            isLoggingOut -> departure.animateTo(WaitingDistance, tween(430))
            else -> departure.animateTo(0f, tween(300))
        }
    }
    val tint = MaterialTheme.colorScheme.primary
    val door = remember {
        Path().apply {
            moveTo(11f, 7.5f)
            lineTo(11f, 4.5f)
            lineTo(5f, 4.5f)
            quadraticTo(3.5f, 4.5f, 3.5f, 6f)
            lineTo(3.5f, 18f)
            quadraticTo(3.5f, 19.5f, 5f, 19.5f)
            lineTo(11f, 19.5f)
            lineTo(11f, 16.5f)
        }
    }
    val arrow = remember {
        Path().apply {
            moveTo(8f, 12f)
            lineTo(19.5f, 12f)
            moveTo(15.5f, 8f)
            lineTo(19.5f, 12f)
            lineTo(15.5f, 16f)
        }
    }

    Canvas(modifier.size(24.dp)) {
        val stroke = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val mirror = layoutDirection == LayoutDirection.Rtl
        clipRect {
            withTransform({
                if (mirror) translate(left = size.width, top = 0f)
                scale(
                    scaleX = size.width / 24f * (if (mirror) -1f else 1f),
                    scaleY = size.height / 24f,
                    pivot = Offset.Zero
                )
            }) {
                drawPath(door, tint, style = stroke)
                translate(left = departure.value) {
                    drawPath(arrow, tint, style = stroke)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimatedLogoutIconPreview() {
    SloukaTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimatedLogoutIcon(isLoggingOut = false)
            AnimatedLogoutIcon(isLoggingOut = true)
        }
    }
}
