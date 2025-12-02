package com.example.notasytareas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.data.models.Recordatorio
import kotlinx.coroutines.flow.Flow 

@Dao
interface NotasDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarNota(nota: Nota): Long

    @Query("SELECT * FROM notas_tabla ORDER BY id DESC")
    fun obtenerTodasLasNotas(): Flow<List<Nota>>


    @Query("SELECT * FROM notas_tabla WHERE reminder IS NOT NULL")
    suspend fun obtenerNotasConRecordatorio(): List<Nota>

    @Update
    suspend fun actualizarNota(nota: Nota)

    @Delete
    suspend fun eliminarNota(nota: Nota)

    @Query("SELECT * FROM notas_tabla WHERE id = :id")
    fun obtenerNotaPorId(id: Int): Flow<Nota?>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRecordatorio(recordatorio: Recordatorio): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRecordatorios(recordatorios: List<Recordatorio>)

    @Query("SELECT * FROM recordatorios WHERE notaId = :notaId")
    fun obtenerRecordatoriosDeNota(notaId: Int): Flow<List<Recordatorio>>

    @Query("DELETE FROM recordatorios WHERE notaId = :notaId")
    suspend fun borrarRecordatoriosDeNota(notaId: Int)

    @Query("DELETE FROM recordatorios WHERE id = :id")
    suspend fun borrarRecordatorioPorId(id: Int)

    // Obtener recordatorios futuros para reprogramar al reiniciar
    @Query("SELECT * FROM recordatorios WHERE time > :tiempoActual")
    suspend fun obtenerRecordatoriosFuturos(tiempoActual: Long): List<Recordatorio>

    // Necesitamos obtener la nota de forma síncrona (suspend) para el bucle
    @Query("SELECT * FROM notas_tabla WHERE id = :id LIMIT 1")
    suspend fun obtenerNotaDirecta(id: Int): Nota?
}