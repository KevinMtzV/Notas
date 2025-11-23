package com.example.notasytareas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.example.notasytareas.data.models.Nota
import kotlinx.coroutines.flow.Flow // Importante usar Flow

@Dao
interface NotasDao {

    /**
     * Inserta una nota. Si la nota ya existe, la reemplaza.
     * 'suspend' la hace una función de corrutina (se ejecuta en segundo plano).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarNota(nota: Nota)

    /**
     * Obtiene todas las notas de la tabla, ordenadas por ID descendente.
     * Usa Flow para que la UI se actualice automáticamente cuando los datos cambien.
     */
    @Query("SELECT * FROM notas_tabla ORDER BY id DESC")
    fun obtenerTodasLasNotas(): Flow<List<Nota>>

    // Aquí podrías añadir @Update y @Delete después
    @Update
    suspend fun actualizarNota(nota: Nota)

    @Delete
    suspend fun eliminarNota(nota: Nota)

    @Query("SELECT * FROM notas_tabla WHERE id = :id")
    fun obtenerNotaPorId(id: Int): Flow<Nota?> // 'Nota?' por si el ID no existe
}