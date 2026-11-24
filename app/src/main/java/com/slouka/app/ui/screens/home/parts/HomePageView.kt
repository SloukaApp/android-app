package com.slouka.app.ui.screens.home.parts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.ui.views.decos.FlameMode
import com.slouka.app.ui.views.settings.FlameSettings

@Composable
internal fun HomePageView(
    flameMode: FlameMode,
    animateFlameChanges: Boolean,
    onFlameModeChange: (FlameMode) -> Unit,
    onFlameTransitionsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        FlameSettings(
            flameMode = flameMode,
            animateFlameChanges = animateFlameChanges,
            onFlameModeChange = onFlameModeChange,
            onFlameTransitionsChange = onFlameTransitionsChange,
            enabled = enabled
        )
    }
}

@Preview(name = "Home page", showBackground = true)
@Composable
private fun HomePageViewPreview() {
    SloukaTheme {
        HomePageView(
            flameMode = FlameMode.BURNING,
            animateFlameChanges = true,
            onFlameModeChange = {},
            onFlameTransitionsChange = {}
        )
    }
}
