package com.slouka.app.ui.screens.sessions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.ui.graphics.TransformOrigin
import kotlinx.coroutines.coroutineScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Tablet
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import com.slouka.app.ui.screens.sessions.model.SessionDeviceType
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.slouka.app.R
import com.slouka.app.ui.screens.sessions.model.Session
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random
import kotlinx.coroutines.launch

@Composable
internal fun SessionCard(
    session: Session,
    terminating: Boolean,
    shape: Shape,
    onTerminate: () -> Unit
) {
    var useHeel by remember { mutableStateOf(false) }
    var width by remember { mutableIntStateOf(1) }
    var offset by remember { mutableFloatStateOf(0f) }
    var kicking by remember { mutableStateOf(false) }
    var preparing by remember { mutableStateOf(false) }
    var dragDistance by remember { mutableFloatStateOf(0f) }
    var strikeShift by remember { mutableFloatStateOf(0f) }
    var flight by remember { mutableFloatStateOf(0f) }
    var kickDirection by remember { mutableFloatStateOf(-1f) }
    val kick = remember { Animatable(0f) }
    val footVisibility = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val footSize = 36.dp

    fun selectShoe() {
        useHeel = Random.nextInt(5) == 0
    }

    fun swingShift(cardOffset: Float, direction: Float): Float {
        val toeReach = with(density) {
            val toeX = (if (useHeel) 3f else 2f) / 24f
            val ankleX = 0.83f
            val reach = if (direction > 0) 2f * ankleX - toeX else 1f - toeX
            (footSize * reach - 8.dp).toPx()
        }
        val overlap = with(density) { 8.dp.toPx() }
        return cardOffset + direction * (overlap - toeReach)
    }

    suspend fun dismiss(direction: Float, prepare: Boolean = false) {
        if (preparing || kicking || terminating) return
        if (prepare) selectShoe()
        footVisibility.snapTo(1f)
        if (prepare) {
            preparing = true
            animate(offset, direction * width * 0.22f, animationSpec = tween(240)) { value, _ ->
                offset = value
                dragDistance = value / 0.22f
            }
        }
        // Position the swing once, keeping its pivot fixed through the whole kick.
        // The toe reaches slightly beneath the card at the end of the forward swing.
        strikeShift = swingShift(offset, direction)
        kickDirection = direction
        kicking = true
        preparing = false
        kick.snapTo(0f)
        // Swing around the fixed virtual leg, reaching the card with the sole horizontal.
        kick.animateTo(-0.55f, tween(160))
        kick.animateTo(1f, tween(140, easing = FastOutLinearInEasing))
        haptics.performHapticFeedback(HapticFeedbackType.GestureEnd)
        coroutineScope {
            launch {
                kick.animateTo(0f, tween(160))
                footVisibility.animateTo(0f, tween(240))
            }
            val startOffset = offset
            val targetOffset = direction * width * 1.2f
            animate(0f, 1f, animationSpec = tween(360)) { progress, _ ->
                flight = progress
                offset = startOffset + (targetOffset - startOffset) * progress
            }
        }
        onTerminate()
        dragDistance = 0f
        kicking = false
    }

    LaunchedEffect(terminating, kicking, preparing) {
        if (!terminating && !kicking && !preparing && offset != 0f) {
            val startOffset = offset
            val startFlight = flight
            animate(0f, 1f, animationSpec = tween(250)) { progress, _ ->
                offset = startOffset * (1f - progress)
                flight = startFlight * (1f - progress)
            }
        }
    }

    Box(
        Modifier.fillMaxWidth().onSizeChanged { width = it.width }.zIndex(if (kicking) 1f else 0f)
    ) {
        if (offset != 0f || kicking) {
            val direction = if (kicking) kickDirection else offset.sign
            val reveal = if (kicking) footVisibility.value else (abs(dragDistance) / (width * 0.35f)).coerceIn(0f, 1f)
            val swingOffset = if (kicking) strikeShift else swingShift(offset, direction)
            // Clip the foot at the wall and draw the card over it to protect its contents.
            Box(
                Modifier.matchParentSize()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.errorContainer)
            ) {
                Icon(
                    painterResource(if (useHeel) R.drawable.ic_shoe_heel else R.drawable.ic_shoe_formal),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.align(if (direction > 0) Alignment.CenterStart else Alignment.CenterEnd)
                        .size(footSize)
                        .graphicsLayer {
                            scaleX = -direction
                            // The virtual leg stays above the icon, defining the swing radius.
                            transformOrigin = TransformOrigin(0.83f, -0.40f)
                            translationX = -direction * (
                                size.width * 1.5f * (1f - reveal) + 8.dp.toPx() * reveal
                            )
                            translationX += swingOffset * reveal
                            translationY = size.height * 0.35f * (1f - reveal)
                            rotationZ = direction * 40f * (1f - kick.value)
                        }
                )
            }
        }
        Card(
            shape = shape,
            modifier = Modifier.fillMaxWidth()
                .graphicsLayer {
                    translationX = offset
                    translationY = -size.height * 2.5f * flight * (2f - flight)
                    rotationZ = -offset.sign * 12f * flight
                }
                .draggable(
                    state = rememberDraggableState {
                        dragDistance = (dragDistance + it).coerceIn(-width.toFloat(), width.toFloat())
                        offset = dragDistance * 0.22f
                    },
                    orientation = Orientation.Horizontal,
                    enabled = !session.isCurrent && !preparing && !kicking && !terminating,
                    onDragStarted = {
                        selectShoe()
                        dragDistance = offset / 0.22f
                    },
                    onDragStopped = {
                        if (abs(dragDistance) >= width * 0.35f) dismiss(dragDistance.sign)
                        else {
                            animate(offset, 0f, animationSpec = tween(200)) { value, _ -> offset = value }
                            dragDistance = 0f
                        }
                    }
                )
        ) {
            Row(
                Modifier.heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val deviceIcon = when (session.deviceType) {
                    SessionDeviceType.PHONE -> Icons.Rounded.Smartphone
                    SessionDeviceType.TABLET -> Icons.Rounded.Tablet
                    SessionDeviceType.DESKTOP -> Icons.Rounded.Computer
                    SessionDeviceType.LAPTOP -> Icons.Rounded.Laptop
                }
                Icon(
                    deviceIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        session.device,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${session.details} · ${if (session.isCurrent) "Current session" else session.lastActive}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!session.isCurrent) {
                    IconButton(
                        enabled = !preparing && !kicking && !terminating,
                        onClick = { scope.launch { dismiss(-1f, prepare = true) } }
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "End session on ${session.device}")
                    }
                }
            }
        }

    }
}
