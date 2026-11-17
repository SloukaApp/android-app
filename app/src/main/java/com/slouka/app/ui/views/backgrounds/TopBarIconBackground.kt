package com.slouka.app.ui.views.backgrounds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.slouka.app.ui.views.bars.model.TopBarMotionViewModel
import com.slouka.app.ui.views.bars.model.TopBarMotionViewState

@Composable
internal fun TopBarIconBackground(
    icons: List<ImageVector>,
    iconSize: Dp,
    motionMode: IconMotionMode,
    tiltEnabled: Boolean,
    tiltSensitivity: Float,
    parallaxDistance: Dp,
    selectionMode: IconSelectionMode,
    randomSeed: Int,
    modifier: Modifier = Modifier,
    visibilityFraction: () -> Float = { 1f },
    visible: Boolean = true
) {
    val motion: State<TopBarMotionViewState> = if (LocalInspectionMode.current) {
        rememberUpdatedState(TopBarMotionViewState(motionMode = motionMode))
    } else {
        val viewModel: TopBarMotionViewModel = hiltViewModel()
        val lifecycleOwner = LocalLifecycleOwner.current
        val view = LocalView.current
        val configuration = LocalConfiguration.current
        val rotation = view.display?.rotation ?: 0

        LaunchedEffect(viewModel, configuration, rotation, motionMode, tiltEnabled) {
            viewModel.onSettingsChanged(rotation, motionMode, tiltEnabled)
        }

        produceState(viewModel.state.value, viewModel, lifecycleOwner, visible) {
            if (visible) {
                lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.state.collect { value = it }
                }
            }
        }
    }
    val mode by remember(motion) { derivedStateOf { motion.value.motionMode } }
    val parallaxPx = with(LocalDensity.current) { parallaxDistance.toPx() }

    if (mode == IconMotionMode.TILT_CONTROL) {
        TiltControlledIconColumns(
            icons = icons,
            iconSize = iconSize,
            sensitivity = tiltSensitivity,
            tilt = { motion.value.let { Offset(it.x, it.y) } },
            selectionMode = selectionMode,
            randomSeed = randomSeed,
            visible = visible,
            visibilityFraction = visibilityFraction,
            modifier = modifier
        )
    } else {
        DiagonalIconColumns(
            icons = icons,
            direction = DiagonalIconDirection.UP_RIGHT,
            iconSize = iconSize,
            parallaxOffset = {
                if (mode == IconMotionMode.PARALLAX) {
                    motion.value.let { Offset(it.x, it.y) * parallaxPx }
                } else {
                    Offset.Zero
                }
            },
            selectionMode = selectionMode,
            randomSeed = randomSeed,
            visible = visible,
            visibilityFraction = visibilityFraction,
            modifier = modifier
        )
    }
}
