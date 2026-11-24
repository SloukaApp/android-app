package com.slouka.app.ui.screens.settings.model

import androidx.lifecycle.ViewModel
import com.slouka.app.settings.AppSettingsRepository
import com.slouka.app.ui.views.decos.FlameMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val repository: AppSettingsRepository
) : ViewModel() {
    val settings = this.repository.settings

    fun setFlameMode(mode: FlameMode) = this.repository.setFlameMode(mode)

    fun setAnimateFlameChanges(enabled: Boolean) = this.repository.setAnimateFlameChanges(enabled)
}
