package com.slouka.app.ui.screens.home.model

sealed interface HomeViewEffect {
    data object NavigateToSessions : HomeViewEffect
    data class ShowError(val message: String) : HomeViewEffect
}
