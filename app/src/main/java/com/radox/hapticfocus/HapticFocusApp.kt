package com.radox.hapticfocus

import android.app.Application
import com.radox.hapticfocus.pomodoro.PomodoroController

class HapticFocusApp : Application() {

    /** Un solo controlador para todo el proceso (lo usan la Activity y el receptor de alarmas). */
    lateinit var controller: PomodoroController
        private set

    override fun onCreate() {
        super.onCreate()
        controller = PomodoroController(this)
    }
}
