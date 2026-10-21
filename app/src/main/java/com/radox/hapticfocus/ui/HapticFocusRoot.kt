package com.radox.hapticfocus.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.radox.hapticfocus.pomodoro.PomodoroController

private enum class Screen { TIMER, SETTINGS }

/** Una sola Activity: cambia entre la pantalla del temporizador y la de ajustes. */
@Composable
fun HapticFocusRoot(controller: PomodoroController) {
    var screen by remember { mutableStateOf(Screen.TIMER) }
    val state = controller.state

    // Deslizar hacia atrás en ajustes regresa al temporizador en vez de cerrar la app
    BackHandler(enabled = screen == Screen.SETTINGS) { screen = Screen.TIMER }

    when (screen) {
        Screen.TIMER -> TimerScreen(
            state = state,
            onToggle = { controller.toggle() },
            onReset = { controller.reset() },
            onOpenSettings = { screen = Screen.SETTINGS },
        )

        Screen.SETTINGS -> SettingsScreen(
            initial = state.settings,
            onSave = {
                controller.applySettings(it)
                screen = Screen.TIMER
            },
            onCancel = { screen = Screen.TIMER },
            onTap = { controller.tap() },
        )
    }
}
