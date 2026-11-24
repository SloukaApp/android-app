package com.slouka.app.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources

sealed interface UiText {
    data class Resource(@StringRes val id: Int) : UiText
    data class Raw(val value: String) : UiText

    fun resolve(resources: Resources): String = when (this) {
        is Resource -> resources.getString(id)
        is Raw -> value
    }

    @Composable
    fun resolve(): String = resolve(LocalResources.current)
}
