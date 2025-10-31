package com.example.notasytareas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge // 👈 1. Importa esto
import com.example.notasytareas.ui.theme.AppTheme
import com.example.notasytareas.navigation.AppNav

// 👈 2. Importa las clases para WindowSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {

                // Calcula el tamaño de la pantalla
                val windowSizeClass = calculateWindowSizeClass(this)
                val widthSizeClass = windowSizeClass.widthSizeClass

                // Pasa el tamaño al AppNav
                AppNav(widthSizeClass = widthSizeClass)
            }
        }
    }
}