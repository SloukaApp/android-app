package com.slouka.app.ui.views.decos

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.slouka.app.ui.theme.SloukaTheme
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AnimatedFlame(
    burning: Boolean,
    modifier: Modifier = Modifier,
    animateStateChanges: Boolean = true
) {
    AnimatedFlame(
        mode = if (burning) FlameMode.BURNING else FlameMode.REST,
        modifier = modifier,
        animateStateChanges = animateStateChanges
    )
}

private const val OneShotIgnitionEnd = 0.08f
private const val OneShotGlowEnd = 0.84f

@Composable
fun AnimatedFlame(
    mode: FlameMode,
    modifier: Modifier = Modifier,
    animateStateChanges: Boolean = true
) {
    val transition = remember { FlameTransitionState(mode) }
    val phase = if (transition.needsDynamicPhase) {
        rememberInfiniteTransition(label = "Flame").animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(FlameCycleMillis, easing = LinearEasing)),
            label = "Flame phase"
        )
    } else {
        null
    }
    LaunchedEffect(mode, animateStateChanges) {
        transition.moveTo(mode, animateStateChanges, phase = phase?.value)
    }

    val outlineColor = MaterialTheme.colorScheme.onSurfaceVariant
    val idleOutline = remember {
        Path().apply {
            setFlameContour(sway = 0f, leftRise = 0f, rightRise = 0f, innerLift = 0f)
        }
    }
    val idleStroke = remember {
        Stroke(
            width = 50f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(54f, 92f))
        )
    }
    val outline = remember { Path() }
    val center = remember { Path() }
    val glowCenter = Offset(480f, 817f)
    val glowRadius = 350f
    val glowBrush = remember {
        Brush.radialGradient(
            0f to Color(0xFFFFBB55),
            0.5f to Color(0x80FF9A24),
            1f to Color(0x00FF9A24),
            center = glowCenter,
            radius = glowRadius
        )
    }

    Canvas(modifier.size(24.dp)) {
        val igniting = transition.isIgniting
        val progress = transition.progress
        val cooling = transition.cooling
        val time = if (igniting) progress * (2f * PI.toFloat()) else phase?.value ?: 0f
        val strength = when {
            igniting -> oneShotIntensity(progress)
            phase != null -> 1f - cooling
            else -> 0f
        }
        val fillAlpha = transition.fillAlpha
        val centerAlpha = transition.centerAlpha
        val outlineAlpha = transition.outlineAlpha
        val previousPhase = transition.sourcePhase ?: 0f
        val carryover = if (igniting && transition.sourcePhase != null) {
            1f - FastOutSlowInEasing.transform((progress / 0.18f).coerceAtMost(1f))
        } else {
            0f
        }
        val newMotion = strength * (1f - carryover)
        val innerLift = innerFlameLift(time) * newMotion + innerFlameLift(previousPhase) * carryover
        val flash = if (igniting) oneShotFlash(progress) else 0f
        val glowFade = ((progress - OneShotIgnitionEnd) / (OneShotGlowEnd - OneShotIgnitionEnd))
            .coerceIn(0f, 1f)
        val glowStrength = if (progress < OneShotIgnitionEnd) flash
        else 1f - glowFade * glowFade * (3f - 2f * glowFade)
        val glowAlpha = if (igniting) 0.36f * glowStrength else 0f
        if (fillAlpha > 0f) {
            outline.setFlameContour(
                sway = (flameSway(time) + ignitionBend(if (igniting) progress else 0f)) * newMotion +
                    flameSway(previousPhase) * carryover,
                leftRise = leftTongueRise(time) * newMotion + leftTongueRise(previousPhase) * carryover,
                rightRise = rightTongueRise(time) * newMotion + rightTongueRise(previousPhase) * carryover,
                innerLift = innerLift
            )
        }
        if (centerAlpha > 0f) center.setFlameCenter(innerLift)

        val iconSize = size.minDimension
        val flicker = ((sin(time * 2f) + 1f) * newMotion + (sin(previousPhase * 2f) + 1f) * carryover) * 0.5f
        val warmTip = lerp(Color(0xFFFFB347), Color(0xFFFFD36D), flicker)
        val flameBrush = Brush.verticalGradient(
            colors = listOf(
                lerp(lerp(warmTip, Color(0xFFE57335), cooling), Color(0xFFFFF4BD), flash),
                lerp(lerp(Color(0xFFFF891F), Color(0xFFDC5920), cooling), Color(0xFFFFBE49), flash),
                lerp(lerp(Color(0xFFF4511E), Color(0xFFD54016), cooling), Color(0xFFFF7A26), flash)
            ),
            startY = 140f,
            endY = 840f
        )
        val flamePivot = Offset(480f, 840f)
        val scaleX = flashScaleX(flash) - 0.15f * cooling
        val forming = if (igniting) 0.22f * (1f - fillAlpha) else 0f
        val scaleY = flashScaleY(flash) - 0.56f * cooling - forming
        withTransform({
            translate((size.width - iconSize) / 2f, (size.height - iconSize) / 2f)
            scale(iconSize / 960f, iconSize / 960f, pivot = Offset.Zero)
        }) {
            if (glowAlpha > 0f) {
                scale(scaleX = 1f + 0.35f * glowFade, scaleY = 0.32f, pivot = glowCenter) {
                    drawCircle(
                        brush = glowBrush,
                        radius = glowRadius,
                        center = glowCenter,
                        alpha = glowAlpha
                    )
                }
            }
            if (outlineAlpha > 0f) {
                drawPath(idleOutline, outlineColor, alpha = outlineAlpha, style = idleStroke)
            }
            if (fillAlpha > 0f) {
                scale(scaleX, scaleY, pivot = flamePivot) {
                    drawPath(outline, flameBrush, alpha = fillAlpha)
                }
            }
            if (centerAlpha > 0f) {
                scale(scaleX, scaleY.coerceAtLeast(0.49f), pivot = flamePivot) {
                    drawPath(center, flameBrush, alpha = centerAlpha)
                }
            }
            if (strength > 0f) {
                val cycle = time / (2f * PI.toFloat())
                for (tongue in 0..1) {
                    // Release the sparks on the rising side of each tongue's motion.
                    val emission = if (tongue == 0) 0.17f else 0.67f
                    val emissionTime = if (igniting) emission else 0f
                    if (cycle < emissionTime) continue
                    val elapsed = (cycle - emission + 1f) % 1f
                    val timeSinceExtinguishing = progress * FlameExtinguishMillis / FlameCycleMillis
                    if (transition.isExtinguishing && elapsed < timeSinceExtinguishing) continue
                    val sparkProgress = elapsed / 0.43f
                    if (sparkProgress >= 1f) continue

                    val launchPhase = emission * 2f * PI.toFloat()
                    val tipX = if (tongue == 0) 433f else 651f
                    val tipY = if (tongue == 0) -807f else -669f
                    val launchStrength = if (igniting) oneShotIntensity(emission) else 1f
                    val launchFlash = if (igniting) oneShotFlash(emission) else 0f
                    val launchSway = (flameSway(launchPhase) + ignitionBend(emissionTime)) * launchStrength
                    val tipAtLaunchX = flameX(tipX, tipY, launchSway)
                    val originX = 480f + (tipAtLaunchX - 480f) * flashScaleX(launchFlash)
                    val launchGap = if (tongue == 0) 28f else 0f
                    val tipAtLaunchY = flameY(
                        tipX,
                        tipY,
                        leftTongueRise(launchPhase) * launchStrength,
                        rightTongueRise(launchPhase) * launchStrength
                    )
                    val originY = 840f + (tipAtLaunchY - 840f) * flashScaleY(launchFlash) - launchGap
                    val fade = (1f - sparkProgress) * (1f - 0.35f * sparkProgress) * strength
                    // Both sparks travel the same distance from their own launch point.
                    val rise = 250f * sparkProgress * (2f - sparkProgress)

                    drawCircle(
                        color = Color(0xFFFFD078),
                        radius = 28f,
                        center = Offset(originX, originY - rise),
                        alpha = fade
                    )
                }
            }
        }
    }
}

