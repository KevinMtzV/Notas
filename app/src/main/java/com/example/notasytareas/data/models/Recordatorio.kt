package com.example.notasytareas.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recordatorios",
    foreignKeys = [
        ForeignKey(
            entity = Nota::class,
            parentColumns = ["id"],
            childColumns = ["notaId"],
            onDelete = ForeignKey.CASCADE // Si borras la nota, se borran sus alarmas
        )
    ],
    indices = [Index(value = ["notaId"])]
)
data class Recordatorio(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val notaId: Int,
    val time: Long,
    val isDone: Boolean = false // Opcional, por si quieres marcar alarmas como "ya sonó"
)