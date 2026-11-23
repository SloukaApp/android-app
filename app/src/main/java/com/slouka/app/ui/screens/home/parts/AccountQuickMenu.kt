package com.slouka.app.ui.screens.home.parts

import android.view.Gravity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.slouka.app.R
import com.slouka.app.ui.screens.home.model.HomeAccountMenuState
import com.slouka.app.ui.screens.home.model.HomeViewEvent
import com.slouka.app.ui.screens.home.model.LogoutPhase
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.ui.views.decos.AnimatedLogoutIcon
import com.slouka.app.ui.views.decos.MemberAvatar
import com.slouka.app.ui.views.settings.GroupedList
import com.slouka.app.ui.views.settings.GroupedListDefaults

private val AvatarFlightSize = 62.dp
private val AccountAvatarSize = 52.dp
private val AccountMenuInset = 12.dp
private val AccountMenuMargin = 16.dp
private val AccountMenuHeaderHeight = 60.dp
private const val DialogAvatarHandoff = 0.40f

@Composable
internal fun BoxScope.AccountQuickMenu(
    state: HomeAccountMenuState,
    accountEmail: String?,
    avatarOrigin: Offset,
    motion: Animatable<Float, AnimationVector1D>,
    onEvent: (HomeViewEvent) -> Unit
) {
    val visible = state.isOpen
    val closingAfterLogout = state.logoutPhase == LogoutPhase.CLOSING
    val inMotion by remember(motion) { derivedStateOf { motion.value > 0f } }
    if (!visible && !inMotion && !closingAfterLogout) return

    val dispatch by rememberUpdatedState(onEvent)
    val dismiss by rememberUpdatedState {
        if (!state.isLoggingOut) onEvent(HomeViewEvent.DismissAccountMenu)
    }
    var screenOrigin by remember { mutableStateOf(Offset.Unspecified) }
    var screenWidth by remember { mutableIntStateOf(0) }
    var dialogOrigin by remember { mutableStateOf(Offset.Unspecified) }
    var headerCenter by remember { mutableStateOf(Offset.Unspecified) }
    var avatarTarget by remember { mutableStateOf(Offset.Unspecified) }
    val hasPositions = screenOrigin.isSpecified && dialogOrigin.isSpecified &&
        headerCenter.isSpecified && avatarTarget.isSpecified
    val progress = motion.asState()
    val scrim = MaterialTheme.colorScheme.scrim
    val screenInsets = WindowInsets.safeDrawing
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val windowMargin = with(density) { AccountMenuMargin.roundToPx() }
    val windowWidth = screenWidth - 2 * windowMargin - screenInsets.getLeft(density, layoutDirection) -
        screenInsets.getRight(density, layoutDirection)

    Box(
        modifier = Modifier.matchParentSize().onGloballyPositioned {
            screenOrigin = it.localToScreen(Offset.Zero)
            screenWidth = it.size.width
        },
        contentAlignment = AbsoluteAlignment.TopLeft
    ) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = progress.value.phase(0f, 0.56f) * 0.42f }
                .background(scrim)
        )
        if (hasPositions) {
            FlyingAccountAvatar(progress, avatarOrigin, headerCenter, avatarTarget, screenOrigin, insideDialog = false)
        }
    }

    Dialog(
        onDismissRequest = dismiss,
        properties = DialogProperties(
            dismissOnBackPress = !state.isLoggingOut,
            dismissOnClickOutside = !state.isLoggingOut
        )
    ) {
        val displayedEmail = remember { accountEmail }
        val dialogWindow = (LocalView.current.parent as DialogWindowProvider).window
        val windowGravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        val windowTopOffset = windowMargin + with(density) { AccountMenuHeaderHeight.roundToPx() }
        SideEffect {
            dialogWindow.setDimAmount(0f)
            dialogWindow.setWindowAnimations(0)
            val attributes = dialogWindow.attributes
            val targetWidth = if (windowWidth > 0) windowWidth else attributes.width
            if (attributes.gravity != windowGravity || attributes.y != windowTopOffset || attributes.width != targetWidth) {
                dialogWindow.attributes = attributes.apply {
                    gravity = windowGravity
                    y = windowTopOffset
                    width = targetWidth
                }
            }
        }

        LaunchedEffect(visible, hasPositions, closingAfterLogout) {
            if (visible && !hasPositions) return@LaunchedEffect

            motion.animateTo(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(if (visible) 760 else 310, easing = LinearEasing)
            )
            if (closingAfterLogout) dispatch(HomeViewEvent.LogoutMenuClosed)
        }

        Box(
            modifier = Modifier.onGloballyPositioned { dialogOrigin = it.localToScreen(Offset.Zero) },
            contentAlignment = AbsoluteAlignment.TopLeft
        ) {
            val interactive by remember(motion) { derivedStateOf { motion.value >= 0.99f } }
            AccountMenuCard(
                progress = progress,
                state = state,
                accountEmail = displayedEmail,
                interactive = visible && interactive,
                onEvent = onEvent,
                onHeaderPositioned = { headerCenter = it.centerOnScreen() },
                onAvatarPositioned = { avatarTarget = it.centerOnScreen() }
            )
            if (hasPositions) {
                FlyingAccountAvatar(progress, avatarOrigin, headerCenter, avatarTarget, dialogOrigin, insideDialog = true)
            }
        }
    }
}

