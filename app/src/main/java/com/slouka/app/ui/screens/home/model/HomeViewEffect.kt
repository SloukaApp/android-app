package com.slouka.app.ui.screens.home.model

sealed interface HomeViewEffect {
    data object NavigateToAccount : HomeViewEffect
    data object NavigateToAppSettings : HomeViewEffect
}
