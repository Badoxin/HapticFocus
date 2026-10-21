package com.radox.hapticfocus.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlin.math.sqrt

/**
 * Detecta una sacudida con el acelerómetro: dos movimientos fuertes seguidos
 * en menos de [WINDOW_MS]. Solo escucha entre [start] y [stop] (mientras suena la alarma),
 * así no gasta batería ni se activa sin querer durante el día.
 */
class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit,
) : SensorEventListener {

    private val sensorManager: SensorManager? = context.getSystemService(SensorManager::class.java)
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val hits = ArrayDeque<Long>()

    fun start() {
        val sm = sensorManager ?: return
        val sensor = accelerometer ?: return
        sm.unregisterListener(this)
        hits.clear()
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        hits.clear()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // En reposo la magnitud es ~1 g
        val g = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
        if (g < THRESHOLD_G) return

        val now = SystemClock.elapsedRealtime()

        // Una sola sacudida genera varias lecturas seguidas: contar una por golpe
        val last = hits.lastOrNull()
        if (last != null && now - last < MIN_GAP_MS) return

        while (hits.isNotEmpty() && now - hits.first() > WINDOW_MS) hits.removeFirst()
        hits.addLast(now)

        if (hits.size >= REQUIRED_HITS) {
            hits.clear()
            onShake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        // Si se activa sin querer, sube THRESHOLD_G; si cuesta activarla, bájalo.
        private const val THRESHOLD_G = 1.6f
        private const val REQUIRED_HITS = 2
        private const val WINDOW_MS = 1_000L
        private const val MIN_GAP_MS = 150L
    }
}
