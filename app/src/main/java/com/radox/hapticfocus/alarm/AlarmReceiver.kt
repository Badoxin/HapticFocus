package com.radox.hapticfocus.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.radox.hapticfocus.HapticFocusApp

/**
 * Recibe dos avisos:
 *  - ACTION_PHASE_END: el sistema avisa que llegó la hora de fin de la fase.
 *  - ACTION_STOP_ALARM: el usuario tocó "Detener" en la notificación.
 *
 * Si Android había cerrado la app, al llegar el aviso se vuelve a crear el proceso
 * (HapticFocusApp) y el controlador retoma la sesión guardada.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as HapticFocusApp
        when (intent.action) {
            ACTION_PHASE_END -> app.controller.checkEnd()
            ACTION_STOP_ALARM -> app.controller.dismissAlarm()
        }
    }

    companion object {
        const val ACTION_PHASE_END = "com.radox.hapticfocus.action.PHASE_END"
        const val ACTION_STOP_ALARM = "com.radox.hapticfocus.action.STOP_ALARM"
    }
}
