package com.slouka.app.ui.screens.sessions.model

import androidx.annotation.StringRes

sealed interface SessionsViewEffect {
    data class ShowError(@StringRes val message: Int) : SessionsViewEffect
}
