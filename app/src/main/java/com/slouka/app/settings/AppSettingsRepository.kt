package com.slouka.app.settings

import android.content.Context
import androidx.core.content.edit
import com.slouka.app.ui.views.decos.FlameMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class AppSettingsRepository @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val mSettings = MutableStateFlow(
        AppSettings(
            flameMode = FlameMode.entries.firstOrNull { it.name == prefs.getString(FLAME_MODE, null) }
                ?: FlameMode.BURNING,
            animateFlameChanges = prefs.getBoolean(ANIMATE_FLAME_CHANGES, true)
        )
    )
    val settings = this.mSettings.asStateFlow()

    fun setFlameMode(mode: FlameMode) {
        this.prefs.edit { putString(FLAME_MODE, mode.name) }
        this.mSettings.update { it.copy(flameMode = mode) }
    }

    fun setAnimateFlameChanges(enabled: Boolean) {
        this.prefs.edit { putBoolean(ANIMATE_FLAME_CHANGES, enabled) }
        this.mSettings.update { it.copy(animateFlameChanges = enabled) }
    }

    private companion object {
        const val FLAME_MODE = "flame_mode"
        const val ANIMATE_FLAME_CHANGES = "animate_flame_changes"
    }
}
