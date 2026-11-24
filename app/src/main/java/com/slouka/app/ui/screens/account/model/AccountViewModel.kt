package com.slouka.app.ui.screens.account.model

import androidx.lifecycle.ViewModel
import com.slouka.app.account.AccountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(repository: AccountRepository) : ViewModel() {
    val accountEmail = repository.accountEmail
}
