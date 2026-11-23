package com.slouka.app.ui.screens.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slouka.app.R
import com.slouka.app.ui.screens.account.model.AccountViewModel
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.ui.views.settings.GroupedList
import com.slouka.app.ui.views.settings.GroupedListDefaults

@Composable
fun AccountScreenRoot(
    onBack: () -> Unit,
    onSessionsClick: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel()
) {
    val email by viewModel.accountEmail.collectAsStateWithLifecycle()
    AccountScreen(email = email, onBack = onBack, onSessionsClick = onSessionsClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountScreen(
    email: String?,
    onBack: () -> Unit,
    onSessionsClick: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.account_menu_manage)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back))
                }
            }
        )
    }) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            if (email != null) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                )
            }
            GroupedList(count = 1) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.account_sessions)) },
                    leadingContent = { Icon(Icons.Rounded.Devices, contentDescription = null) },
                    trailingContent = {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
                    },
                    colors = GroupedListDefaults.itemColors(),
                    modifier = Modifier.clickable(onClick = onSessionsClick)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountScreenPreview() {
    SloukaTheme {
        AccountScreen(email = stringResource(R.string.preview_account_email), onBack = {}, onSessionsClick = {})
    }
}
