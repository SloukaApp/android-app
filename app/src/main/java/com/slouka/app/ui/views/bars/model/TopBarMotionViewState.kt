package com.slouka.app.ui.views.bars.model

import com.slouka.app.ui.views.backgrounds.IconMotionMode

data class TopBarMotionViewState(
    val motionMode: IconMotionMode = IconMotionMode.DRIFT,
    val x: Float = 0f,
    val y: Float = 0f
)
