package com.slouka.app.ui.screens.home.model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.account.AccountRepository
import com.slouka.app.account.exceptions.AccountRequestException
import com.slouka.app.ui.views.decos.FlameMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val page = savedStateHandle.getMutableStateFlow("home_page", HomePage.HOME)
    private val query = savedStateHandle.getMutableStateFlow("home_query", "")
    private val flameMode = savedStateHandle.getMutableStateFlow(
        "home_flame_display_mode",
        if (savedStateHandle.get<Boolean>("home_flame_burning") == false) FlameMode.REST
        else FlameMode.BURNING
    )
    private val animateFlameChanges = savedStateHandle.getMutableStateFlow("home_flame_transitions", true)
    private val mIsLoggingOut = MutableStateFlow(false)
    private val mViewEffect = Channel<HomeViewEffect>(Channel.BUFFERED)

    val state = combine(page, query, flameMode, animateFlameChanges, mIsLoggingOut) { page, query, mode, animate, isLoggingOut ->
        HomeViewState(page, query, mode, animate, isLoggingOut = isLoggingOut)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        HomeViewState(page.value, query.value, flameMode.value, animateFlameChanges.value)
    )
    val effect = mViewEffect.receiveAsFlow()

    fun onEvent(event: HomeViewEvent) {
        when (event) {
            is HomeViewEvent.PageSelected -> page.value = event.page
            is HomeViewEvent.QueryChanged -> query.value = event.query
            is HomeViewEvent.FlameModeChanged -> flameMode.value = event.mode
            is HomeViewEvent.FlameTransitionsChanged -> animateFlameChanges.value = event.enabled
            HomeViewEvent.SessionsClicked -> viewModelScope.launch {
                mViewEffect.send(HomeViewEffect.NavigateToSessions)
            }
            HomeViewEvent.LogoutClicked -> this.logout()
        }
    }

    private fun logout() {
        if (this.mIsLoggingOut.value) return
        this.mIsLoggingOut.value = true

        this.viewModelScope.launch {
            try {
                this@HomeViewModel.accountRepository.logout().onFailure { failure ->
                    val message = (failure as? AccountRequestException)?.detailMessage
                        ?: "Could not log out. Please try again."
                    this@HomeViewModel.mViewEffect.send(HomeViewEffect.ShowError(message))
                }
            } finally {
                this@HomeViewModel.mIsLoggingOut.value = false
            }
        }
    }
}
