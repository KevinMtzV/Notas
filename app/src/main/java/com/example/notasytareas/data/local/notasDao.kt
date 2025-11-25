package com.example.notasytareas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.example.notasytareas.data.models.Nota
import kotlinx.coroutines.flow.Flow 

@Dao
interface NotasDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarNota(nota: Nota): Long

    @Query("SELECT * FROM notas_tabla ORDER BY id DESC")
    fun obtenerTodasLasNotas(): Flow<List<Nota>>

    @Update
    suspend fun actualizarNota(nota: Nota)

    @Delete
    suspend fun eliminarNota(nota: Nota)

    @Query("SELECT * FROM notas_tabla WHERE id = :id")
    fun obtenerNotaPorId(id: Int): Flow<Nota?> 
}