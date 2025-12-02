package com.example.notasytareas

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.notasytareas.ReminderBroadcastReceiver
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.data.models.Recordatorio

object AlarmScheduler {

    fun scheduleAlarm(context: Context, nota: Nota, recordatorio: Recordatorio) {
        Log.d("AlarmTest", "Iniciando scheduleAlarm. NotaID: ${nota.id}, RecID: ${recordatorio.id}")

        // --- CORRECCIÓN ---
        // Validamos usando recordatorio.time, NO nota.reminder
        if (recordatorio.time <= System.currentTimeMillis()) {
            Log.w("AlarmTest", "La alarma no se programó porque la hora ya pasó.")
            return
        }
        // ------------------

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("note_id", nota.id)
            putExtra("note_title", nota.titulo)
            putExtra("note_content", nota.contenido)
            putExtra("is_task", nota.isTask)
            putExtra("reminder_id", recordatorio.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            recordatorio.id, // ID Único por recordatorio
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                recordatorio.time, // Usamos la hora del recordatorio
                pendingIntent
            )
            // Log actualizado para mostrar la hora correcta
            Log.d("AlarmTest", "¡ÉXITO! Alarma programada para: ${recordatorio.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmTest", "ERROR: Falta permiso SCHEDULE_EXACT_ALARM")
        }
    }

    fun cancelAlarm(context: Context, recordatorioId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            recordatorioId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("AlarmTest", "Alarma cancelada ID: $recordatorioId")
    }
}