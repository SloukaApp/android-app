package com.slouka.app.ui.screens.sessions.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        Session("current", "This Android device", "Slouka · Android", "Active now", isCurrent = true),
        Session("tablet", "Android tablet", "Slouka · Android", "Today, 10:24", deviceType = SessionDeviceType.TABLET),
        Session("chrome", "Chrome on Windows", "Web session", "Yesterday, 18:42", deviceType = SessionDeviceType.DESKTOP),
        Session("iphone", "iPhone", "Slouka · iOS", "November 15, 12:10"),
        Session("mac", "Safari on Mac", "Web session", "November 12, 09:35", deviceType = SessionDeviceType.LAPTOP)
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
                mViewEffect.send(SessionsViewEffect.ShowError("Could not end this session. Please try again."))
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
