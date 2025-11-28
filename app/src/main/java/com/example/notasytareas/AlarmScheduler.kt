package com.example.notasytareas

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.notasytareas.ReminderBroadcastReceiver
import com.example.notasytareas.data.models.Nota

object AlarmScheduler {

    fun scheduleAlarm(context: Context, nota: Nota) {
        // Validar que tenga fecha y que sea futura
        if (nota.reminder == null || nota.reminder <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Crear el Intent que apunta a TU Receiver
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("note_id", nota.id)
            putExtra("note_title", nota.titulo)
            putExtra("note_content", nota.contenido)
        }

        // Crear el PendingIntent único para esta nota
        // Usamos el ID de la nota como request code para que sea único
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            nota.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Programar la alarma
        try {
            // setExactAndAllowWhileIdle asegura que suene aunque el cel esté en reposo (Doze mode)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nota.reminder,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Alarma programada para nota ${nota.id} a las ${nota.reminder}")
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Falta permiso SCHEDULE_EXACT_ALARM")
        }
    }

    fun cancelAlarm(context: Context, noteId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}