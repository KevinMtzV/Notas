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

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class) // 👈 3. Añade el OptIn
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // 👈 4. Añade esto (para UI de borde a borde)

        setContent {
            AppTheme {

                // --- 5. Calcula el tamaño de la pantalla ---
                val windowSizeClass = calculateWindowSizeClass(this)
                val widthSizeClass = windowSizeClass.widthSizeClass

                // --- 6. Pasa el tamaño a tu AppNav ---
                AppNav(widthSizeClass = widthSizeClass)
            }
        }
    }
}