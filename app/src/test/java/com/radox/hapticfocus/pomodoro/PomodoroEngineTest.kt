package com.radox.hapticfocus.pomodoro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de la lógica del temporizador (no necesitan reloj ni emulador). */
class PomodoroEngineTest {

    private class Recorder : EngineListener {
        val alarms = mutableListOf<AlarmKind>()
        var alarmStops = 0
        var lastScheduledEnd: Long? = null
        var cancelCount = 0

        override fun onStateChanged(state: PomodoroState) {}
        override fun onAlarmStarted(kind: AlarmKind) { alarms.add(kind) }
        override fun onAlarmStopped() { alarmStops++ }
        override fun onScheduleEnd(endTimeMs: Long) { lastScheduledEnd = endTimeMs }
        override fun onCancelScheduledEnd() { cancelCount++ }
    }

    private var now = 1_000_000L
    private val recorder = Recorder()

    // Trabajo 1 min, descanso 1 min, largo 2 min, descanso largo cada 2 ciclos
    private fun engine(auto: Boolean = true) = PomodoroEngine(
        Settings(workMin = 1, breakMin = 1, longBreakMin = 2, cyclesBeforeLong = 2, autoStart = auto),
        clock = { now },
        listener = recorder,
    )

    @Test
    fun startsIdleInWorkPhase() {
        val e = engine()
        assertEquals(Phase.WORK, e.state.phase)
        assertEquals(Status.IDLE, e.state.status)
        assertEquals(60_000L, e.state.remainingMs)
        assertEquals(1, e.state.cycle)
    }

    @Test
    fun alarmRingsWhenWorkEnds() {
        val e = engine()
        e.start()
        now += 59_000
        e.tick()
        assertEquals(Status.RUNNING, e.state.status)
        assertEquals(1_000L, e.state.remainingMs)

        now += 1_000
        e.tick()
        assertEquals(Status.ALARM, e.state.status)
        assertEquals(listOf(AlarmKind.WORK), recorder.alarms)
    }

    @Test
    fun autoModeStartsBreakByItselfAfterDismiss() {
        val e = engine(auto = true)
        e.start()
        now += 60_000
        e.tick()
        e.dismissAlarm()

        assertEquals(Phase.BREAK, e.state.phase)
        assertEquals(Status.RUNNING, e.state.status)
        assertEquals(now + 60_000L, recorder.lastScheduledEnd)
        assertEquals(1, recorder.alarmStops)
    }

    @Test
    fun manualModeWaitsForPlayAfterDismiss() {
        val e = engine(auto = false)
        e.start()
        now += 60_000
        e.tick()
        e.dismissAlarm()

        assertEquals(Phase.BREAK, e.state.phase)
        assertEquals(Status.IDLE, e.state.status)
        assertEquals(60_000L, e.state.remainingMs)
    }

    @Test
    fun fullSetEndsWithLongBreakAndStartsOver() {
        val e = engine()
        e.start()

        // Trabajo 1 -> descanso
        now += 60_000; e.tick(); e.dismissAlarm()
        assertEquals(Phase.BREAK, e.state.phase)
        assertEquals(1, e.state.cycle)

        // Descanso -> trabajo 2
        now += 60_000; e.tick(); e.dismissAlarm()
        assertEquals(Phase.WORK, e.state.phase)
        assertEquals(2, e.state.cycle)

        // Trabajo 2 -> descanso LARGO (patrón especial)
        now += 60_000; e.tick()
        assertEquals(AlarmKind.WORK_LONG, recorder.alarms.last())
        e.dismissAlarm()
        assertEquals(Phase.LONG_BREAK, e.state.phase)
        assertEquals(120_000L, e.state.totalMs)

        // Descanso largo -> vuelve a trabajo, ciclo 1
        now += 120_000; e.tick(); e.dismissAlarm()
        assertEquals(Phase.WORK, e.state.phase)
        assertEquals(1, e.state.cycle)
    }

    @Test
    fun pauseKeepsRemainingTimeAndResumeContinues() {
        val e = engine()
        e.start()
        now += 20_000
        e.pause()
        assertEquals(Status.PAUSED, e.state.status)
        assertEquals(40_000L, e.state.remainingMs)

        now += 500_000 // pasa mucho tiempo en pausa
        e.tick()
        assertEquals(40_000L, e.state.remainingMs)

        e.start()
        assertEquals(now + 40_000L, recorder.lastScheduledEnd)
    }

    @Test
    fun resetGoesBackToFullPhaseTime() {
        val e = engine()
        e.start()
        now += 30_000
        e.tick()
        e.reset()
        assertEquals(Status.IDLE, e.state.status)
        assertEquals(60_000L, e.state.remainingMs)
    }

    @Test
    fun toleranceLetsSystemAlarmFinishPhaseSlightlyEarly() {
        val e = engine()
        e.start()
        now += 59_500
        e.tick(toleranceMs = 2_000)
        assertEquals(Status.ALARM, e.state.status)
    }

    @Test
    fun restoreExpiredRunningSessionRingsImmediately() {
        val e = engine()
        e.restore(Session(Phase.WORK, 1, Status.RUNNING, endTimeMs = now - 5_000, remainingMs = 0))
        assertEquals(Status.ALARM, e.state.status)
        assertEquals(listOf(AlarmKind.WORK), recorder.alarms)
    }

    @Test
    fun restoreRunningSessionContinuesCountdown() {
        val e = engine()
        e.restore(Session(Phase.BREAK, 1, Status.RUNNING, endTimeMs = now + 30_000, remainingMs = 0))
        assertEquals(Phase.BREAK, e.state.phase)
        assertEquals(Status.RUNNING, e.state.status)
        assertEquals(30_000L, e.state.remainingMs)
        assertEquals(now + 30_000L, recorder.lastScheduledEnd)
    }

    @Test
    fun applySettingsResetsEverything() {
        val e = engine()
        e.start()
        now += 10_000
        e.applySettings(Settings(workMin = 50, breakMin = 10))
        assertEquals(Phase.WORK, e.state.phase)
        assertEquals(Status.IDLE, e.state.status)
        assertEquals(3_000_000L, e.state.remainingMs)
    }

    @Test
    fun settingsAreClampedToLimits() {
        val s = Settings(workMin = 500, breakMin = 0, longBreakMin = 999, cyclesBeforeLong = 1).sanitized()
        assertEquals(90, s.workMin)
        assertEquals(1, s.breakMin)
        assertEquals(60, s.longBreakMin)
        assertEquals(2, s.cyclesBeforeLong)
    }

    @Test
    fun formatsTime() {
        assertEquals("25:00", formatTime(25 * 60_000L))
        assertEquals("00:01", formatTime(1))
        assertEquals("01:30", formatTime(90_000))
        assertEquals("00:00", formatTime(-5))
        assertTrue(formatTime(0) == "00:00")
        assertFalse(formatTime(60_000) == "00:59")
    }
}
