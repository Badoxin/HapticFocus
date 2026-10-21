package com.radox.hapticfocus.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import com.radox.hapticfocus.MainActivity
import com.radox.hapticfocus.R
import com.radox.hapticfocus.pomodoro.AlarmKind

/**
 * Muestra el aviso visual cuando una fase termina y la app no está en pantalla.
 * La vibración la maneja HapticManager, por eso el canal de la notificación es silencioso.
 */
class AlarmNotifier(private val context: Context) {

    private val manager: NotificationManager? = context.getSystemService(NotificationManager::class.java)

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Fin de fase",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Avisa cuando termina el trabajo o el descanso"
            enableVibration(false)
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager?.createNotificationChannel(channel)
    }

    fun show(kind: AlarmKind) {
        val nm = manager ?: return

        val (title, text) = when (kind) {
            AlarmKind.WORK -> "¡A descansar!" to "Terminó tu sesión de trabajo."
            AlarmKind.WORK_LONG -> "¡Descanso largo!" to "Completaste todos los ciclos. ¡Buen trabajo!"
            AlarmKind.BREAK -> "¡A trabajar!" to "Terminó el descanso."
        }

        val openApp = PendingIntent.getActivity(
            context,
            REQUEST_OPEN,
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val stop = PendingIntent.getBroadcast(
            context,
            REQUEST_STOP,
            Intent(context, AlarmReceiver::class.java).setAction(AlarmReceiver.ACTION_STOP_ALARM),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val stopAction = Notification.Action.Builder(
            Icon.createWithResource(context, R.drawable.ic_notification),
            "Detener",
            stop,
        ).build()

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(Notification.CATEGORY_ALARM)
            .setContentIntent(openApp)
            .addAction(stopAction)
            .setDeleteIntent(stop) // si la descartas, también se detiene la vibración
            .setAutoCancel(true)
            .build()

        try {
            nm.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Sin permiso de notificaciones: la vibración igual funciona
        }
    }

    fun cancel() {
        manager?.cancel(NOTIFICATION_ID)
    }

    private companion object {
        const val CHANNEL_ID = "hapticfocus_alarm"
        const val NOTIFICATION_ID = 1
        const val REQUEST_OPEN = 10
        const val REQUEST_STOP = 11
    }
}
