package com.slouka.app.ui.views.bars

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slouka.app.R
import com.slouka.app.ui.theme.SloukaTheme

@Composable
internal fun AnimatedNavigationIcon(
    @DrawableRes outlined: Int,
    @DrawableRes filled: Int,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val fill = animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(220),
        label = "navigation_icon_fill"
    )

    Box(modifier.size(24.dp)) {
        Icon(
            imageVector = ImageVector.vectorResource(outlined),
            contentDescription = null,
            modifier = Modifier.matchParentSize().drawWithContent {
                val edge = size.height * (1f - fill.value)
                clipRect(bottom = edge) { this@drawWithContent.drawContent() }
            }
        )
        Icon(
            imageVector = ImageVector.vectorResource(filled),
            contentDescription = null,
            modifier = Modifier.matchParentSize().drawWithContent {
                val edge = size.height * (1f - fill.value)
                clipRect(top = edge) { this@drawWithContent.drawContent() }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnimatedNavigationIconPreview() {
    SloukaTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AnimatedNavigationIcon(
                R.drawable.ic_nav_home_rounded_outline,
                R.drawable.ic_nav_home_rounded_filled,
                selected = false
            )
            AnimatedNavigationIcon(
                R.drawable.ic_nav_home_rounded_outline,
                R.drawable.ic_nav_home_rounded_filled,
                selected = true
            )
        }
    }
}
