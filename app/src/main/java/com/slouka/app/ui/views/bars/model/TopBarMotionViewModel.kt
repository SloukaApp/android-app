package com.slouka.app.ui.views.bars.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.motion.MotionRepository
import com.slouka.app.ui.views.backgrounds.IconMotionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TopBarMotionViewModel @Inject constructor(
    private val motionRepository: MotionRepository
) : ViewModel() {
    private val mSettings = MutableStateFlow(MotionSettings())

    val state = this.mSettings
        .flatMapLatest { settings ->
            if (!this.motionRepository.isAvailable) {
                flowOf(TopBarMotionViewState())
            } else if (!settings.tiltEnabled || settings.mode == IconMotionMode.DRIFT) {
                flowOf(TopBarMotionViewState(motionMode = settings.mode))
            } else {
                this.motionRepository.observe(settings.rotation).map {
                    TopBarMotionViewState(
                        motionMode = if (it.isAvailable) settings.mode else IconMotionMode.DRIFT,
                        x = it.x,
                        y = it.y
                    )
                }
            }
        }
        .stateIn(
            scope = this.viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 0, replayExpirationMillis = 0),
            initialValue = TopBarMotionViewState(
                motionMode = if (this.motionRepository.isAvailable) IconMotionMode.TILT_CONTROL else IconMotionMode.DRIFT
            )
        )

    fun onSettingsChanged(rotation: Int, mode: IconMotionMode, tiltEnabled: Boolean) {
        this.mSettings.value = MotionSettings(rotation, mode, tiltEnabled)
    }
}

private data class MotionSettings(
    val rotation: Int = 0,
    val mode: IconMotionMode = IconMotionMode.TILT_CONTROL,
    val tiltEnabled: Boolean = true
)
