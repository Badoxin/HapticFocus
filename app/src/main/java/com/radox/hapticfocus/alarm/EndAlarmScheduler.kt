package com.radox.hapticfocus.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Le pide al sistema que despierte a la app a la hora exacta en que termina la fase,
 * aunque la pantalla esté apagada o Android haya cerrado el proceso.
 */
class EndAlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager? = context.getSystemService(AlarmManager::class.java)

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(AlarmReceiver.ACTION_PHASE_END)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    fun schedule(endTimeMs: Long) {
        val am = alarmManager ?: return
        val pi = pendingIntent()
        val canBeExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        try {
            if (canBeExact) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeMs, pi)
            } else {
                // Sin permiso de alarmas exactas: puede retrasarse un poco, pero suena
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeMs, pi)
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTimeMs, pi)
        }
    }

    fun cancel() {
        alarmManager?.cancel(pendingIntent())
    }

    private companion object {
        const val REQUEST_CODE = 100
    }
}