private fun oneShotIntensity(progress: Float): Float {
    val ignition = FastOutSlowInEasing.transform((progress / OneShotIgnitionEnd).coerceAtMost(1f))
    val cooldown = ((1f - progress) / 0.46f).coerceAtMost(1f)
    return ignition * cooldown
}

private fun oneShotFlash(progress: Float): Float {
    val peak = OneShotIgnitionEnd
    return if (progress < peak) {
        FastOutSlowInEasing.transform(progress / peak)
    } else {
        1f - FastOutSlowInEasing.transform(((progress - peak) / 0.52f).coerceAtMost(1f))
    }
}

private fun flashScaleX(flash: Float) = 1f + 0.10f * flash

private fun flashScaleY(flash: Float) = 1f + 0.15f * flash

private fun ignitionBend(progress: Float): Float {
    val bendProgress = (progress / 0.32f).coerceAtMost(1f)
    return 24f * sin(bendProgress * PI.toFloat())
}

private fun flameSway(phase: Float) = sin(phase) * 21f + sin(phase * 2f) * 7f

private fun leftTongueRise(phase: Float): Float {
    val lift = sin(phase).coerceAtLeast(0f)
    return 96f * lift * lift
}

private fun rightTongueRise(phase: Float): Float {
    val followLift = sin(phase - 0.24f).coerceAtLeast(0f)
    val smallLift = (-sin(phase)).coerceAtLeast(0f)
    return 42f * followLift * followLift + 35f * smallLift * smallLift
}

private fun flameX(x: Float, y: Float, sway: Float) =
    x + sway * ((-y - 400f) / 400f).coerceIn(0f, 1f)

