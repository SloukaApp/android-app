package com.slouka.app.ui.screens.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.slouka.app.ui.screens.sessions.model.SessionsViewEffect
import com.slouka.app.ui.screens.sessions.model.SessionsViewEvent
import com.slouka.app.ui.screens.sessions.model.SessionsViewModel
import com.slouka.app.ui.screens.sessions.model.SessionsViewState
import com.slouka.app.ui.views.snackbars.ErrorSnackbarHost
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(state: SessionsViewState, event: (SessionsViewEvent) -> Unit, onBack: () -> Unit,
                   snackbarHostState: SnackbarHostState) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sessions") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            })
        },
        snackbarHost = { ErrorSnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    "Swipe a session to end it.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            itemsIndexed(state.sessions, key = { _, session -> session.id }) { index, session ->
                val first = index == 0
                val last = index == state.sessions.lastIndex
                val shape = RoundedCornerShape(
                    topStart = if (first) 16.dp else 0.dp,
                    topEnd = if (first) 16.dp else 0.dp,
                    bottomStart = if (last) 16.dp else 0.dp,
                    bottomEnd = if (last) 16.dp else 0.dp
                )
                Column(Modifier.animateItem()) {
                    SessionCard(
                        session = session,
                        shape = shape,
                        terminating = session.id in state.terminatingIds,
                        onTerminate = { event(SessionsViewEvent.TerminateSession(session.id)) }
                    )
                    if (!last) {
                        Box(Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 52.dp, end = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
            if (state.sessions.none { !it.isCurrent }) {
                item { Text("No other active sessions.") }
            }
        }
    }
}

@Composable
fun SessionsScreenRoot(onBack: () -> Unit, viewModel: SessionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is SessionsViewEffect.ShowError -> this@repeatOnLifecycle.launch {
                        hostState.showSnackbar(
                            effect.message
                        )
                    }
                }
            }
        }
    }
    SessionsScreen(state, viewModel::onEvent, onBack, hostState)
}
