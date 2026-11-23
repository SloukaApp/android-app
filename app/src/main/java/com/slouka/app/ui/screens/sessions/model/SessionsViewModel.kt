package com.slouka.app.ui.screens.sessions.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.R
import com.slouka.app.ui.UiText.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SessionsViewModel @Inject constructor() : ViewModel() {
    private val mViewState = MutableStateFlow(SessionsViewState(sessions = listOf(
        Session(
            id = "current",
            device = Resource(R.string.sessions_demo_device_current),
            details = Resource(R.string.sessions_demo_client_android),
            lastActive = Resource(R.string.sessions_demo_active_now),
            isCurrent = true
        ),
        Session(
            id = "tablet",
            device = Resource(R.string.sessions_demo_device_tablet),
            details = Resource(R.string.sessions_demo_client_android),
            lastActive = Resource(R.string.sessions_demo_tablet_last_active),
            deviceType = SessionDeviceType.TABLET
        ),
        Session(
            id = "chrome",
            device = Resource(R.string.sessions_demo_device_chrome),
            details = Resource(R.string.sessions_demo_client_web),
            lastActive = Resource(R.string.sessions_demo_chrome_last_active),
            deviceType = SessionDeviceType.DESKTOP
        ),
        Session(
            id = "iphone",
            device = Resource(R.string.sessions_demo_device_iphone),
            details = Resource(R.string.sessions_demo_client_ios),
            lastActive = Resource(R.string.sessions_demo_iphone_last_active)
        ),
        Session(
            id = "mac",
            device = Resource(R.string.sessions_demo_device_safari),
            details = Resource(R.string.sessions_demo_client_web),
            lastActive = Resource(R.string.sessions_demo_safari_last_active),
            deviceType = SessionDeviceType.LAPTOP
        )
    )))
    private val mViewEffect = Channel<SessionsViewEffect>(Channel.BUFFERED)

    val state = mViewState.asStateFlow()
    val effect = mViewEffect.receiveAsFlow()

    fun onEvent(event: SessionsViewEvent) {
        when (event) {
            is SessionsViewEvent.TerminateSession -> terminateSession(event.id)
        }
    }

    private fun terminateSession(id: String) {
        val current = state.value
        val session = current.sessions.find { it.id == id } ?: return
        if (session.isCurrent || id in current.terminatingIds) return

        mViewState.update { it.copy(terminatingIds = it.terminatingIds + id) }
        viewModelScope.launch {
            try {
                revokeSession(id)
                mViewState.update { it.copy(sessions = it.sessions.filterNot { item -> item.id == id }) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                mViewEffect.send(SessionsViewEffect.ShowError(R.string.sessions_error_end))
            } finally {
                mViewState.update { it.copy(terminatingIds = it.terminatingIds - id) }
            }
        }
    }

    private suspend fun revokeSession(id: String) {
        // TODO: Inject a sessions repository and call its suspend revoke method here.
        // Throw on failure so that the card returns and the screen shows an error.
    }
}
