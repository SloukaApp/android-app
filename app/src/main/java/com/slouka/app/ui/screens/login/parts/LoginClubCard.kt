package com.slouka.app.ui.screens.login.parts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slouka.app.ui.views.decos.MemberAvatar
import kotlin.math.roundToInt

@Composable
internal fun LoginClubCard(
    showSuccess: Boolean,
    shortcutEnabled: Boolean,
    onHomeShortcut: () -> Unit,
    onSuccessFinished: () -> Unit,
    avatarModifier: Modifier,
    content: @Composable () -> Unit
) {
    val reveal = remember { Animatable(0f) }
    val sweep = remember { Animatable(0f) }
    val cardRequester = remember { BringIntoViewRequester() }
    val finish = rememberUpdatedState(onSuccessFinished)

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            cardRequester.bringIntoView()
            reveal.animateTo(1f, tween(durationMillis = 160))
            sweep.animateTo(1f, tween(durationMillis = 420, easing = LinearEasing))
            finish.value()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .offset { IntOffset(0, (-8.dp.toPx() * reveal.value).roundToInt()) }
            .bringIntoViewRequester(cardRequester)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .graphicsLayer { alpha = 1f - reveal.value }
                    .then(if (showSuccess) Modifier.clearAndSetSemantics {} else Modifier)
                    .padding(all = 10.dp)
            ) {
                Text(
                    text = "Club card",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .combinedClickable(enabled = shortcutEnabled, onClick = {}, onLongClick = onHomeShortcut)
                        .alpha(0.5f)
                        .padding(bottom = 8.dp)
                )

                content()
            }

            if (showSuccess) {
                ClubCardSweep(progress = { sweep.value }, modifier = Modifier.matchParentSize())

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer { alpha = ((sweep.value - 0.24f) / 0.30f).coerceIn(0f, 1f) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MemberAvatar(
                        contentDescription = "Club member",
                        modifier = avatarModifier,
                        diameter = 52.dp
                    )
                    Text("Slouka Club", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun ClubCardSweep(progress: () -> Float, modifier: Modifier = Modifier) {
    val light = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = modifier.drawWithCache {
            val band = 22.dp.toPx()
            val glow = Brush.verticalGradient(
                0f to Color.Transparent,
                0.50f to light.copy(alpha = 0.21f),
                1f to Color.Transparent,
                startY = 0f,
                endY = band
            )
            onDrawBehind {
                val position = progress()
                if (position > 0f && position < 1f) {
                    translate(top = (size.height + band) * (1f - position) - band) {
                        drawRect(glow, size = Size(size.width, band))
                        drawLine(
                            color = light.copy(alpha = 0.74f),
                            start = Offset(0f, band / 2f),
                            end = Offset(size.width, band / 2f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }
        }
    ) {}
}
