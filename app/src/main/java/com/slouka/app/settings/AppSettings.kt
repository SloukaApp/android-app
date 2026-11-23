package com.slouka.app.settings

import com.slouka.app.ui.views.decos.FlameMode

data class AppSettings(
    val flameMode: FlameMode = FlameMode.BURNING,
    val animateFlameChanges: Boolean = true
)
