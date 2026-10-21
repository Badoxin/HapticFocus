package com.radox.hapticfocus.pomodoro

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.radox.hapticfocus.alarm.AlarmNotifier
import com.radox.hapticfocus.alarm.EndAlarmScheduler
import com.radox.hapticfocus.data.Store
import com.radox.hapticfocus.haptics.HapticManager
import com.radox.hapticfocus.sensors.ShakeDetector

/**
 * Une el motor del Pomodoro con todo lo de Android: vibración, sacudida, alarma del sistema,
 * notificación y guardado. Vive mientras viva el proceso de la app, así que no se pierde
 * el estado si la pantalla gira o la Activity se vuelve a crear.
 */
class PomodoroController(app: Application) : EngineListener {

    private val store = Store(app)
    private val haptics = HapticManager(app)
    private val scheduler = EndAlarmScheduler(app)
    private val notifier = AlarmNotifier(app)
    private val handler = Handler(Looper.getMainLooper())
    private val shake = ShakeDetector(app) { dismissAlarm() }

    private val engine = PomodoroEngine(
        settings = store.loadSettings(),
        clock = { System.currentTimeMillis() },
        listener = this,
    )

    /** Estado observable: Compose se redibuja solo cuando cambia. */
    var state by mutableStateOf(engine.state)
        private set

    private var foreground = false
    private var ticking = false

    private val ticker = object : Runnable {
        override fun run() {
            engine.tick()
            if (engine.state.status == Status.RUNNING) {
                handler.postDelayed(this, TICK_MS)
            } else {
                ticking = false
            }
        }
    }

    init {
        // Si había una sesión en curso (o terminó mientras la app estaba cerrada), retomarla
        engine.restore(store.loadSession())
    }

    // --- Acciones que llama la interfaz --------------------------------------------------

    fun toggle() {
        if (state.status != Status.ALARM) haptics.tap()
        engine.toggle()
    }

    fun reset() {
        haptics.tap()
        engine.reset()
    }

    fun dismissAlarm() {
        engine.dismissAlarm()
    }

    fun applySettings(settings: Settings) {
        val clean = settings.sanitized()
        store.saveSettings(clean)
        engine.applySettings(clean)
    }

    fun tap() {
        haptics.tap()
    }

    /** Lo llama la alarma del sistema cuando llega la hora de fin. */
    fun checkEnd() {
        engine.tick(toleranceMs = 2_000L)
    }

    /** La Activity avisa si está en pantalla; si lo está, no hace falta la notificación. */
    fun setForeground(value: Boolean) {
        foreground = value
        if (value) notifier.cancel()
    }

    // --- EngineListener ---------------------------------------------------------------

    override fun onStateChanged(state: PomodoroState) {
        val previous = this.state
        this.state = state

        if (state.status == Status.RUNNING) startTicker()

        // Guardar la sesión solo cuando cambia algo importante (no en cada tick)
        if (previous.phase != state.phase ||
            previous.cycle != state.cycle ||
            previous.status != state.status
        ) {
            store.saveSession(engine.snapshot())
        }
    }

    override fun onAlarmStarted(kind: AlarmKind) {
        haptics.startAlarm(kind)
        shake.start()
        if (!foreground) notifier.show(kind)
    }

    override fun onAlarmStopped() {
        haptics.stopAlarm()
        shake.stop()
        notifier.cancel()
    }

    override fun onScheduleEnd(endTimeMs: Long) {
        scheduler.schedule(endTimeMs)
    }

    override fun onCancelScheduledEnd() {
        scheduler.cancel()
    }

    // --- Interno ----------------------------------------------------------------------

    private fun startTicker() {
        if (ticking) return
        ticking = true
        handler.postDelayed(ticker, TICK_MS)
    }

    private companion object {
        const val TICK_MS = 250L
    }
}
