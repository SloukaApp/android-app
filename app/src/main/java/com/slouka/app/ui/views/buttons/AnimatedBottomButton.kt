package com.slouka.app.ui.views.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedBottomButton(
    visible: Boolean,
    onClick: () -> Unit,
    text: String,
    enabled: Boolean,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
    onEnterFinished: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 250)
        ) + fadeOut(),
        modifier = modifier
    ) {
        LaunchedEffect(visible, transition.currentState) {
            if (visible && transition.currentState == EnterExitState.Visible) {
                onEnterFinished()
            }
        }

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = bottomPadding)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            enabled = enabled
        ) {
            Text(text)
        }
    }
}
