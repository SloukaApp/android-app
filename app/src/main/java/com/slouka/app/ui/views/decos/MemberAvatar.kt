package com.slouka.app.ui.views.decos

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.slouka.app.ui.animations.AuthPanelTransitionMillis

@Composable
internal fun MemberAvatar(
    contentDescription: String?,
    modifier: Modifier = Modifier,
    diameter: Dp = 36.dp
) {
    Box(
        modifier = modifier
            .size(diameter)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.Person,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(diameter * (2f / 3f))
        )
    }
}

@Composable
internal fun SharedTransitionScope.memberAvatarSharedElement(
    visibilityScope: AnimatedVisibilityScope
): Modifier = Modifier.sharedElement(
    sharedContentState = rememberSharedContentState(key = "member_avatar"),
    animatedVisibilityScope = visibilityScope,
    boundsTransform = { _, _ -> tween(AuthPanelTransitionMillis, easing = FastOutSlowInEasing) },
    zIndexInOverlay = 2f
)
