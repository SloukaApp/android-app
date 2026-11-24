package com.slouka.app.ui.screens.settings

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slouka.app.R
import com.slouka.app.settings.AppSettings
import com.slouka.app.ui.screens.settings.model.AppSettingsViewModel
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.ui.views.decos.AnimatedFlame
import com.slouka.app.ui.views.decos.FlameMode
import com.slouka.app.ui.views.settings.FlameModeSelector
import com.slouka.app.ui.views.settings.GroupedList
import com.slouka.app.ui.views.settings.GroupedListDefaults

@Composable
fun AppSettingsScreenRoot(
    onBack: () -> Unit,
    viewModel: AppSettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    AppSettingsScreen(
        settings = settings,
        onBack = onBack,
        onFlameModeChange = viewModel::setFlameMode,
        onFlameTransitionsChange = viewModel::setAnimateFlameChanges
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSettingsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onFlameModeChange: (FlameMode) -> Unit,
    onFlameTransitionsChange: (Boolean) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.app_settings_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.common_back))
                }
            }
        )
    }) { innerPadding ->
        GroupedList(
            count = 2,
            title = stringResource(R.string.app_settings_flame),
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) { index ->
            when (index) {
                0 -> ListItem(
                    headlineContent = { Text(stringResource(R.string.app_settings_flame_mode)) },
                    leadingContent = {
                        AnimatedFlame(
                            mode = settings.flameMode,
                            modifier = Modifier.size(28.dp),
                            animateStateChanges = settings.animateFlameChanges
                        )
                    },
                    supportingContent = {
                        FlameModeSelector(
                            flameMode = settings.flameMode,
                            onFlameModeChange = onFlameModeChange,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    },
                    colors = GroupedListDefaults.itemColors()
                )
                else -> ListItem(
                    headlineContent = { Text(stringResource(R.string.app_settings_flame_transitions)) },
                    trailingContent = { Switch(checked = settings.animateFlameChanges, onCheckedChange = null) },
                    colors = GroupedListDefaults.itemColors(),
                    modifier = Modifier.toggleable(
                        value = settings.animateFlameChanges,
                        role = Role.Switch,
                        onValueChange = onFlameTransitionsChange
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AppSettingsScreenPreview() {
    SloukaTheme {
        AppSettingsScreen(AppSettings(), onBack = {}, onFlameModeChange = {}, onFlameTransitionsChange = {})
    }
}
