package com.radox.hapticfocus.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

/** Anillo de progreso. [progress] va de 0 a 1 y el contenido se dibuja en el centro. */
@Composable
fun ProgressRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.085f
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            // Pista de fondo
            drawArc(
                color = Palette.Track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )

            // Progreso (empieza arriba y avanza en sentido horario)
            val sweep = 360f * progress.coerceIn(0f, 1f)
            if (sweep > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        content()
    }
}

/** Botón alargado con texto (Play, Pausa, Detener, Guardar...). */
@Composable
fun PillButton(
    text: String,
    color: Color,
    height: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(percent = 32))
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                color = Palette.TextPrimary,
                fontSize = (height * 0.42f).asSp(),
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
        )
    }
}

/** Botón redondo con un símbolo (↺, −, +). */
@Composable
fun CircleButton(
    text: String,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Palette.Chip)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                color = Palette.TextPrimary,
                fontSize = (size * 0.5f).asSp(),
            ),
            maxLines = 1,
        )
    }
}

/** Interruptor encendido / apagado. */
@Composable
fun ToggleSwitch(
    checked: Boolean,
    height: Dp,
    onToggle: () -> Unit,
) {
    Box(
        Modifier
            .width(height * 1.8f)
            .height(height)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (checked) Palette.Save else Palette.Cancel)
            .clickable(onClick = onToggle)
            .padding(height * 0.1f),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .size(height * 0.8f)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
