package com.slouka.app.ui.screens.home.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slouka.app.ui.theme.SloukaTheme
import com.slouka.app.ui.views.decos.AnimatedFlame
import com.slouka.app.ui.views.decos.FlameMode

@Composable
internal fun HomePageView(
    flameMode: FlameMode,
    animateFlameChanges: Boolean,
    onFlameModeChange: (FlameMode) -> Unit,
    onFlameTransitionsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        AnimatedFlame(
            mode = flameMode,
            modifier = Modifier.size(64.dp),
            animateStateChanges = animateFlameChanges
        )
        SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 16.dp)) {
            FlameMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == flameMode,
                    onClick = { onFlameModeChange(mode) },
                    enabled = enabled,
                    shape = SegmentedButtonDefaults.itemShape(index, FlameMode.entries.size),
                    label = {
                        Text(
                            when (mode) {
                                FlameMode.REST -> "Off"
                                FlameMode.BURNING_STATIC -> "Static"
                                FlameMode.BURNING -> "Dynamic"
                            }
                        )
                    }
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("One-shot transitions")
            Switch(
                checked = animateFlameChanges,
                onCheckedChange = onFlameTransitionsChange,
                enabled = enabled,
                modifier = Modifier.semantics { contentDescription = "One-shot transitions" }
            )
        }
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
