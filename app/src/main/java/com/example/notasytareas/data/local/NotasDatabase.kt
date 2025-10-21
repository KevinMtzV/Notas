package com.example.notasytareas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notasytareas.data.models.Nota

@Database(entities = [Nota::class], version = 2, exportSchema = false)
abstract class NotasDatabase : RoomDatabase() {

    abstract fun notasDao(): NotasDao

    companion object {
        /**
         * @Volatile asegura que el valor de INSTANCE esté siempre actualizado
         * y sea el mismo para todos los hilos de ejecución.
         */
        @Volatile
        private var INSTANCE: NotasDatabase? = null

        fun getDatabase(context: Context): NotasDatabase {
            // Si INSTANCE no es nulo, la retorna.
            // Si es nulo, crea la base de datos dentro de un bloque 'synchronized'.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotasDatabase::class.java,
                    "notas_database" // Nombre del archivo de la base de datos
                )
                    .fallbackToDestructiveMigration() // Política de migración (simple por ahora)
                    .build()

                INSTANCE = instance
                // retorna la instancia
                instance
            }
        }
    }
}