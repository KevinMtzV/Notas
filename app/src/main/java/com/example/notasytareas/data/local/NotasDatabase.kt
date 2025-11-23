package com.example.notasytareas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notasytareas.data.models.Nota

@Database(entities = [Nota::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NotasDatabase : RoomDatabase() {

    abstract fun notasDao(): NotasDao

    companion object {
        @Volatile
        private var INSTANCE: NotasDatabase? = null

        fun getDatabase(context: Context): NotasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotasDatabase::class.java,
                    "notas_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}