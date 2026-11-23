package com.slouka.app.ui.views.bars

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.slouka.app.R
import com.slouka.app.ui.views.backgrounds.IconBackgroundVisibility
import com.slouka.app.ui.views.backgrounds.IconMotionMode
import com.slouka.app.ui.views.backgrounds.IconSelectionMode
import com.slouka.app.ui.views.backgrounds.TopBarIconBackground
import com.slouka.app.ui.views.backgrounds.iconBackgroundFadeSpec
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressTopAppBar(
    title: String,
    steps: IntArray,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
    backgroundIconSize: Dp = 48.dp,
    backgroundIcons: List<ImageVector> = listOf(Icons.Rounded.Key, Icons.Rounded.Lock, Icons.Rounded.Person),
    iconSelectionMode: IconSelectionMode = IconSelectionMode.PATTERN,
    iconRandomSeed: Int = 0,
    motionMode: IconMotionMode = IconMotionMode.TILT_CONTROL,
    tiltEnabled: Boolean = true,
    tiltSensitivity: Float = 2.8f,
    parallaxDistance: Dp = 10.dp,
    background: (@Composable BoxScope.() -> Unit)? = null,
    showBackground: Boolean = true,
    backgroundEnterDelayMillis: Long = 0,
    visibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    onProgressIndeterminateChanged: (Boolean) -> Unit = {},
    navigationIcon: @Composable (() -> Unit) = {}
) {
    var navigationIconWidth by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val expanded by remember(scrollBehavior) {
        derivedStateOf { scrollBehavior.state.collapsedFraction < 0.98f }
    }
    var backgroundReady by remember(backgroundEnterDelayMillis) {
        mutableStateOf(backgroundEnterDelayMillis <= 0)
    }
    LaunchedEffect(backgroundEnterDelayMillis) {
        delay(backgroundEnterDelayMillis.milliseconds)
        backgroundReady = true
    }

    val screenTransition = visibilityScope?.transition
    val navigationAppearance = screenTransition?.animateFloat(
        transitionSpec = { iconBackgroundFadeSpec(durationMillis = 500) },
        label = "navigation_icon_background"
    ) { state -> if (state == EnterExitState.PostExit) 0f else 1f }
    val backgroundVisible = backgroundReady && showBackground && expanded &&
        (screenTransition != null || lifecycleState.isAtLeast(Lifecycle.State.STARTED))
    val visibilityFraction = {
        (1f - scrollBehavior.state.collapsedFraction) * (navigationAppearance?.value ?: 1f)
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        val backgroundModifier = Modifier.matchParentSize().then(
            if (sharedTransitionScope != null) {
                // Keep the layer in the overlay before navigation starts so the new surface
                // cannot cover the outgoing icons between frames.
                with(sharedTransitionScope) {
                    Modifier.renderInSharedTransitionScopeOverlay(
                        zIndexInOverlay = 1f,
                        renderInOverlay = { true }
                    )
                }
            } else {
                Modifier
            }
        )

        if (background != null) {
            IconBackgroundVisibility(
                visible = backgroundVisible,
                modifier = backgroundModifier,
                visibilityFraction = visibilityFraction
            ) { appearance ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = appearance.value
                            scaleX = alpha
                            scaleY = alpha
                        },
                    content = background
                )
            }
        } else {
            TopBarIconBackground(
                icons = backgroundIcons,
                visible = backgroundVisible,
                visibilityFraction = visibilityFraction,
                iconSize = backgroundIconSize,
                motionMode = motionMode,
                tiltEnabled = tiltEnabled,
                tiltSensitivity = tiltSensitivity,
                parallaxDistance = parallaxDistance,
                selectionMode = iconSelectionMode,
                randomSeed = iconRandomSeed,
                modifier = backgroundModifier
            )
        }

        LargeTopAppBar(
            title = {},
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            ),
            navigationIcon = {
                Box(Modifier.onSizeChanged { navigationIconWidth = it.width }) {
                    navigationIcon()
                }
            }
        )

        CollapsingTopBarTitle(
            title = title,
            scrollBehavior = scrollBehavior,
            navigationIconWidth = { navigationIconWidth },
            modifier = Modifier.matchParentSize().windowInsetsPadding(TopAppBarDefaults.windowInsets)
        )

        AdvancedLinearProgressBar(
            steps = steps,
            isVisible = showProgress,
            onIndeterminateChanged = onProgressIndeterminateChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingTopBarTitle(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    navigationIconWidth: () -> Int,
    modifier: Modifier = Modifier
) {
    val expandedStyle = MaterialTheme.typography.headlineMedium
    val collapsedStyle = MaterialTheme.typography.titleLarge
    val collapsedScale = collapsedStyle.fontSize.value / expandedStyle.fontSize.value
    val collapsedHeight = TopAppBarDefaults.LargeAppBarCollapsedHeight
    val expandedHeight = TopAppBarDefaults.LargeAppBarExpandedHeight

    Layout(
        modifier = modifier,
        content = {
            Text(
                text = title,
                style = expandedStyle.copy(textMotion = TextMotion.Animated),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics { heading() }
            )
        }
    ) { measurables, constraints ->
        val expandedStart = 16.dp.roundToPx()
        val collapsedStart = maxOf(expandedStart, navigationIconWidth() + 8.dp.roundToPx())
        val end = 8.dp.roundToPx()
        val titleWidth = minOf(
            constraints.maxWidth - expandedStart - end,
            ((constraints.maxWidth - collapsedStart - end) / collapsedScale).toInt()
        ).coerceAtLeast(0)
        val text = measurables.single().measure(
            constraints.copy(minWidth = 0, maxWidth = titleWidth, minHeight = 0)
        )

        layout(constraints.maxWidth, constraints.maxHeight) {
            val fraction = scrollBehavior.state.collapsedFraction.coerceIn(0f, 1f)
            val scale = lerp(1f, collapsedScale, fraction)
            val expandedY = expandedHeight.toPx() - 28.dp.toPx() - text[LastBaseline]
            val collapsedY = (collapsedHeight.toPx() - text.height * scale) / 2f
            val x = lerp(expandedStart.toFloat(), collapsedStart.toFloat(), fraction)
            val y = lerp(expandedY, collapsedY, fraction)

            text.placeRelativeWithLayer(x.roundToInt(), y.roundToInt()) {
                transformOrigin = TransformOrigin(
                    pivotFractionX = if (layoutDirection == LayoutDirection.Ltr) 0f else 1f,
                    pivotFractionY = 0f
                )
                scaleX = scale
                scaleY = scale
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ProgressTopAppBarPreview() {
    ProgressTopAppBar(
        title = stringResource(R.string.preview_top_bar_title),
        steps = IntArray(1) { -1 },
        showProgress = true,
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    )
}
