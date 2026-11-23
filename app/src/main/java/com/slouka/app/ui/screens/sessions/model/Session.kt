package com.slouka.app.ui.screens.sessions.model

import com.slouka.app.ui.UiText

data class Session(
    val id: String,
    val device: UiText,
    val details: UiText,
    val lastActive: UiText,
    val isCurrent: Boolean = false,
    val deviceType: SessionDeviceType = SessionDeviceType.PHONE
)