private fun flameY(x: Float, y: Float, leftRise: Float, rightRise: Float): Float {
    // The original two tips flex independently, with the notch as their shared base.
    val leftWeight = ((550f - x) / 120f).coerceIn(0f, 1f)
    val rightWeight = ((x - 550f) / 100f).coerceIn(0f, 1f)
    val leftHeight = ((-y - 520f) / 280f).coerceIn(0f, 1f)
    val rightHeight = ((-y - 500f) / 170f).coerceIn(0f, 1f)
    return y + 960f - leftRise * leftWeight * leftHeight - rightRise * rightWeight * rightHeight
}

// Material Symbols Rounded, Mode Heat (Fill), Apache-2.0.
private fun Path.setFlameContour(
    sway: Float,
    leftRise: Float,
    rightRise: Float,
    innerLift: Float
) {
    fun curve(cx: Float, cy: Float, ex: Float, ey: Float) = quadraticTo(
        flameX(cx, cy, sway), flameY(cx, cy, leftRise, rightRise),
        flameX(ex, ey, sway), flameY(ex, ey, leftRise, rightRise)
    )

    rewind()
    moveTo(160f, 560f)
    curve(160f, -513f, 227f, -617f)
    curve(294f, -721f, 411f, -799f)
    curve(433f, -814f, 456.5f, -800.5f)
    curve(480f, -787f, 480f, -760f)
    lineTo(flameX(480f, -708f, sway), flameY(480f, -708f, leftRise, rightRise))
    curve(480f, -674f, 503.5f, -651f)
    curve(527f, -628f, 561f, -628f)
    curve(578f, -628f, 593.5f, -635.5f)
    curve(609f, -643f, 621f, -657f)
    curve(629f, -667f, 641.5f, -669.5f)
    curve(654f, -672f, 665f, -664f)
    curve(728f, -619f, 764f, -549f)
    curve(800f, -479f, 800f, -400f)
    curve(800f, -312f, 757f, -239.5f)
    // The cutout and the center flame share the same translation, preserving their gap.
    fun liftedY(y: Float) = y - innerLift
    fun cutoutCurve(cx: Float, cy: Float, ex: Float, ey: Float) =
        quadraticTo(cx, liftedY(cy), ex, liftedY(ey))

    quadraticTo(714f, 793f, 644f, liftedY(835f))
    cutoutCurve(661f, 811f, 670.5f, 782.5f)
    cutoutCurve(680f, 754f, 680f, 722f)
    cutoutCurve(680f, 682f, 665f, 646.5f)
    cutoutCurve(650f, 611f, 622f, 583f)
    lineTo(480f, liftedY(444f))
    lineTo(339f, liftedY(583f))
    cutoutCurve(310f, 612f, 295f, 647f)
    cutoutCurve(280f, 682f, 280f, 722f)
    cutoutCurve(280f, 754f, 289.5f, 782.5f)
    cutoutCurve(299f, 811f, 316f, 835f)
    quadraticTo(246f, 793f, 203f, 720.5f)
    curve(160f, -312f, 160f, -400f)
    close()
}

private fun innerFlameLift(phase: Float) = (0.5f + 0.5f * sin(2f * phase - 0.4f)) * 38f

private fun Path.setFlameCenter(innerLift: Float) {
    fun innerX(x: Float) = 480f + (x - 480f) * 0.83f
    fun innerY(y: Float) = 840f + (y - 840f) * 0.86f - innerLift
    fun innerCurve(cx: Float, cy: Float, ex: Float, ey: Float) =
        quadraticTo(innerX(cx), innerY(cy), innerX(ex), innerY(ey))

    rewind()
    moveTo(480f, innerY(556f))
    lineTo(innerX(565f), innerY(639f))
    innerCurve(582f, 656f, 591f, 677f)
    innerCurve(600f, 698f, 600f, 722f)
    innerCurve(600f, 771f, 565f, 805.5f)
    innerCurve(530f, 840f, 480f, 840f)
    innerCurve(430f, 840f, 395f, 805.5f)
    innerCurve(360f, 771f, 360f, 722f)
    innerCurve(360f, 699f, 369f, 677.5f)
    innerCurve(378f, 656f, 395f, 639f)
    close()
}

@Preview(name = "Flame at rest", showBackground = true)
@Composable
private fun FlameAtRestPreview() {
    SloukaTheme { AnimatedFlame(burning = false, modifier = Modifier.size(64.dp)) }
}

@Preview(name = "Burning flame", showBackground = true)
@Composable
private fun BurningFlamePreview() {
    SloukaTheme { AnimatedFlame(burning = true, modifier = Modifier.size(64.dp)) }
}

@Preview(name = "Steady flame", showBackground = true)
@Composable
private fun SteadyFlamePreview() {
    SloukaTheme { AnimatedFlame(mode = FlameMode.BURNING_STATIC, modifier = Modifier.size(64.dp)) }
}
