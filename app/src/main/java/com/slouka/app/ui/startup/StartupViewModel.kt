package com.slouka.app.ui.startup

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.account.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.ProviderException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {
    private val mState = MutableStateFlow(StartupState.CHECKING)
    val state = mState.asStateFlow()
    var allowHome by mutableStateOf(false)
        private set
    var animateLogout by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val sessionResets = sessionManager.sessionResets
            val initialReset = sessionResets.value
            val hasSession = withContext(Dispatchers.IO) { hasReadableSession() }
            allowHome = hasSession && sessionResets.value == initialReset
            mState.value = if (allowHome) {
                StartupState.SIGNED_IN
            } else StartupState.SIGNED_OUT

            sessionResets.dropWhile { it == initialReset }.collect {
                animateLogout = it.isLogout
                allowHome = false
            }
        }
    }

    fun onHomeOpened() {
        animateLogout = false
        allowHome = true
    }

    private fun hasReadableSession(): Boolean = try {
        sessionManager.hasStoredSession()
    } catch (failure: GeneralSecurityException) {
        unreadableSession()
    } catch (failure: IOException) {
        unreadableSession()
    } catch (failure: ProviderException) {
        unreadableSession()
    } catch (failure: IllegalArgumentException) {
        unreadableSession()
    } catch (failure: ClassCastException) {
        unreadableSession()
    }

    private fun unreadableSession(): Boolean {
        Log.w("StartupViewModel", "Could not read the saved session")
        return false
    }
}
