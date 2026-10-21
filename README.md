# HapticFocus (Wear OS · Kotlin · Jetpack Compose)

Temporizador Pomodoro para reloj con vibración háptica, sacudida para detener la alarma,
descanso largo, modo automático/manual y aviso con la pantalla apagada.

## Cómo abrirlo

1. Descomprime la carpeta `HapticFocus`.
2. Android Studio → **File → Open** → elige la carpeta `HapticFocus` (la que tiene `settings.gradle.kts`).
3. Espera a que termine **Gradle Sync** (la primera vez descarga bastante).
   - Si pide instalar un SDK de Android faltante (API 35), acéptalo.
   - Si ofrece actualizar Gradle / el plugin de Android ("Upgrade Assistant"), puedes aceptar.
4. Arriba elige tu emulador **Wear OS Square** y presiona **▶ Run**.

## Qué probar

- Ajustes (⚙): pon Trabajo 1 min, Descanso 1 min, Desc. largo 1 min, Ciclos 2 → Guardar.
- Play. Al terminar: vibración en bucle (hasta 60 s) + notificación si la app no está en pantalla.
- Detener con el botón, con "Detener" de la notificación o **sacudiendo**.
- Modo AUTO: la siguiente fase arranca sola al detener. Modo manual: espera tu Play.
- Cierra la app a la mitad: al volver (o al sonar la alarma) retoma el temporizador.

## Pruebas de lógica (sin emulador)

`app/src/test/.../PomodoroEngineTest.kt` — clic derecho → Run. Cubre fases, ciclos,
descanso largo, auto/manual, pausa, retomar sesión y límites de ajustes.

## Estructura

- `pomodoro/PomodoroEngine.kt`  máquina de estados (Kotlin puro)
- `pomodoro/PomodoroController.kt`  une el motor con vibración, sensor, alarma y guardado
- `haptics/`  patrones de vibración · `sensors/`  sacudida · `alarm/`  alarma exacta + notificación
- `data/Store.kt`  ajustes y sesión · `ui/`  pantallas en Compose (tamaños proporcionales a la pantalla)

## Notas

- El emulador no vibra físicamente; la vibración real se nota en un reloj o con logs.
- Versiones usadas: AGP 8.10.1, Kotlin 2.1.21, Gradle 8.11.1, Compose BOM 2025.06.00.
