package com.radox.hapticfocus.haptics

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.radox.hapticfocus.pomodoro.AlarmKind

/**
 * Patrones de vibración. Cada par es (vibra, pausa) en milisegundos.
 * La última pausa es larga para que, al repetirse, haya un respiro.
 */
internal object HapticPatterns {
    // Terminó un trabajo -> toca descansar: tres vibraciones largas
    private val WORK = listOf(500L to 250L, 500L to 250L, 500L to 1500L)

    // Terminó un trabajo y se completó el set -> descanso LARGO: cinco vibraciones largas
    private val WORK_LONG = listOf(
        600L to 200L, 600L to 200L, 600L to 200L, 600L to 200L, 600L to 1500L,
    )

    // Terminó un descanso -> toca trabajar: cuatro vibraciones cortas
    private val BREAK = listOf(180L to 120L, 180L to 120L, 180L to 120L, 180L to 1500L)

    fun pairsFor(kind: AlarmKind): List<Pair<Long, Long>> = when (kind) {
        AlarmKind.WORK -> WORK
        AlarmKind.WORK_LONG -> WORK_LONG
        AlarmKind.BREAK -> BREAK
    }

    /**
     * Repite el patrón hasta cubrir [maxMs]. Android espera [espera, vibra, espera, vibra, ...],
     * por eso el arreglo empieza con 0 y luego va en pares (vibra, pausa).
     */
    fun buildTimings(pairs: List<Pair<Long, Long>>, maxMs: Long): LongArray {
        val out = ArrayList<Long>()
        out.add(0L)
        var total = 0L
        while (total < maxMs) {
            for ((on, off) in pairs) {
                out.add(on)
                out.add(off)
                total += on + off
            }
        }
        return out.toLongArray()
    }
}

class HapticManager(context: Context) {

    private val vibrator: Vibrator? = findVibrator(context)

    // USAGE_ALARM permite vibrar aunque la app esté en segundo plano
    private val alarmAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    /** Vibra con el patrón de [kind] durante un máximo de [ALARM_MAX_MS] o hasta [stopAlarm]. */
    @Suppress("DEPRECATION")
    fun startAlarm(kind: AlarmKind) {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val timings = HapticPatterns.buildTimings(HapticPatterns.pairsFor(kind), ALARM_MAX_MS)
        v.vibrate(VibrationEffect.createWaveform(timings, -1), alarmAttributes)
    }

    fun stopAlarm() {
        vibrator?.cancel()
    }

    /** Pequeño toque al presionar botones. */
    fun tap() {
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    }

    @Suppress("DEPRECATION")
    private fun findVibrator(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    companion object {
        /** La alarma deja de vibrar sola tras este tiempo (la fase sigue esperando que la detengas). */
        const val ALARM_MAX_MS = 60_000L
    }
}
