package com.slouka.app.ui.animations

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

internal const val AuthPanelTransitionMillis = 440

@Composable
internal fun AnimatedVisibilityScope.authPanelProgress(
    openOnExit: Boolean,
    closeOnEnter: Boolean = false
): State<Float>? {
    if (!openOnExit && !closeOnEnter) return null

    return transition.animateFloat(
        transitionSpec = { tween(AuthPanelTransitionMillis, easing = FastOutSlowInEasing) },
        label = "auth_panels"
    ) { state ->
        when (state) {
            EnterExitState.PreEnter -> if (closeOnEnter) 1f else 0f
            EnterExitState.Visible -> 0f
            EnterExitState.PostExit -> if (openOnExit) 1f else 0f
        }
    }
}

internal fun SharedTransitionScope.authPanel(
    progress: State<Float>?,
    upward: Boolean
): Modifier {
    if (progress == null) return Modifier

    val direction = if (upward) -1f else 1f
    return Modifier
        .layout { measurable, constraints ->
            val panel = measurable.measure(constraints)
            layout(panel.width, panel.height) {
                panel.placeRelative(0, (panel.height * progress.value * direction).roundToInt())
            }
        }
        .renderInSharedTransitionScopeOverlay(
            zIndexInOverlay = 1f,
            renderInOverlay = { progress.value > 0f }
        )
}
