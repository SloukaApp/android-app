package com.slouka.app.ui.screens.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.slouka.app.R
import com.slouka.app.ui.screens.sessions.model.SessionsViewEffect
import com.slouka.app.ui.screens.sessions.model.SessionsViewEvent
import com.slouka.app.ui.screens.sessions.model.SessionsViewModel
import com.slouka.app.ui.screens.sessions.model.SessionsViewState
import com.slouka.app.ui.views.settings.GroupedListDefaults
import com.slouka.app.ui.views.snackbars.ErrorSnackbarHost
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(state: SessionsViewState, event: (SessionsViewEvent) -> Unit, onBack: () -> Unit,
                   snackbarHostState: SnackbarHostState) {
    val (currentSessions, otherSessions) = remember(state.sessions) { state.sessions.partition { it.isCurrent } }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.account_sessions)) }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.common_back)) }
            })
        },
        snackbarHost = { ErrorSnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(GroupedListDefaults.itemGap)
        ) {
            if (otherSessions.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.sessions_swipe_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            items(currentSessions, key = { it.id }) { session ->
                Box(Modifier.padding(bottom = 16.dp - GroupedListDefaults.itemGap)) {
                    SessionCard(
                        session = session,
                        shape = GroupedListDefaults.itemShape(0, 1),
                        terminating = false,
                        onTerminate = {}
                    )
                }
            }
            itemsIndexed(otherSessions, key = { _, session -> session.id }) { index, session ->
                Box(Modifier.animateItem()) {
                    SessionCard(
                        session = session,
                        shape = GroupedListDefaults.animatedItemShape(index, otherSessions.size),
                        terminating = session.id in state.terminatingIds,
                        onTerminate = { event(SessionsViewEvent.TerminateSession(session.id)) }
                    )
                }
            }
            if (otherSessions.isEmpty()) {
                item { Text(stringResource(R.string.sessions_empty)) }
            }
        }
    }
}

@Composable
fun SessionsScreenRoot(onBack: () -> Unit, viewModel: SessionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val resources = LocalResources.current
    LaunchedEffect(viewModel, lifecycleOwner, resources) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is SessionsViewEffect.ShowError -> this@repeatOnLifecycle.launch {
                        hostState.showSnackbar(
                            resources.getString(effect.message)
                        )
                    }
                }
            }
        }
    }
    SessionsScreen(state, viewModel::onEvent, onBack, hostState)
}
