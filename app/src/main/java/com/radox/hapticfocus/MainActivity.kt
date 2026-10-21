package com.radox.hapticfocus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.radox.hapticfocus.pomodoro.PomodoroController
import com.radox.hapticfocus.ui.HapticFocusRoot

class MainActivity : ComponentActivity() {

    private val controller: PomodoroController
        get() = (application as HapticFocusApp).controller

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* sin acción */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 13+ pide permiso para mostrar notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            HapticFocusRoot(controller)
        }
    }

    override fun onStart() {
        super.onStart()
        controller.setForeground(true)
    }

    override fun onStop() {
        controller.setForeground(false)
        super.onStop()
    }
}
