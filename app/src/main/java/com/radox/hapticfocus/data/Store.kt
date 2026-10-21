package com.radox.hapticfocus.data

import android.content.Context
import android.content.SharedPreferences
import com.radox.hapticfocus.pomodoro.Phase
import com.radox.hapticfocus.pomodoro.Session
import com.radox.hapticfocus.pomodoro.Settings
import com.radox.hapticfocus.pomodoro.Status

/** Guarda los ajustes y la sesión en curso (para retomarla si Android cierra la app). */
class Store(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("hapticfocus", Context.MODE_PRIVATE)

    fun loadSettings(): Settings {
        val d = Settings()
        return Settings(
            workMin = prefs.getInt(KEY_WORK, d.workMin),
            breakMin = prefs.getInt(KEY_BREAK, d.breakMin),
            longBreakMin = prefs.getInt(KEY_LONG_BREAK, d.longBreakMin),
            cyclesBeforeLong = prefs.getInt(KEY_CYCLES, d.cyclesBeforeLong),
            autoStart = prefs.getBoolean(KEY_AUTO, d.autoStart),
        ).sanitized()
    }

    fun saveSettings(s: Settings) {
        prefs.edit()
            .putInt(KEY_WORK, s.workMin)
            .putInt(KEY_BREAK, s.breakMin)
            .putInt(KEY_LONG_BREAK, s.longBreakMin)
            .putInt(KEY_CYCLES, s.cyclesBeforeLong)
            .putBoolean(KEY_AUTO, s.autoStart)
            .apply()
    }

    fun loadSession(): Session? {
        val phase = prefs.getString(KEY_S_PHASE, null)
            ?.let { runCatching { Phase.valueOf(it) }.getOrNull() } ?: return null
        val status = prefs.getString(KEY_S_STATUS, null)
            ?.let { runCatching { Status.valueOf(it) }.getOrNull() } ?: return null
        return Session(
            phase = phase,
            cycle = prefs.getInt(KEY_S_CYCLE, 1).coerceAtLeast(1),
            status = status,
            endTimeMs = prefs.getLong(KEY_S_END, 0L),
            remainingMs = prefs.getLong(KEY_S_REMAINING, 0L),
        )
    }

    fun saveSession(s: Session) {
        prefs.edit()
            .putString(KEY_S_PHASE, s.phase.name)
            .putString(KEY_S_STATUS, s.status.name)
            .putInt(KEY_S_CYCLE, s.cycle)
            .putLong(KEY_S_END, s.endTimeMs)
            .putLong(KEY_S_REMAINING, s.remainingMs)
            .apply()
    }

    private companion object {
        const val KEY_WORK = "workMin"
        const val KEY_BREAK = "breakMin"
        const val KEY_LONG_BREAK = "longBreakMin"
        const val KEY_CYCLES = "cyclesBeforeLong"
        const val KEY_AUTO = "autoStart"
        const val KEY_S_PHASE = "session_phase"
        const val KEY_S_STATUS = "session_status"
        const val KEY_S_CYCLE = "session_cycle"
        const val KEY_S_END = "session_end"
        const val KEY_S_REMAINING = "session_remaining"
    }
}
