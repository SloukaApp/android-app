package com.slouka.app.ui.views.backgrounds

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TiltIconMotionTest {
    @Test
    fun quickOppositeImpulseCanReverseHorizontalMotionOnTheNextFrame() {
        val motion = TiltIconMotion()
        val first = motion.advance(Offset(1f, 0f), 1f / 60f, 0.1f, 2.8f)
        val second = motion.advance(Offset(-1f, 0f), 1f / 60f, 0.1f, 2.8f)

        assertTrue(first.x > 0.0006f)
        assertTrue(second.x < first.x)
    }

    @Test
    fun neutralTiltMovesOnlyUpAndIgnoresSmallNoise() {
        val motion = TiltIconMotion()
        var position = Offset.Zero

        repeat(120) {
            position = motion.advance(Offset(0.02f, -0.03f), 1f / 60f, 0.1f, 2f)
        }

        assertEquals(0f, position.x, 0.0001f)
        assertEquals(1.8f, position.y, 0.0001f)
    }

    @Test
    fun holdingTiltKeepsMovingInsteadOfHoldingAnOffset() {
        val motion = TiltIconMotion()
        var position = Offset.Zero

        repeat(120) { position = motion.advance(Offset(1f, 0f), 1f / 60f, 0.1f, 2f) }
        val first = position
        repeat(120) { position = motion.advance(Offset(1f, 0f), 1f / 60f, 0.1f, 2f) }

        assertTrue(position.x - first.x > 0.3f)
    }

    @Test
    fun downwardTiltCanReverseTheDefaultUpwardMotion() {
        val motion = TiltIconMotion()
        var position = Offset.Zero

        repeat(120) { position = motion.advance(Offset(0f, 1f), 1f / 60f, 0.1f, 2f) }

        assertEquals(0f, position.x, 0.0001f)
        assertTrue(position.y > 0.1f && position.y < 0.2f)
    }

    @Test
    fun motionHasSimilarSpeedAtDifferentFrameRates() {
        fun simulate(frameRate: Int): Offset {
            val motion = TiltIconMotion()
            var position = Offset.Zero

            repeat(frameRate * 4) {
                position = motion.advance(Offset(0.5f, 0.4f), 1f / frameRate, 0.1f, 2f)
            }

            return position
        }

        val slow = simulate(30)
        val fast = simulate(120)
        assertEquals(slow.x, fast.x, 0.005f)
        assertEquals(slow.y, fast.y, 0.005f)
    }
}
