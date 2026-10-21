package com.radox.hapticfocus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

object Palette {
    val Background = Color(0xFF000000)
    val Track = Color(0xFF1E1E1E)

    val Work = Color(0xFFFF7043)
    val Break = Color(0xFF26A69A)
    val LongBreak = Color(0xFF42A5F5)
    val Alarm = Color(0xFFFFB300)
    val AlarmButton = Color(0xFFC62828)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF9E9E9E)
    val TextMuted = Color(0xFF757575)

    val Chip = Color(0xFF263238)
    val Cancel = Color(0xFF37474F)
    val Save = Color(0xFF2E7D32)
}

/** Convierte un tamaño en dp a sp, para que el texto escale con la pantalla del reloj. */
@Composable
fun Dp.asSp(): TextUnit = with(LocalDensity.current) { this@asSp.toSp() }

/**
 * Fondo negro que ocupa toda la pantalla del reloj. Deja un margen mayor en pantallas
 * redondas para que nada quede cortado en las esquinas. Todos los tamaños de la interfaz
 * se calculan a partir del espacio disponible, así se adapta a cualquier reloj.
 */
@Composable
fun WatchSurface(content: @Composable BoxWithConstraintsScope.() -> Unit) {
    val isRound = LocalConfiguration.current.isScreenRound
    BoxWithConstraints(Modifier.fillMaxSize().background(Palette.Background)) {
        val side = minOf(maxWidth, maxHeight)
        val margin = side * (if (isRound) 0.14f else 0.05f)
        BoxWithConstraints(Modifier.fillMaxSize().padding(margin)) {
            content()
        }
    }
}
