package com.example.notasytareas.data.repository

import com.example.notasytareas.data.local.NotasDao
import com.example.notasytareas.data.models.Nota
import kotlinx.coroutines.flow.Flow

class NotasRepository(private val notasDao: NotasDao) : Notas_repository {

    override val todasLasNotas: Flow<List<Nota>> = notasDao.obtenerTodasLasNotas()

    override suspend fun insertarNota(nota: Nota): Long = notasDao.insertarNota(nota)

    override suspend fun actualizarNota(nota: Nota) = notasDao.actualizarNota(nota)

    override suspend fun eliminarNota(nota: Nota) = notasDao.eliminarNota(nota)

    override fun obtenerNotaPorId(id: Int): Flow<Nota?> = notasDao.obtenerNotaPorId(id)

    override suspend fun obtenerNotasActivasParaRecordatorio(): List<Nota> = notasDao.obtenerNotasConRecordatorio()

}
