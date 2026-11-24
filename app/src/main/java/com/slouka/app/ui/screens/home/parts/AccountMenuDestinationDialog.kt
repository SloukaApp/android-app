package com.slouka.app.ui.screens.home.parts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.slouka.app.ui.navigation.AccountRoute
import com.slouka.app.ui.navigation.AppSettingsRoute
import com.slouka.app.ui.navigation.SessionsRoute
import com.slouka.app.ui.screens.account.AccountScreenRoot
import com.slouka.app.ui.screens.home.model.AccountMenuDestination
import com.slouka.app.ui.screens.sessions.SessionsScreenRoot
import com.slouka.app.ui.screens.settings.AppSettingsScreenRoot
import com.slouka.app.ui.views.settings.GroupedListDefaults

@Composable
internal fun AccountMenuDestinationDialog(
    destination: AccountMenuDestination?,
    bounds: Rect?,
    onDismiss: () -> Unit
) {
    var displayedDestination by remember { mutableStateOf<AccountMenuDestination?>(null) }
    var displayedBounds by remember { mutableStateOf(Rect.Zero) }
    LaunchedEffect(destination, bounds) {
        if (destination != null && bounds != null && !bounds.isEmpty) {
            displayedDestination = destination
            displayedBounds = bounds
        }
    }
    val shownDestination = displayedDestination ?: return

    val navigation = rememberNavController()
    val goBack: () -> Unit = {
        if (destination != null) {
            if (navigation.previousBackStackEntry != null) navigation.popBackStack() else onDismiss()
        }
    }
    Dialog(
        onDismissRequest = goBack,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val window = (LocalView.current.parent as DialogWindowProvider).window
        SideEffect {
            window.setDimAmount(0f)
            window.setWindowAnimations(0)
        }
        val progress = remember { Animatable(0f) }
        var windowOrigin by remember { mutableStateOf(Offset.Unspecified) }
        LaunchedEffect(destination, windowOrigin.isSpecified) {
            if (windowOrigin.isSpecified) {
                progress.animateTo(if (destination == null) 0f else 1f, tween(500))
            }
            if (destination == null) {
                navigation.popBackStack(navigation.graph.id, inclusive = true)
                displayedDestination = null
            }
        }

        val colors = MaterialTheme.colorScheme
        val startingShape = GroupedListDefaults.itemShape(
            index = shownDestination.ordinal,
            count = AccountMenuDestination.entries.size + 1
        )
        Box(
            Modifier
                .fillMaxSize()
                .onGloballyPositioned { windowOrigin = it.localToScreen(Offset.Zero) }
                .drawWithCache {
                    val outline = Path()
                    val topRadius = startingShape.topStart.toPx(displayedBounds.size, this)
                    val bottomRadius = startingShape.bottomStart.toPx(displayedBounds.size, this)
                    onDrawWithContent {
                        if (!windowOrigin.isSpecified || progress.value == 0f) return@onDrawWithContent
                        val fraction = progress.value
                        if (fraction == 1f) {
                            drawContent()
                        } else {
                            val anchor = displayedBounds.translate(-windowOrigin)
                            val frame = Rect(
                                left = lerp(anchor.left, 0f, fraction),
                                top = lerp(anchor.top, 0f, fraction),
                                right = lerp(anchor.right, size.width, fraction),
                                bottom = lerp(anchor.bottom, size.height, fraction)
                            )
                            val top = CornerRadius(topRadius * (1f - fraction))
                            val bottom = CornerRadius(bottomRadius * (1f - fraction))
                            outline.reset()
                            outline.addRoundRect(RoundRect(frame, top, top, bottom, bottom))
                            clipPath(outline) {
                                drawRect(lerpColor(colors.surfaceContainerHigh, colors.background, fraction))
                                this@onDrawWithContent.drawContent()
                            }
                        }
                    }
                }
        ) {
            val startDestination = when (shownDestination) {
                AccountMenuDestination.ACCOUNT -> AccountRoute
                AccountMenuDestination.APP_SETTINGS -> AppSettingsRoute
            }
            NavHost(
                navController = navigation,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    val fraction = progress.value
                    alpha = fraction
                    if (windowOrigin.isSpecified) {
                        translationX = (displayedBounds.left - windowOrigin.x) * (1f - fraction)
                        translationY = (displayedBounds.top - windowOrigin.y) * (1f - fraction)
                    }
                }
            ) {
                composable<AccountRoute> {
                    AccountScreenRoot(
                        onBack = goBack,
                        onSessionsClick = { navigation.navigate(SessionsRoute) }
                    )
                }
                composable<SessionsRoute> {
                    SessionsScreenRoot(onBack = goBack)
                }
                composable<AppSettingsRoute> {
                    AppSettingsScreenRoot(onBack = goBack)
                }
            }
        }
    }
}
