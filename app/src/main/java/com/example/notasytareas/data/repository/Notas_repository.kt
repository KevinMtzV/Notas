package com.example.notasytareas.data.repository

import com.example.notasytareas.data.models.Nota
import kotlinx.coroutines.flow.Flow

interface Notas_repository {
    val todasLasNotas: Flow<List<Nota>>
    suspend fun insertarNota(nota: Nota): Long
    suspend fun actualizarNota(nota: Nota)
    suspend fun eliminarNota(nota: Nota)
    fun obtenerNotaPorId(id: Int): Flow<Nota?>
}
