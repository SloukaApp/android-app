package com.slouka.app.ui.views.snackbars

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun ErrorSnackbarHost(hostState: SnackbarHostState) {
    val currentSnackbar = hostState.currentSnackbarData
    val accessibilityManager = LocalAccessibilityManager.current

    LaunchedEffect(currentSnackbar, accessibilityManager) {
        val snackbar = currentSnackbar ?: return@LaunchedEffect
        val duration = when (snackbar.visuals.duration) {
            SnackbarDuration.Short -> 4_000L
            SnackbarDuration.Long -> 10_000L
            SnackbarDuration.Indefinite -> return@LaunchedEffect
        }
        val timeout = accessibilityManager?.calculateRecommendedTimeoutMillis(
            originalTimeoutMillis = duration,
            containsIcons = true,
            containsText = true,
            containsControls = snackbar.visuals.actionLabel != null
        ) ?: duration

        delay(timeout.milliseconds)
        snackbar.dismiss()
    }

    AnimatedContent(
        targetState = currentSnackbar,
        modifier = Modifier.imePadding().clipToBounds(),
        contentAlignment = Alignment.BottomCenter,
        transitionSpec = {
            val enter = slideInVertically(tween(250, easing = LinearOutSlowInEasing)) { it }
            val exit = slideOutVertically(tween(200, easing = FastOutLinearInEasing)) { it }
            (enter togetherWith exit).using(null)
        },
        label = "errorSnackbar"
    ) { snackbarData ->
        if (snackbarData == null) return@AnimatedContent

        Snackbar(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .semantics {
                    if (snackbarData == currentSnackbar) liveRegion = LiveRegionMode.Polite
                    dismiss {
                        snackbarData.dismiss()
                        true
                    }
                },
            shape = RoundedCornerShape(6.dp),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            action = snackbarData.visuals.actionLabel?.let { actionLabel ->
                {
                    TextButton(onClick = { snackbarData.performAction() }) {
                        Text(
                            text = actionLabel,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )

                Text(
                    text = snackbarData.visuals.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
