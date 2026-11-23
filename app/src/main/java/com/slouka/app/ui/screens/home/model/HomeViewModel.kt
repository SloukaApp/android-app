package com.slouka.app.ui.screens.home.model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.R
import com.slouka.app.account.AccountRepository
import com.slouka.app.account.exceptions.AccountRequestException
import com.slouka.app.settings.AppSettingsRepository
import com.slouka.app.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {
    private val page = savedStateHandle.getMutableStateFlow("home_page", HomePage.HOME).apply {
        if (value == HomePage.ACCOUNT) value = HomePage.HOME
    }
    private val query = savedStateHandle.getMutableStateFlow("home_query", "")
    private val mAccountMenu = MutableStateFlow(HomeAccountMenuState())
    val accountMenu = this.mAccountMenu.asStateFlow()
    private val mViewEffect = Channel<HomeViewEffect>(Channel.BUFFERED)

    private val pageState = combine(page, query, appSettingsRepository.settings) { page, query, settings ->
        HomeViewState(page, query, settings.flameMode, settings.animateFlameChanges)
    }

    val state = combine(pageState, mAccountMenu, accountRepository.accountEmail) { pageState, menu, email ->
        pageState.copy(accountMenu = menu, accountEmail = email)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        HomeViewState(
            page = page.value,
            query = query.value,
            flameMode = appSettingsRepository.settings.value.flameMode,
            animateFlameChanges = appSettingsRepository.settings.value.animateFlameChanges,
            accountEmail = accountRepository.accountEmail.value
        )
    )
    val effect = mViewEffect.receiveAsFlow()

    fun onEvent(event: HomeViewEvent) {
        when (event) {
            is HomeViewEvent.PageSelected -> page.value = event.page
            is HomeViewEvent.QueryChanged -> query.value = event.query
            is HomeViewEvent.FlameModeChanged -> this.appSettingsRepository.setFlameMode(event.mode)
            is HomeViewEvent.FlameTransitionsChanged -> this.appSettingsRepository.setAnimateFlameChanges(event.enabled)
            HomeViewEvent.OpenAccountMenu -> if (!this.mAccountMenu.value.isOpen) {
                this.mAccountMenu.value = HomeAccountMenuState(isOpen = true)
            }
            HomeViewEvent.DismissAccountMenu -> this.dismissAccountMenu()
            HomeViewEvent.ManageAccountClicked -> this.openAccountDestination(HomeViewEffect.NavigateToAccount)
            HomeViewEvent.AppSettingsClicked -> this.openAccountDestination(HomeViewEffect.NavigateToAppSettings)
            HomeViewEvent.LogoutClicked -> this.logout()
            HomeViewEvent.LogoutArrowFinished -> {
                val menu = this.mAccountMenu.value
                if (menu.logoutPhase == LogoutPhase.CONFIRMED) {
                    this.mAccountMenu.value = menu.copy(isOpen = false, logoutPhase = LogoutPhase.CLOSING)
                }
            }
            HomeViewEvent.LogoutMenuClosed -> if (this.mAccountMenu.value.logoutPhase == LogoutPhase.CLOSING) {
                this.mAccountMenu.value = HomeAccountMenuState()
            }
        }
    }

    private fun dismissAccountMenu(): Boolean {
        val menu = this.mAccountMenu.value
        if (!menu.isOpen || menu.isLoggingOut) return false

        this.mAccountMenu.value = HomeAccountMenuState()
        return true
    }

    private fun openAccountDestination(effect: HomeViewEffect) {
        if (!this.dismissAccountMenu()) return
        this.viewModelScope.launch { this@HomeViewModel.mViewEffect.send(effect) }
    }

    private fun logout() {
        val menu = this.mAccountMenu.value
        if (!menu.isOpen || menu.isLoggingOut) return

        this.mAccountMenu.value = menu.copy(logoutPhase = LogoutPhase.REQUESTING, logoutError = null)

        this.viewModelScope.launch {
            this@HomeViewModel.mAccountMenu.value = this@HomeViewModel.accountRepository.logout().fold(
                onSuccess = { HomeAccountMenuState(isOpen = true, logoutPhase = LogoutPhase.CONFIRMED) },
                onFailure = { failure ->
                    val message = (failure as? AccountRequestException)?.detailMessage?.let { UiText.Raw(it) }
                        ?: UiText.Resource(R.string.account_menu_error_logout)
                    HomeAccountMenuState(isOpen = true, logoutError = message)
                }
            )
        }
    }
}
