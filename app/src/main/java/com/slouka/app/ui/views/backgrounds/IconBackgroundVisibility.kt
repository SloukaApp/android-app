package com.slouka.app.ui.views.backgrounds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier

internal fun iconBackgroundFadeSpec(durationMillis: Int = 250) = tween<Float>(durationMillis)

@Composable
internal fun IconBackgroundVisibility(
    visible: Boolean,
    modifier: Modifier,
    visibilityFraction: () -> Float = { 1f },
    content: @Composable (State<Float>) -> Unit
) {
    val fraction = rememberUpdatedState(visibilityFraction)

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = EnterTransition.None,
        exit = ExitTransition.None
    ) {
        val appearance = transition.animateFloat(
            transitionSpec = { iconBackgroundFadeSpec() },
            label = "icon_background_appearance"
        ) { state ->
            if (state == EnterExitState.Visible) 1f else 0f
        }

        val combinedAppearance = remember(appearance) {
            derivedStateOf { appearance.value * fraction.value().coerceIn(0f, 1f) }
        }
        content(combinedAppearance)
    }
}
