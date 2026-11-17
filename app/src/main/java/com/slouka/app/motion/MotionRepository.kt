package com.slouka.app.motion

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.view.Surface
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.exp

@Singleton
class MotionRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val sensor = this.manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val isAvailable: Boolean
        get() = this.sensor != null

    fun observe(rotation: Int) = callbackFlow {
        val manager = this@MotionRepository.manager
        val sensor = this@MotionRepository.sensor

        if (manager == null || sensor == null) {
            trySend(MotionState())
            close()
            return@callbackFlow
        }

        var timestamp = 0L
        var originX = 0f
        var originY = 0f
        var filteredX = 0f
        var filteredY = 0f

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val (x, y) = when (rotation) {
                    Surface.ROTATION_90 -> -event.values[1] to event.values[0]
                    Surface.ROTATION_180 -> -event.values[0] to -event.values[1]
                    Surface.ROTATION_270 -> event.values[1] to -event.values[0]
                    else -> event.values[0] to event.values[1]
                }

                if (timestamp == 0L) {
                    originX = x
                    originY = y
                    filteredX = x
                    filteredY = y
                    trySend(MotionState(isAvailable = true))
                } else {
                    val seconds = (event.timestamp - timestamp) / 1_000_000_000f
                    val weight = 1f - exp(-seconds / 0.06f)
                    filteredX += (x - filteredX) * weight
                    filteredY += (y - filteredY) * weight
                    // Retain part of a quick impulse instead of filtering shakes away.
                    val responseX = filteredX + (x - filteredX) * 0.65f - originX
                    val responseY = filteredY + (y - filteredY) * 0.65f - originY
                    val range = SensorManager.GRAVITY_EARTH * 0.5f

                    trySend(
                        MotionState(
                            isAvailable = true,
                            x = (-responseX / range).coerceIn(-1f, 1f),
                            y = (responseY / range).coerceIn(-1f, 1f)
                        )
                    )
                }

                timestamp = event.timestamp
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val registered = manager.registerListener(listener, sensor, 33_333, Handler(Looper.getMainLooper()))
        if (!registered) {
            trySend(MotionState())
            close()
        }

        awaitClose { manager.unregisterListener(listener) }
    }.buffer(Channel.CONFLATED)
}
