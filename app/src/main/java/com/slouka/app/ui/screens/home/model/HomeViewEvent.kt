package com.slouka.app.ui.screens.home.model

import com.slouka.app.ui.views.decos.FlameMode

sealed interface HomeViewEvent {
    data class PageSelected(val page: HomePage) : HomeViewEvent
    data class QueryChanged(val query: String) : HomeViewEvent
    data class FlameModeChanged(val mode: FlameMode) : HomeViewEvent
    data class FlameTransitionsChanged(val enabled: Boolean) : HomeViewEvent
    data object OpenAccountMenu : HomeViewEvent
    data object DismissAccountMenu : HomeViewEvent
    data object ManageAccountClicked : HomeViewEvent
    data object AppSettingsClicked : HomeViewEvent
    data object LogoutClicked : HomeViewEvent
    data object LogoutArrowFinished : HomeViewEvent
    data object LogoutMenuClosed : HomeViewEvent
}
