package com.slouka.app.ui.views.backgrounds

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.Dp
import kotlin.math.ceil
import kotlin.math.floor

@Composable
internal fun IconColumnPattern(
    icons: List<ImageVector>,
    tint: Color,
    iconSize: Dp,
    spacing: Dp,
    selectionMode: IconSelectionMode,
    randomSeed: Int,
    appearance: State<Float>,
    offset: DrawScope.() -> Offset
) {
    require(icons.isNotEmpty())
    val painters = icons.map { rememberVectorPainter(it) }
    val colorFilter = ColorFilter.tint(tint)

    Canvas(modifier = Modifier.clipToBounds()) {
        val step = spacing.toPx()
        val fullSize = iconSize.toPx()
        val fraction = appearance.value
        if (fraction <= 0f) return@Canvas
        val iconPx = fullSize * fraction
        val inset = (fullSize - iconPx) / 2f
        val shift = offset()
        val firstColumn = floor((-shift.x - fullSize) / step).toInt()
        val lastColumn = ceil((size.width - shift.x) / step).toInt()

        for (column in firstColumn..lastColumn) {
            val stagger = if (column % 2 == 0) 0f else step / 2f
            val shiftY = stagger + shift.y
            val firstRow = floor((-shiftY - fullSize) / step).toInt()
            val lastRow = ceil((size.height - shiftY) / step).toInt()

            for (row in firstRow..lastRow) {
                translate(left = column * step + shift.x + inset, top = row * step + shiftY + inset) {
                    val index = iconPatternIndex(column, row, icons.size, selectionMode, randomSeed)
                    with(painters[index]) {
                        draw(size = Size(iconPx, iconPx), alpha = fraction, colorFilter = colorFilter)
                    }
                }
            }
        }
    }
}
