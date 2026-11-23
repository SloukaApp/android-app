package com.slouka.app.ui.views.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.slouka.app.R
import com.slouka.app.ui.views.decos.AnimatedFlame
import com.slouka.app.ui.views.decos.FlameMode

@Composable
internal fun FlameSettings(
    flameMode: FlameMode,
    animateFlameChanges: Boolean,
    onFlameModeChange: (FlameMode) -> Unit,
    onFlameTransitionsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedFlame(
            mode = flameMode,
            modifier = Modifier.size(64.dp),
            animateStateChanges = animateFlameChanges
        )
        FlameModeSelector(
            flameMode = flameMode,
            onFlameModeChange = onFlameModeChange,
            modifier = Modifier.padding(horizontal = 16.dp),
            enabled = enabled
        )
        val transitionsLabel = stringResource(R.string.app_settings_flame_transitions)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(transitionsLabel)
            Switch(
                checked = animateFlameChanges,
                onCheckedChange = onFlameTransitionsChange,
                enabled = enabled,
                modifier = Modifier.semantics { contentDescription = transitionsLabel }
            )
        }
    }
}

@Composable
internal fun FlameModeSelector(
    flameMode: FlameMode,
    onFlameModeChange: (FlameMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        FlameMode.entries.forEachIndexed { index, mode ->
            SegmentedButton(
                selected = mode == flameMode,
                onClick = { onFlameModeChange(mode) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index, FlameMode.entries.size),
                label = {
                    Text(
                        when (mode) {
                            FlameMode.REST -> stringResource(R.string.app_settings_flame_off)
                            FlameMode.BURNING_STATIC -> stringResource(R.string.app_settings_flame_static)
                            FlameMode.BURNING -> stringResource(R.string.app_settings_flame_dynamic)
                        }
                    )
                }
            )
        }
    }
}
