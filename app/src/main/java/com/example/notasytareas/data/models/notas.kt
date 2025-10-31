package com.example.notasytareas.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notas_tabla") // Define el nombre de la tabla
data class Nota(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val contenido: String,
    val isTask: Boolean = false,
    val isDone: Boolean = false,
    val fechaLimite: Long? = null // Almacenará el timestamp
)