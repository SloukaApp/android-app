package com.slouka.app.ui.screens.home.model

import com.slouka.app.ui.views.decos.FlameMode

data class HomeViewState(
    val page: HomePage = HomePage.HOME,
    val query: String = "",
    val flameMode: FlameMode = FlameMode.BURNING,
    val animateFlameChanges: Boolean = true,
    val streakDays: Int = 0,
    val isLoggingOut: Boolean = false
)
