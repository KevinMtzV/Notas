package com.example.notasytareas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.notasytareas.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Verificamos si la acción es de reinicio
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d("BootReceiver", "El celular se reinició. Iniciando reprogramación...")

            // goAsync() mantiene vivo el Receiver mientras leemos la base de datos
            val pendingResult = goAsync()

            val application = context.applicationContext as NotasApplication
            val repository = application.repository

            // Corrutina para trabajar en segundo plano
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val tiempoActual = System.currentTimeMillis()
                    // Usamos la función SUSPEND que devuelve una List<Nota> directa
                    // NO usamos el Flow
                    val recordatoriosFuturos = repository.obtenerRecordatoriosFuturos(tiempoActual)

                    var contador = 0

                    // Recorrer y reprogramar
                    recordatoriosFuturos.forEach { recordatorio ->
                        // 3. Buscamos la nota asociada a este recordatorio
                        val nota = repository.obtenerNotaDirecta(recordatorio.notaId)

                        if (nota != null) {
                            // 4. AHORA SÍ pasamos los 3 parámetros requeridos: context, nota, recordatorio
                            AlarmScheduler.scheduleAlarm(context, nota, recordatorio)
                            contador++
                        }
                    }
                    Log.d("BootReceiver", "ÉXITO: Se reprogramaron $contador alarmas.")

                } catch (e: Exception) {
                    Log.e("BootReceiver", "FALLO: Error al reprogramar alarmas: ${e.message}")
                } finally {
                    // Liberar el Receiver
                    pendingResult.finish()
                }
            }
        }
    }
}