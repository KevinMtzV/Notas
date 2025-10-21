package com.example.notasytareas

import android.app.Application
import com.example.notasytareas.data.local.NotasDatabase
import com.example.notasytareas.data.repository.NotasRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class NotasApplication : Application() {

    /**
     * Creamos un CoroutineScope a nivel de aplicación para que
     * las operaciones de la base de datos no se cancelen
     * si un ViewModel se destruye.
     */
    val applicationScope = CoroutineScope(SupervisorJob())

    /**
     * Usamos 'lazy' para que la base de datos y el repositorio
     * solo se creen la primera vez que se necesiten.
     */

    // Instancia de la Base de Datos
    val database by lazy { NotasDatabase.getDatabase(this) }

    // Instancia del Repositorio (le pasamos el DAO)
    val repository by lazy { NotasRepository(database.notasDao()) }
}
