package com.radox.hapticfocus.pomodoro

/** Configuración del usuario. Se comparte entre modo automático y manual. */
data class Settings(
    val workMin: Int = 25,
    val breakMin: Int = 5,
    val longBreakMin: Int = 15,
    val cyclesBeforeLong: Int = 4,
    val autoStart: Boolean = true,
) {
    /** Devuelve una copia con todos los valores dentro de los límites permitidos. */
    fun sanitized(): Settings = copy(
        workMin = workMin.coerceIn(LIMIT_WORK),
        breakMin = breakMin.coerceIn(LIMIT_BREAK),
        longBreakMin = longBreakMin.coerceIn(LIMIT_LONG_BREAK),
        cyclesBeforeLong = cyclesBeforeLong.coerceIn(LIMIT_CYCLES),
    )

    companion object {
        val LIMIT_WORK = 1..90
        val LIMIT_BREAK = 1..30
        val LIMIT_LONG_BREAK = 1..60
        val LIMIT_CYCLES = 2..8
    }
}
