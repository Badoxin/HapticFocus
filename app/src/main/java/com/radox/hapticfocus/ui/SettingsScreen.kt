package com.radox.hapticfocus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.radox.hapticfocus.pomodoro.Settings

@Composable
fun SettingsScreen(
    initial: Settings,
    onSave: (Settings) -> Unit,
    onCancel: () -> Unit,
    onTap: () -> Unit,
) {
    var draft by remember { mutableStateOf(initial) }

    WatchSurface {
        val h = maxHeight
        val controlH = h * 0.21f
        val gap = h * 0.05f

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            // Primera fila: así se ve sin tener que desplazarse
            item {
                SwitchRow(
                    label = "Inicio automático",
                    caption = if (draft.autoStart) {
                        "la siguiente fase arranca sola"
                    } else {
                        "esperas tu Play en cada fase"
                    },
                    checked = draft.autoStart,
                    controlHeight = controlH,
                    onToggle = {
                        onTap()
                        draft = draft.copy(autoStart = !draft.autoStart)
                    },
                )
            }
            item {
                StepperRow(
                    label = "Trabajo",
                    value = draft.workMin,
                    range = Settings.LIMIT_WORK,
                    unit = " min",
                    controlHeight = controlH,
                    onTap = onTap,
                    onChange = { draft = draft.copy(workMin = it) },
                )
            }
            item {
                StepperRow(
                    label = "Descanso",
                    value = draft.breakMin,
                    range = Settings.LIMIT_BREAK,
                    unit = " min",
                    controlHeight = controlH,
                    onTap = onTap,
                    onChange = { draft = draft.copy(breakMin = it) },
                )
            }
            item {
                StepperRow(
                    label = "Descanso largo",
                    value = draft.longBreakMin,
                    range = Settings.LIMIT_LONG_BREAK,
                    unit = " min",
                    controlHeight = controlH,
                    onTap = onTap,
                    onChange = { draft = draft.copy(longBreakMin = it) },
                )
            }
            item {
                StepperRow(
                    label = "Ciclos hasta el descanso largo",
                    value = draft.cyclesBeforeLong,
                    range = Settings.LIMIT_CYCLES,
                    unit = "",
                    controlHeight = controlH,
                    onTap = onTap,
                    onChange = { draft = draft.copy(cyclesBeforeLong = it) },
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(controlH),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    PillButton(
                        text = "Cancelar",
                        color = Palette.Cancel,
                        height = controlH,
                        modifier = Modifier.weight(1f),
                        onClick = onCancel,
                    )
                    PillButton(
                        text = "Guardar",
                        color = Palette.Save,
                        height = controlH,
                        modifier = Modifier.weight(1f),
                        onClick = { onSave(draft) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    range: IntRange,
    unit: String,
    controlHeight: Dp,
    onTap: () -> Unit,
    onChange: (Int) -> Unit,
) {
    fun change(delta: Int) {
        val next = (value + delta).coerceIn(range)
        if (next != value) {
            onTap()
            onChange(next)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = Palette.TextSecondary,
                fontSize = maxOf(controlHeight * 0.36f, 11.dp).asSp(),
                textAlign = TextAlign.Center,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleButton(text = "−", size = controlHeight) { change(-1) }
            BasicText(
                text = "$value$unit",
                style = TextStyle(
                    color = Palette.TextPrimary,
                    fontSize = (controlHeight * 0.5f).asSp(),
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
            )
            CircleButton(text = "+", size = controlHeight) { change(1) }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    caption: String,
    checked: Boolean,
    controlHeight: Dp,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(controlHeight * 0.3f),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = label,
                style = TextStyle(
                    color = Palette.TextPrimary,
                    fontSize = maxOf(controlHeight * 0.42f, 12.dp).asSp(),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                text = caption,
                style = TextStyle(
                    color = Palette.TextMuted,
                    fontSize = maxOf(controlHeight * 0.3f, 10.dp).asSp(),
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ToggleSwitch(checked = checked, height = controlHeight * 0.75f, onToggle = onToggle)
    }
}
