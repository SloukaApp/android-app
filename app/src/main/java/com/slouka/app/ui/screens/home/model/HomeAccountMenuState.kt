package com.slouka.app.ui.screens.home.model

import com.slouka.app.ui.UiText

data class HomeAccountMenuState(
    val isOpen: Boolean = false,
    val logoutPhase: LogoutPhase = LogoutPhase.IDLE,
    val logoutError: UiText? = null,
    val destination: AccountMenuDestination? = null
) {
    val isLoggingOut: Boolean
        get() = logoutPhase != LogoutPhase.IDLE
}
