package com.slouka.app.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.slouka.app.R
import com.slouka.app.ui.screens.home.model.HomePage
import com.slouka.app.ui.screens.home.model.HomeViewEffect
import com.slouka.app.ui.screens.home.model.HomeViewEvent
import com.slouka.app.ui.screens.home.model.HomeViewModel
import com.slouka.app.ui.screens.home.model.HomeViewState
import com.slouka.app.ui.screens.home.parts.AccountPageView
import com.slouka.app.ui.screens.home.parts.HomePageView
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.ui.views.decos.AnimatedFlame
import com.slouka.app.ui.views.decos.FlameMode
import com.slouka.app.ui.views.decos.MemberAvatar
import com.slouka.app.ui.views.snackbars.ErrorSnackbarHost
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeViewState,
    event: (HomeViewEvent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    avatarModifier: Modifier = Modifier,
    showPageContent: Boolean = true,
    isInteractive: Boolean = true,
    animateOnEnter: Boolean = false,
    keepAvatarInPlace: Boolean = false
) {
    val showAccount = state.page == HomePage.ACCOUNT
    val pageAppearance = remember { Animatable(if (animateOnEnter || !showPageContent) 0f else 1f) }
    val contentVisible by remember { derivedStateOf { pageAppearance.value > 0f } }
    val appearance = pageAppearance.asState()

    LaunchedEffect(showPageContent) {
        val target = if (showPageContent) 1f else 0f
        if (pageAppearance.value != target) pageAppearance.animateTo(target, tween(320))
    }

    BackHandler(enabled = showAccount && contentVisible && isInteractive) {
        event(HomeViewEvent.PageSelected(HomePage.HOME))
    }

    Scaffold(
        snackbarHost = { ErrorSnackbarHost(snackbarHostState) },
        topBar = {
            val colors = TopAppBarDefaults.topAppBarColors()
            val reveal = Modifier.revealHomeContent(appearance, contentVisible, startOffset = (-32).dp)
            Box {
                Box(Modifier.matchParentSize().then(reveal).background(colors.containerColor))
                CenterAlignedTopAppBar(
                    colors = colors.copy(containerColor = Color.Transparent),
                    title = { Text("Dashboard", modifier = reveal) },
                    navigationIcon = { StreakIndicator(days = state.streakDays, modifier = reveal) },
                    actions = {
                        // Keep the landing point fixed while the rest of the bar slides in.
                        IconButton(
                            modifier = if (keepAvatarInPlace) Modifier else reveal,
                            enabled = contentVisible && isInteractive,
                            onClick = { event(HomeViewEvent.PageSelected(HomePage.ACCOUNT)) }
                        ) {
                            MemberAvatar(contentDescription = "Open account", modifier = avatarModifier)
                        }
                    }
                )
            }
        },
        bottomBar = {
            Column(
                Modifier
                    .revealHomeContent(appearance, contentVisible, startOffset = 32.dp)
                    .imePadding()
            ) {
                HomeSearchField(
                    query = state.query,
                    onQueryChange = { event(HomeViewEvent.QueryChanged(it)) },
                    enabled = contentVisible,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                NavigationBar {
                    NavigationBarItem(
                        selected = !showAccount,
                        onClick = { event(HomeViewEvent.PageSelected(HomePage.HOME)) },
                        enabled = contentVisible,
                        icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = showAccount,
                        onClick = { event(HomeViewEvent.PageSelected(HomePage.ACCOUNT)) },
                        enabled = contentVisible,
                        icon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                        label = { Text("Account") }
                    )
                }
            }
        }
    ) { innerPadding ->
        val pageModifier = Modifier
            .padding(innerPadding)
            .revealHomeContent(appearance, contentVisible, startOffset = 8.dp)
        when (state.page) {
            HomePage.HOME -> HomePageView(
                flameMode = state.flameMode,
                animateFlameChanges = state.animateFlameChanges,
                onFlameModeChange = { event(HomeViewEvent.FlameModeChanged(it)) },
                onFlameTransitionsChange = { event(HomeViewEvent.FlameTransitionsChanged(it)) },
                enabled = contentVisible,
                modifier = pageModifier
            )
            HomePage.ACCOUNT -> AccountPageView(
                onSessionsClick = { event(HomeViewEvent.SessionsClicked) },
                onLogoutClick = { event(HomeViewEvent.LogoutClicked) },
                isLoggingOut = state.isLoggingOut,
                enabled = contentVisible,
                modifier = pageModifier
            )
        }
    }
}

private fun Modifier.revealHomeContent(
    appearance: State<Float>,
    visible: Boolean,
    startOffset: Dp
): Modifier =
    this
        .then(if (visible) Modifier else Modifier.clearAndSetSemantics { hideFromAccessibility() })
        .graphicsLayer {
            alpha = appearance.value
            translationY = startOffset.toPx() * (1f - alpha)
        }

@Composable
private fun StreakIndicator(days: Int, modifier: Modifier = Modifier) {
    val description = pluralStringResource(R.plurals.streak_days, days, days)

    Row(
        modifier = modifier
            .padding(start = 12.dp)
            .clearAndSetSemantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        AnimatedFlame(
            mode = if (days > 0) FlameMode.BURNING_STATIC else FlameMode.REST,
            modifier = Modifier.size(24.dp)
        )
        Text(text = days.toString(), style = MaterialTheme.typography.labelLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    SearchBarDefaults.InputField(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { focusManager.clearFocus() },
        expanded = false,
        onExpandedChange = {},
        enabled = enabled,
        placeholder = { Text("Search words") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(enabled = enabled, onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                }
            }
        },
        colors = SearchBarDefaults.colors().inputFieldColors,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun HomeScreenRoot(
    onSessionsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    avatarModifier: Modifier = Modifier,
    showPageContent: Boolean = true,
    isInteractive: Boolean = true,
    animateOnEnter: Boolean = false,
    keepAvatarInPlace: Boolean = false
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigateToSessions by rememberUpdatedState(onSessionsClick)
    val allowNavigation by rememberUpdatedState(isInteractive)
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    HomeViewEffect.NavigateToSessions -> if (allowNavigation) navigateToSessions()
                    is HomeViewEffect.ShowError -> this@repeatOnLifecycle.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
            }
        }
    }

    HomeScreen(
        state = state,
        event = { if (isInteractive) viewModel.onEvent(it) },
        snackbarHostState = snackbarHostState,
        avatarModifier = avatarModifier,
        showPageContent = showPageContent,
        isInteractive = isInteractive,
        animateOnEnter = animateOnEnter,
        keepAvatarInPlace = keepAvatarInPlace
    )
}

@Preview(name = "Home tab", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SloukaTheme {
        HomeScreen(state = HomeViewState(streakDays = 7), event = {})
    }
}

@Preview(name = "Account tab", showBackground = true)
@Composable
private fun HomeScreenAccountPreview() {
    SloukaTheme {
        HomeScreen(state = HomeViewState(page = HomePage.ACCOUNT, query = "apple"), event = {})
    }
}
