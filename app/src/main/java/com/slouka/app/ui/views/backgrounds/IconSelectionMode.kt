package com.slouka.app.ui.views.backgrounds

enum class IconSelectionMode {
    PATTERN,
    RANDOM
}

internal fun iconPatternPeriod(iconCount: Int): Int = iconCount * 2

internal fun iconPatternIndex(column: Int, row: Int, count: Int, mode: IconSelectionMode, seed: Int): Int {
    val period = iconPatternPeriod(count)
    val x = Math.floorMod(column, period)
    val y = Math.floorMod(row, period)

    return when (mode) {
        IconSelectionMode.PATTERN -> (x + y) % count
        IconSelectionMode.RANDOM -> {
            var hash = seed xor (x * 73856093) xor (y * 19349663)
            hash = (hash xor (hash ushr 16)) * -2048144789
            hash = (hash xor (hash ushr 13)) * -1028477387
            Math.floorMod(hash xor (hash ushr 16), count)
        }
    }
}
