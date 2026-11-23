package com.slouka.app.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import com.slouka.app.ui.screens.home.parts.AccountQuickMenu
import com.slouka.app.ui.screens.home.parts.DecksPageView
import com.slouka.app.ui.screens.home.parts.DictionaryPageView
import com.slouka.app.ui.screens.home.parts.HomePageView
import com.slouka.app.ui.screens.home.parts.centerOnScreen
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.ui.views.bars.AnimatedNavigationIcon
import com.slouka.app.ui.views.decos.AnimatedFlame
import com.slouka.app.ui.views.decos.FlameMode
import com.slouka.app.ui.views.decos.MemberAvatar

private val HomeNavigationPages = listOf(HomePage.HOME, HomePage.DECKS, HomePage.DICTIONARY)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeViewState,
    event: (HomeViewEvent) -> Unit,
    avatarModifier: Modifier = Modifier,
    showPageContent: Boolean = true,
    isInteractive: Boolean = true,
    finishingLogout: Boolean = false,
    animateOnEnter: Boolean = false,
    keepAvatarInPlace: Boolean = false
) {
    val pageAppearance = remember { Animatable(if (animateOnEnter || !showPageContent) 0f else 1f) }
    val contentVisible by remember { derivedStateOf { pageAppearance.value > 0f } }
    val appearance = pageAppearance.asState()
    val ready by remember { derivedStateOf { pageAppearance.value == 1f } }
    val showAccountMenu = isInteractive || finishingLogout
    val accountMenuMotion = remember { Animatable(0f) }
    var accountAvatarOrigin by remember { mutableStateOf(Offset.Unspecified) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(showPageContent) {
        val target = if (showPageContent) 1f else 0f
        if (pageAppearance.value != target) pageAppearance.animateTo(target, tween(320))
    }

    BackHandler(enabled = state.page != HomePage.HOME && contentVisible && isInteractive) {
        event(HomeViewEvent.PageSelected(HomePage.HOME))
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                val colors = TopAppBarDefaults.topAppBarColors()
                val reveal = Modifier.revealHomeContent(appearance, contentVisible, startOffset = (-32).dp)
                Box {
                    Box(Modifier.matchParentSize().then(reveal).background(colors.containerColor))
                    CenterAlignedTopAppBar(
                        colors = colors.copy(containerColor = Color.Transparent),
                        title = {
                            val title = when (state.page) {
                                HomePage.DECKS -> R.string.home_page_decks
                                HomePage.DICTIONARY -> R.string.home_page_dictionary
                                HomePage.HOME, HomePage.ACCOUNT -> R.string.home_page_dashboard
                            }
                            Text(stringResource(title), modifier = reveal)
                        },
                        navigationIcon = { StreakIndicator(days = state.streakDays, modifier = reveal) },
                        actions = {
                            // Keep the landing point fixed while the rest of the bar slides in.
                            IconButton(
                                modifier = if (keepAvatarInPlace) Modifier else reveal,
                                enabled = ready && isInteractive && !state.accountMenu.isLoggingOut,
                                onClick = {
                                    focusManager.clearFocus()
                                    event(HomeViewEvent.OpenAccountMenu)
                                }
                            ) {
                                MemberAvatar(
                                    contentDescription = stringResource(R.string.account_menu_open),
                                    modifier = avatarModifier
                                        .onGloballyPositioned { accountAvatarOrigin = it.centerOnScreen() }
                                        .graphicsLayer {
                                            alpha = if (showAccountMenu && accountMenuMotion.value > 0f) 0f else 1f
                                        }
                                )
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
                        HomeNavigationPages.forEach { page ->
                            val (outlined, filled, label) = when (page) {
                                HomePage.DECKS -> Triple(
                                    R.drawable.ic_nav_decks_rounded_outline,
                                    R.drawable.ic_nav_decks_rounded_filled,
                                    R.string.home_page_decks
                                )
                                HomePage.DICTIONARY -> Triple(
                                    R.drawable.ic_nav_dictionary_rounded_outline,
                                    R.drawable.ic_nav_dictionary_rounded_filled,
                                    R.string.home_page_dictionary
                                )
                                HomePage.HOME, HomePage.ACCOUNT -> Triple(
                                    R.drawable.ic_nav_home_rounded_outline,
                                    R.drawable.ic_nav_home_rounded_filled,
                                    R.string.home_page_home
                                )
                            }
                            val selected = state.page == page
                            NavigationBarItem(
                                selected = selected,
                                onClick = { event(HomeViewEvent.PageSelected(page)) },
                                enabled = contentVisible,
                                icon = { AnimatedNavigationIcon(outlined, filled, selected) },
                                label = { Text(stringResource(label)) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            val pageModifier = Modifier
                .padding(innerPadding)
                .revealHomeContent(appearance, contentVisible, startOffset = 8.dp)
            when (state.page) {
                HomePage.HOME, HomePage.ACCOUNT -> HomePageView(
                    flameMode = state.flameMode,
                    animateFlameChanges = state.animateFlameChanges,
                    onFlameModeChange = { event(HomeViewEvent.FlameModeChanged(it)) },
                    onFlameTransitionsChange = { event(HomeViewEvent.FlameTransitionsChanged(it)) },
                    enabled = contentVisible,
                    modifier = pageModifier
                )
                HomePage.DECKS -> DecksPageView(modifier = pageModifier)
                HomePage.DICTIONARY -> DictionaryPageView(modifier = pageModifier)
            }
        }

        if (showAccountMenu) {
            AccountQuickMenu(
                state = state.accountMenu,
                accountEmail = state.accountEmail,
                avatarOrigin = accountAvatarOrigin,
                motion = accountMenuMotion,
                onEvent = event
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
        placeholder = { Text(stringResource(R.string.home_search_hint)) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(enabled = enabled, onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.home_search_clear))
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
    onManageAccountClick: () -> Unit,
    onAppSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    avatarModifier: Modifier = Modifier,
    showPageContent: Boolean = true,
    isInteractive: Boolean = true,
    finishingLogout: Boolean = false,
    animateOnEnter: Boolean = false,
    keepAvatarInPlace: Boolean = false
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navigateToAccount by rememberUpdatedState(onManageAccountClick)
    val navigateToAppSettings by rememberUpdatedState(onAppSettingsClick)
    val allowNavigation by rememberUpdatedState(isInteractive)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    HomeViewEffect.NavigateToAccount -> if (allowNavigation) navigateToAccount()
                    HomeViewEffect.NavigateToAppSettings -> if (allowNavigation) navigateToAppSettings()
                }
            }
        }
    }

    HomeScreen(
        state = state,
        event = { event ->
            val finishesLogout = event == HomeViewEvent.LogoutArrowFinished || event == HomeViewEvent.LogoutMenuClosed
            if (isInteractive || (finishingLogout && finishesLogout)) viewModel.onEvent(event)
        },
        avatarModifier = avatarModifier,
        showPageContent = showPageContent,
        isInteractive = isInteractive,
        finishingLogout = finishingLogout,
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

@Preview(name = "Dictionary tab", showBackground = true)
@Composable
private fun HomeScreenDictionaryPreview() {
    SloukaTheme {
        HomeScreen(state = HomeViewState(page = HomePage.DICTIONARY, query = stringResource(R.string.preview_search_query)), event = {})
    }
}
