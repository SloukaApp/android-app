package com.slouka.app.ui.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@Composable
internal fun Modifier.rememberInvalidInputFeedback(attempt: Int): Modifier {
    val offset = remember { Animatable(0f) }
    val haptics = LocalHapticFeedback.current
    var lastAttempt by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(attempt) {
        if (attempt == 0) lastAttempt = 0
        if (attempt > lastAttempt) {
            lastAttempt = attempt
            haptics.performHapticFeedback(HapticFeedbackType.Reject)
            offset.snapTo(0f)
            offset.animateTo(0f, keyframes {
                durationMillis = 260
                (-4f) at 45
                4f at 105
                (-2f) at 165
                1f at 215
            })
        }
    }

    return this.graphicsLayer { translationX = offset.value.dp.toPx() }
}
