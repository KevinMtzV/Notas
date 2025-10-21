package com.example.notasytareas.data.repository

import com.example.notasytareas.data.local.NotasDao
import com.example.notasytareas.data.models.Nota
import kotlinx.coroutines.flow.Flow

/**
 * El repositorio solo necesita el DAO como dependencia.
 * El ViewModel le pasará el DAO.
 */
class NotasRepository(private val notasDao: NotasDao) {

    // Simplemente exponemos las funciones del DAO.
    // El Flow notificará al ViewModel de cualquier cambio.
    val todasLasNotas: Flow<List<Nota>> = notasDao.obtenerTodasLasNotas()

    // Usamos 'suspend' para que el ViewModel tenga que llamarlo
    // desde una corrutina (ej. viewModelScope.launch).
    suspend fun insertarNota(nota: Nota) {
        notasDao.insertarNota(nota)
    }

    suspend fun actualizarNota(nota: Nota) {
        notasDao.actualizarNota(nota)
    }

    fun obtenerNotaPorId(id: Int): Flow<Nota?> {
        return notasDao.obtenerNotaPorId(id)
    }
}

