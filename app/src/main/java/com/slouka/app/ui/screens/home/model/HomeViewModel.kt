package com.slouka.app.ui.screens.home.model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.R
import com.slouka.app.account.AccountRepository
import com.slouka.app.account.exceptions.AccountRequestException
import com.slouka.app.settings.AppSettingsRepository
import com.slouka.app.ui.UiText
import com.slouka.app.words.WordSearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val wordSearchRepository: WordSearchRepository
) : ViewModel() {
    private val page = savedStateHandle.getMutableStateFlow("home_page", HomePage.HOME).apply {
        if (value == HomePage.ACCOUNT) value = HomePage.HOME
    }
    private val query = savedStateHandle.getMutableStateFlow("home_query", "")
    private val mAccountMenu = MutableStateFlow(HomeAccountMenuState())
    val accountMenu = this.mAccountMenu.asStateFlow()

    private val searchState = query.map { it to wordSearchRepository.search(it) }
    private val pageState = combine(page, searchState, appSettingsRepository.settings) { page, search, settings ->
        val (query, results) = search
        HomeViewState(
            page = page,
            query = query,
            flameMode = settings.flameMode,
            animateFlameChanges = settings.animateFlameChanges,
            searchResults = results
        )
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
            accountEmail = accountRepository.accountEmail.value,
            searchResults = wordSearchRepository.search(query.value)
        )
    )
    fun onEvent(event: HomeViewEvent) {
        when (event) {
            is HomeViewEvent.PageSelected -> page.value = event.page
            is HomeViewEvent.QueryChanged -> query.value = event.query
            is HomeViewEvent.FlameModeChanged -> this.appSettingsRepository.setFlameMode(event.mode)
            is HomeViewEvent.FlameTransitionsChanged -> this.appSettingsRepository.setAnimateFlameChanges(event.enabled)
            HomeViewEvent.OpenAccountMenu -> if (!this.mAccountMenu.value.isOpen) {
                this.mAccountMenu.value = HomeAccountMenuState(isOpen = true)
            }
            HomeViewEvent.DismissAccountMenu -> this.mAccountMenu.update { menu ->
                if (!menu.isLoggingOut && menu.destination == null) HomeAccountMenuState() else menu
            }
            is HomeViewEvent.AccountDestinationSelected -> this.mAccountMenu.update { menu ->
                if (menu.isOpen && !menu.isLoggingOut && menu.destination == null) {
                    menu.copy(destination = event.destination)
                } else menu
            }
            HomeViewEvent.AccountDestinationDismissed -> this.mAccountMenu.update { it.copy(destination = null) }
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

    private fun logout() {
        val menu = this.mAccountMenu.value
        if (!menu.isOpen || menu.isLoggingOut || menu.destination != null) return

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
