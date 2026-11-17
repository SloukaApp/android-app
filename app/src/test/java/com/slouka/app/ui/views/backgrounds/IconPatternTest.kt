package com.slouka.app.ui.views.backgrounds

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IconPatternTest {
    @Test
    fun patternUsesIconsInListOrder() {
        val row = (0..5).map { iconPatternIndex(it, 0, 3, IconSelectionMode.PATTERN, 0) }

        assertEquals(listOf(0, 1, 2, 0, 1, 2), row)
    }

    @Test
    fun wrappingInEitherDirectionKeepsTheSameIcon() {
        for (mode in IconSelectionMode.entries) {
            for (count in listOf(1, 2, 3, 5)) {
                val period = iconPatternPeriod(count)

                for (column in -10..10) {
                    for (row in -10..10) {
                        val expected = iconPatternIndex(column, row, count, mode, 42)
                        assertEquals(expected, iconPatternIndex(column + period, row, count, mode, 42))
                        assertEquals(expected, iconPatternIndex(column, row - period, count, mode, 42))
                        assertTrue(expected in 0 until count)
                    }
                }
            }
        }
    }

    @Test
    fun randomSelectionIsStableButChangesWithSeed() {
        fun tile(seed: Int) = (0..5).flatMap { column ->
            (0..5).map { row -> iconPatternIndex(column, row, 3, IconSelectionMode.RANDOM, seed) }
        }

        assertEquals(tile(42), tile(42))
        assertTrue(tile(42) != tile(43))
        assertEquals(setOf(0, 1, 2), tile(42).toSet())
    }
}
