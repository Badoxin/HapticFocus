package com.radox.hapticfocus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.radox.hapticfocus.pomodoro.Phase
import com.radox.hapticfocus.pomodoro.PomodoroState
import com.radox.hapticfocus.pomodoro.Status
import com.radox.hapticfocus.pomodoro.formatTime

@Composable
fun TimerScreen(
    state: PomodoroState,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val isAlarm = state.status == Status.ALARM
    val isRunning = state.status == Status.RUNNING

    // Los ajustes solo se abren cuando el temporizador no está corriendo ni sonando
    val canOpenSettings = !isRunning && !isAlarm

    val accent = when {
        isAlarm -> Palette.Alarm
        state.phase == Phase.WORK -> Palette.Work
        state.phase == Phase.BREAK -> Palette.Break
        else -> Palette.LongBreak
    }

    val progress = when {
        isAlarm -> 1f
        state.totalMs > 0 -> state.remainingMs.toFloat() / state.totalMs.toFloat()
        else -> 0f
    }

    val cyclesTotal = state.settings.cyclesBeforeLong

    val label = when {
        isAlarm && state.phase == Phase.WORK ->
            if (state.cycle >= cyclesTotal) "¡Descanso largo!" else "¡A descansar!"
        isAlarm -> "¡A trabajar!"
        state.phase == Phase.WORK -> "Trabajo"
        state.phase == Phase.BREAK -> "Descanso"
        else -> "Descanso largo"
    }

    val sub = if (isAlarm) "sacude para parar" else "Ciclo ${state.cycle}/$cyclesTotal"
    val buttonText = when {
        isAlarm -> "Detener"
        isRunning -> "Pausa"
        else -> "Play"
    }

    WatchSurface {
        val h = maxHeight
        val w = maxWidth

        val headerH = h * 0.14f
        val controlsH = h * 0.21f
        val gap = h * 0.03f
        val ringSize = minOf(w, h - headerH - controlsH - gap * 2)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Encabezado: nombre de la fase + engrane de ajustes
            Row(
                modifier = Modifier.fillMaxWidth().height(headerH),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        color = accent,
                        fontSize = maxOf(headerH * 0.62f, 13.dp).asSp(),
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (canOpenSettings) {
                    Box(
                        modifier = Modifier.size(headerH).clickable(onClick = onOpenSettings),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "⚙",
                            style = TextStyle(
                                color = Palette.TextSecondary,
                                fontSize = (headerH * 0.75f).asSp(),
                            ),
                        )
                    }
                } else {
                    Spacer(Modifier.size(headerH))
                }
            }

            // Anillo con el tiempo restante
            ProgressRing(
                progress = progress,
                color = accent,
                modifier = Modifier.size(ringSize),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicText(
                        text = formatTime(state.remainingMs),
                        style = TextStyle(
                            color = Palette.TextPrimary,
                            fontSize = (ringSize * 0.25f).asSp(),
                            fontWeight = FontWeight.Light,
                            fontFeatureSettings = "tnum",
                            textAlign = TextAlign.Center,
                        ),
                        maxLines = 1,
                    )
                    BasicText(
                        text = sub,
                        style = TextStyle(
                            color = if (isAlarm) Palette.Alarm else Palette.TextMuted,
                            fontSize = maxOf(ringSize * 0.085f, 10.dp).asSp(),
                            textAlign = TextAlign.Center,
                        ),
                        maxLines = 1,
                    )
                }
            }

            // Botones: Play / Pausa / Detener y reiniciar
            Row(
                modifier = Modifier.fillMaxWidth().height(controlsH),
                horizontalArrangement = Arrangement.spacedBy(gap * 2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillButton(
                    text = buttonText,
                    color = if (isAlarm) Palette.AlarmButton else accent,
                    height = controlsH,
                    modifier = Modifier.weight(1f),
                    onClick = onToggle,
                )
                if (!isAlarm) {
                    CircleButton(text = "↺", size = controlsH, onClick = onReset)
                }
            }
        }
    }
}
