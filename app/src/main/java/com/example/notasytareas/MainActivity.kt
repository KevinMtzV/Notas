package com.example.notasytareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.notasytareas.ui.theme.AppTheme
import com.example.notasytareas.navigation.AppNav

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. CAPTURAR DATOS DE LA NOTIFICACIÓN
        // Si la app se abre normal, noteId será -1.
        // Si viene de una notificación, tendrá el ID real.
        val noteIdFromNotification = intent.getIntExtra("note_id", -1)
        val isTaskFromNotification = intent.getBooleanExtra("is_task", false)

        setContent {
            AppTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val widthSizeClass = windowSizeClass.widthSizeClass

                // 2. PASAR LOS DATOS AL APPNAV
                AppNav(
                    widthSizeClass = widthSizeClass,
                    startNoteId = noteIdFromNotification,
                    startIsTask = isTaskFromNotification
                )
            }
        }
    }
}