package com.example.notasytareas.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notas_tabla") // nombre de la tabla
data class Nota(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val contenido: String,
    val photoUris: List<String> = emptyList(),
    val videoUris: List<String> = emptyList(),
    val audioUris: List<String> = emptyList(),
    val isTask: Boolean = false,
    val isDone: Boolean = false,
    val fechaLimite: Long? = null,
    val reminder: Long? = null
)