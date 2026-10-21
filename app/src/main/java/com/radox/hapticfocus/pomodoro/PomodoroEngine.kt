package com.radox.hapticfocus.pomodoro

enum class Phase { WORK, BREAK, LONG_BREAK }

enum class Status { IDLE, RUNNING, PAUSED, ALARM }

/** Qué patrón de vibración / aviso corresponde cuando TERMINA una fase. */
enum class AlarmKind { WORK, WORK_LONG, BREAK }

data class PomodoroState(
    val phase: Phase,
    /** Número de trabajo dentro del set actual (1..cyclesBeforeLong). */
    val cycle: Int,
    val status: Status,
    val remainingMs: Long,
    val totalMs: Long,
    val settings: Settings,
)

/** Lo mínimo que hay que guardar para retomar el temporizador si Android cierra la app. */
data class Session(
    val phase: Phase,
    val cycle: Int,
    val status: Status,
    val endTimeMs: Long,
    val remainingMs: Long,
)

/** El motor no sabe nada de Android: avisa de lo que pasa mediante este listener. */
interface EngineListener {
    fun onStateChanged(state: PomodoroState)
    fun onAlarmStarted(kind: AlarmKind)
    fun onAlarmStopped()
    fun onScheduleEnd(endTimeMs: Long)
    fun onCancelScheduledEnd()
}

/**
 * Máquina de estados del Pomodoro (Kotlin puro, fácil de probar).
 *
 * El tiempo se calcula con una HORA DE FIN, no restando segundos, así no se
 * desfasa aunque la app se suspenda o el reloj apague la pantalla.
 */
class PomodoroEngine(
    settings: Settings,
    private val clock: () -> Long,
    private val listener: EngineListener,
) {
    var state: PomodoroState = initialState(settings.sanitized())
        private set

    private var endTimeMs: Long = 0L

    /** Un solo botón: Play / Pausa / Detener según el estado. */
    fun toggle() {
        when (state.status) {
            Status.RUNNING -> pause()
            Status.IDLE, Status.PAUSED -> start()
            Status.ALARM -> dismissAlarm()
        }
    }

    fun start() {
        if (state.status != Status.IDLE && state.status != Status.PAUSED) return
        endTimeMs = clock() + state.remainingMs
        update(state.copy(status = Status.RUNNING))
        listener.onScheduleEnd(endTimeMs)
    }

    fun pause() {
        if (state.status != Status.RUNNING) return
        val left = (endTimeMs - clock()).coerceAtLeast(0L)
        listener.onCancelScheduledEnd()
        update(state.copy(status = Status.PAUSED, remainingMs = left))
    }

    /** Reinicia la fase actual. */
    fun reset() {
        val wasAlarm = state.status == Status.ALARM
        listener.onCancelScheduledEnd()
        update(state.copy(status = Status.IDLE, remainingMs = state.totalMs))
        if (wasAlarm) listener.onAlarmStopped()
    }

    /** Aplica ajustes nuevos y vuelve a empezar en trabajo (ciclo 1). */
    fun applySettings(newSettings: Settings) {
        val wasAlarm = state.status == Status.ALARM
        listener.onCancelScheduledEnd()
        update(initialState(newSettings.sanitized()))
        if (wasAlarm) listener.onAlarmStopped()
    }

    /**
     * Revisa si la fase ya terminó. [toleranceMs] permite dar por terminada una fase
     * que está a punto de terminar (útil cuando llega la alarma del sistema).
     */
    fun tick(toleranceMs: Long = 0L) {
        if (state.status != Status.RUNNING) return
        val left = endTimeMs - clock()
        if (left <= toleranceMs) {
            enterAlarm()
        } else {
            update(state.copy(remainingMs = left))
        }
    }

    /** Detiene la alarma y pasa a la siguiente fase (arranca sola si está en modo automático). */
    fun dismissAlarm() {
        if (state.status != Status.ALARM) return
        listener.onAlarmStopped()

        val s = state.settings
        val (nextPhase, nextCycle) = when (state.phase) {
            Phase.WORK ->
                (if (state.cycle >= s.cyclesBeforeLong) Phase.LONG_BREAK else Phase.BREAK) to state.cycle
            Phase.BREAK -> Phase.WORK to (state.cycle + 1)
            Phase.LONG_BREAK -> Phase.WORK to 1
        }

        val total = durationMs(nextPhase, s)
        update(
            state.copy(
                phase = nextPhase,
                cycle = nextCycle,
                status = Status.IDLE,
                remainingMs = total,
                totalMs = total,
            ),
        )
        if (s.autoStart) start()
    }

    /** Retoma una sesión guardada (por ejemplo, después de que Android cerró la app). */
    fun restore(session: Session?) {
        if (session == null) return
        val s = state.settings
        val cycle = session.cycle.coerceIn(1, s.cyclesBeforeLong)
        val total = durationMs(session.phase, s)
        val base = PomodoroState(session.phase, cycle, Status.IDLE, total, total, s)

        when (session.status) {
            Status.RUNNING -> {
                val left = session.endTimeMs - clock()
                if (left > 0L) {
                    endTimeMs = session.endTimeMs
                    update(base.copy(status = Status.RUNNING, remainingMs = left.coerceAtMost(total)))
                    listener.onScheduleEnd(endTimeMs)
                } else {
                    // Se acabó mientras la app estaba cerrada: sonar ya
                    endTimeMs = session.endTimeMs
                    update(base.copy(status = Status.ALARM, remainingMs = 0L))
                    listener.onAlarmStarted(kindFor(session.phase, cycle, s))
                }
            }
            Status.PAUSED -> {
                val remaining = if (session.remainingMs in 1..total) session.remainingMs else total
                update(base.copy(status = Status.PAUSED, remainingMs = remaining))
            }
            Status.ALARM -> {
                endTimeMs = session.endTimeMs
                update(base.copy(status = Status.ALARM, remainingMs = 0L))
                listener.onAlarmStarted(kindFor(session.phase, cycle, s))
            }
            Status.IDLE -> update(base)
        }
    }

    fun snapshot(): Session =
        Session(state.phase, state.cycle, state.status, endTimeMs, state.remainingMs)

    private fun enterAlarm() {
        update(state.copy(status = Status.ALARM, remainingMs = 0L))
        listener.onAlarmStarted(kindFor(state.phase, state.cycle, state.settings))
    }

    private fun update(newState: PomodoroState) {
        state = newState
        listener.onStateChanged(newState)
    }

    companion object {
        fun durationMs(phase: Phase, s: Settings): Long {
            val minutes = when (phase) {
                Phase.WORK -> s.workMin
                Phase.BREAK -> s.breakMin
                Phase.LONG_BREAK -> s.longBreakMin
            }
            return minutes * 60_000L
        }

        fun kindFor(phase: Phase, cycle: Int, s: Settings): AlarmKind = when {
            phase != Phase.WORK -> AlarmKind.BREAK
            cycle >= s.cyclesBeforeLong -> AlarmKind.WORK_LONG
            else -> AlarmKind.WORK
        }

        private fun initialState(s: Settings): PomodoroState {
            val total = durationMs(Phase.WORK, s)
            return PomodoroState(Phase.WORK, 1, Status.IDLE, total, total, s)
        }
    }
}

/** 90_000 ms -> "01:30" */
fun formatTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) + 999L) / 1000L
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}