@Composable
private fun FlyingAccountAvatar(
    progress: State<Float>,
    avatarOrigin: Offset,
    headerCenter: Offset,
    avatarTarget: Offset,
    layerOrigin: Offset,
    insideDialog: Boolean
) {
    MemberAvatar(
        contentDescription = null,
        diameter = AvatarFlightSize,
        modifier = Modifier.graphicsLayer {
            val fraction = progress.value
            val flight = fraction.phase(0f, DialogAvatarHandoff)
            val slide = fraction.phase(0.38f, 0.72f)
            val start = if (avatarOrigin.isSpecified) avatarOrigin else headerCenter
            val center = lerp(lerp(start, headerCenter, flight), avatarTarget, slide) - layerOrigin
            val diameter = lerp(
                lerp(36.dp.toPx(), AvatarFlightSize.toPx(), flight),
                AccountAvatarSize.toPx(),
                slide
            )
            alpha = if (fraction > 0f && (fraction >= DialogAvatarHandoff) == insideDialog) 1f else 0f
            translationX = center.x - size.width / 2f
            translationY = center.y - size.height / 2f
            scaleX = diameter / size.width
            scaleY = scaleX
        }
    )
}

@Composable
private fun AccountMenuCard(
    progress: State<Float>,
    state: HomeAccountMenuState,
    accountEmail: String?,
    interactive: Boolean,
    onEvent: (HomeViewEvent) -> Unit,
    onHeaderPositioned: (LayoutCoordinates) -> Unit = {},
    onAvatarPositioned: (LayoutCoordinates) -> Unit = {},
    avatar: @Composable () -> Unit = {}
) {
    var headerHeight by remember { mutableIntStateOf(0) }
    val title = stringResource(R.string.account_menu_title)
    val canAct = interactive && !state.isLoggingOut
    val cornerRadius = 24.dp
    val colors = MaterialTheme.colorScheme
    val dialogColor = lerpColor(colors.surfaceBright, colors.primaryContainer, 0.20f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = progress.value.phase(DialogAvatarHandoff, 0.61f) }
            .drawWithCache {
                val outline = Path()
                val radius = CornerRadius(cornerRadius.toPx())
                val closeRowHeight = AccountMenuHeaderHeight.roundToPx().toFloat()
                onDrawWithContent {
                    val expansion = progress.value.phase(0.66f, 1f)
                    if (expansion == 1f) {
                        drawContent()
                    } else {
                        val headerTop = closeRowHeight.coerceAtMost(size.height)
                        val headerBottom = (closeRowHeight + headerHeight).coerceAtMost(size.height)
                        val top = lerp(headerTop, 0f, expansion)
                        val bottom = lerp(headerBottom, size.height, expansion)
                        if (bottom <= top) return@onDrawWithContent
                        outline.reset()
                        outline.addRoundRect(RoundRect(Rect(0f, top, size.width, bottom), radius))
                        clipPath(outline) { this@onDrawWithContent.drawContent() }
                    }
                }
            }
            .semantics { paneTitle = title },
        color = dialogColor,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AccountMenuHeaderHeight)
                    .revealAccountActions(progress, interactive)
                    .padding(start = AccountMenuInset, end = AccountMenuInset, top = AccountMenuInset),
                contentAlignment = Alignment.CenterStart
            ) {
                IconButton(
                    enabled = canAct,
                    onClick = { onEvent(HomeViewEvent.DismissAccountMenu) },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = colors.onSurface)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.account_menu_close))
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { headerHeight = it.height }
                    .padding(
                        start = AccountMenuInset,
                        end = AccountMenuInset,
                        top = 8.dp,
                        bottom = 16.dp
                    ),
                color = colors.surfaceContainerHigh,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned(onHeaderPositioned)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(AccountAvatarSize).onGloballyPositioned(onAvatarPositioned)) { avatar() }
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                            .drawWithContent {
                                val reveal = progress.value.phase(0.46f, 0.73f)
                                val width = size.width * reveal
                                if (layoutDirection == LayoutDirection.Ltr) {
                                    clipRect(right = width) { this@drawWithContent.drawContent() }
                                } else {
                                    clipRect(left = size.width - width) { this@drawWithContent.drawContent() }
                                }
                            }
                    ) {
                        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = accountEmail ?: stringResource(R.string.account_menu_signed_in),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .revealAccountActions(progress, interactive)
                    .verticalScroll(rememberScrollState(), enabled = interactive)
                    .padding(bottom = AccountMenuInset)
            ) {
                GroupedList(
                    count = 3,
                    modifier = Modifier.padding(horizontal = AccountMenuInset),
                    containerColor = colors.surfaceContainerHigh
                ) { index ->
                    when (index) {
                        0 -> AccountMenuItem(
                            title = stringResource(R.string.account_menu_manage),
                            enabled = canAct,
                            leadingContent = {
                                Icon(Icons.Rounded.ManageAccounts, null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = { onEvent(HomeViewEvent.ManageAccountClicked) }
                        )
                        1 -> AccountMenuItem(
                            title = stringResource(R.string.app_settings_title),
                            enabled = canAct,
                            leadingContent = {
                                Icon(Icons.Rounded.Settings, null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = { onEvent(HomeViewEvent.AppSettingsClicked) }
                        )
                        else -> LogoutMenuItem(state, interactive, canAct, onEvent)
                    }
                }
            }
        }
    }
}

private fun Modifier.revealAccountActions(progress: State<Float>, interactive: Boolean): Modifier =
    this
        .then(if (interactive) Modifier else Modifier.clearAndSetSemantics { hideFromAccessibility() })
        .graphicsLayer {
            alpha = progress.value.phase(0.68f, 1f)
            translationY = 8.dp.toPx() * (1f - alpha)
        }

@Composable
private fun LogoutMenuItem(
    state: HomeAccountMenuState,
    interactive: Boolean,
    canAct: Boolean,
    onEvent: (HomeViewEvent) -> Unit
) {
    Column {
        AccountMenuItem(
            title = stringResource(
                if (state.isLoggingOut) R.string.account_menu_signing_out else R.string.account_menu_logout
            ),
            enabled = canAct,
            leadingContent = {
                AnimatedLogoutIcon(
                    isLoggingOut = state.isLoggingOut,
                    logoutSucceeded = state.logoutPhase == LogoutPhase.CLOSING ||
                        (interactive && state.logoutPhase == LogoutPhase.CONFIRMED),
                    onDepartureFinished = { onEvent(HomeViewEvent.LogoutArrowFinished) }
                )
            },
            trailingContent = {
                Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                    Crossfade(state.logoutPhase == LogoutPhase.REQUESTING, label = "logout_progress") { pending ->
                        if (pending) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            },
            onClick = { onEvent(HomeViewEvent.LogoutClicked) }
        )
        AnimatedContent(
            targetState = state.logoutError,
            label = "logout_error"
        ) { message ->
            if (message != null) {
                Text(
                    text = message.resolve(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(start = 56.dp, end = 16.dp, bottom = 8.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }
    }
}

@Composable
private fun AccountMenuItem(
    title: String,
    enabled: Boolean,
    leadingContent: @Composable () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Crossfade(title, animationSpec = tween(140), label = "account_action_label") { Text(it) }
        },
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = GroupedListDefaults.itemColors(),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    )
}

internal fun LayoutCoordinates.centerOnScreen(): Offset =
    localToScreen(Offset(size.width / 2f, size.height / 2f))

private fun Float.phase(start: Float, end: Float): Float =
    FastOutSlowInEasing.transform(((this - start) / (end - start)).coerceIn(0f, 1f))

@Preview(showBackground = true)
@Composable
private fun AccountQuickMenuPreview() {
    SloukaTheme {
        Box(Modifier.widthIn(max = 420.dp).heightIn(max = 440.dp)) {
            AccountMenuCard(
                progress = remember { mutableStateOf(1f) },
                state = HomeAccountMenuState(isOpen = true),
                accountEmail = stringResource(R.string.preview_account_email),
                interactive = true,
                onEvent = {},
                avatar = { MemberAvatar(contentDescription = null, diameter = AccountAvatarSize) }
            )
        }
    }
}
