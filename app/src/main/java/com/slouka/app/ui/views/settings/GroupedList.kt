package com.slouka.app.ui.views.settings

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal object GroupedListDefaults {
    val itemGap = 2.dp
    private val cornerAnimation = spring<Dp>(stiffness = Spring.StiffnessMediumLow)

    fun itemShape(index: Int, count: Int): RoundedCornerShape =
        itemShape(cornerRadius(index == 0), cornerRadius(index == count - 1))

    @Composable
    fun animatedItemShape(index: Int, count: Int): RoundedCornerShape {
        val top by animateDpAsState(cornerRadius(index == 0), cornerAnimation, label = "list_top_corner")
        val bottom by animateDpAsState(cornerRadius(index == count - 1), cornerAnimation, label = "list_bottom_corner")
        return itemShape(top, bottom)
    }

    private fun cornerRadius(atEdge: Boolean) = if (atEdge) 20.dp else 4.dp

    private fun itemShape(top: Dp, bottom: Dp) =
        RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)

    @Composable
    fun itemColors() = ListItemDefaults.colors(containerColor = Color.Transparent)

    @Composable
    fun containerColor() = MaterialTheme.colorScheme.surfaceContainerHigh
}

@Composable
internal fun GroupedList(
    count: Int,
    modifier: Modifier = Modifier,
    title: String? = null,
    containerColor: Color = GroupedListDefaults.containerColor(),
    content: @Composable (index: Int) -> Unit
) {
    Column(modifier) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp).semantics { heading() }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(GroupedListDefaults.itemGap)) {
            repeat(count) { index ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = containerColor,
                    shape = GroupedListDefaults.itemShape(index, count)
                ) {
                    content(index)
                }
            }
        }
    }
}
