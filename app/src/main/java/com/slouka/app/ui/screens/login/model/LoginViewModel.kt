package com.slouka.app.ui.screens.login.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slouka.app.account.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository
) : ViewModel() {
    private val mViewEffect = Channel<LoginViewEffect>()
    private val mViewState = MutableStateFlow(LoginViewState())

    val effect = this.mViewEffect.receiveAsFlow()
    val state = this.mViewState.asStateFlow()

    fun onEvent(event: LoginViewEvent) {
        when (event) {
            is LoginViewEvent.EmailChanged -> {
                this.mViewState.update { it.copy(email = event.email, isLoading = false) }
            }
            is LoginViewEvent.LoginClicked -> this.login(event.password)
        }
    }

    fun login(password: String) {
        this.mViewState.update { it.copy(isLoading = true) }
        this.viewModelScope.launch {
            val result = this@LoginViewModel.accountRepository.login(
                email = this@LoginViewModel.state.value.email,
                password = password
            )

            result.onSuccess {
                this@LoginViewModel.mViewState.update {
                    it.copy(isLoading = false, isLoginSuccessful = true)
                }
                this@LoginViewModel.mViewEffect.send(LoginViewEffect.NavigateToHome)
            }.onFailure {
                this@LoginViewModel.mViewState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = false,
                        error = "Email or password is incorrect. Please try again."
                    )
                }
            }
        }
    }
}
