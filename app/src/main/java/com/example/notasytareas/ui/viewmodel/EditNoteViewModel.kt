package com.example.notasytareas.ui.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasytareas.AlarmScheduler
import com.example.notasytareas.ReminderBroadcastReceiver
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.data.models.Recordatorio
import com.example.notasytareas.data.repository.NotasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.TimeZone

data class EditNoteUiState(
    val title: String = "",
    val description: String = "",
    val photoUris: List<String> = emptyList(),
    val videoUris: List<String> = emptyList(),
    val audioUris: List<String> = emptyList(),
    val isDone: Boolean = false,
    val fechaLimite: Long? = null,
    // CAMBIO 1: Reemplazamos el recordatorio único por una lista
    val reminders: List<Recordatorio> = emptyList(),
    val showDatePicker: Boolean = false,
    val showReminderDatePicker: Boolean = false,
    val showReminderTimePicker: Boolean = false,
    val tempReminderDate: Long? = null
)

class EditNoteViewModel(
    private val repository: NotasRepository,
    private val noteId: Int,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditNoteUiState())
    val uiState: StateFlow<EditNoteUiState> = _uiState.asStateFlow()

    private var notaActual: Nota? = null

    val isNuevaNota: Boolean = (noteId == -1)

    // Variables para control de archivos
    private var isSaved = false
    private val archivosNuevosCreados = mutableListOf<String>()
    // ------------------------------------------------

    init {
        if (!isNuevaNota) {
            viewModelScope.launch {
                // 1. Cargar la Nota
                repository.obtenerNotaPorId(noteId).first { notaDb ->
                    if (notaDb != null) {
                        notaActual = notaDb
                        _uiState.value = _uiState.value.copy(
                            title = notaDb.titulo,
                            description = notaDb.contenido,
                            photoUris = notaDb.photoUris,
                            videoUris = notaDb.videoUris,
                            audioUris = notaDb.audioUris,
                            isDone = notaDb.isDone,
                            fechaLimite = notaDb.fechaLimite
                            // reminder ya no se carga aquí, se carga abajo
                        )
                    }
                    true
                }

                // CAMBIO 2: Cargar los recordatorios asociados a esta nota
                repository.obtenerRecordatorios(noteId).collect { listaRecordatorios ->
                    _uiState.value = _uiState.value.copy(reminders = listaRecordatorios)
                }
            }
        }
    }

    // --- Funciones de Archivos (Sin cambios) ---
    private fun borrarArchivoFisico(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file") {
                val file = File(uri.path!!)
                if (file.exists()) file.delete()
            }
        } catch (e: Exception) {
            Log.e("Archivo", "Error al borrar archivo: ${e.message}")
        }
    }
    // -------------------------------------------

    fun onTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    // --- Gestión de Multimedia (Sin cambios importantes) ---
    fun addPhotoUri(uri: String) {
        val currentUris = _uiState.value.photoUris.toMutableList()
        currentUris.add(uri)
        _uiState.value = _uiState.value.copy(photoUris = currentUris)
        archivosNuevosCreados.add(uri)
    }

    fun removePhotoUri(uri: String) {
        val currentUris = _uiState.value.photoUris.toMutableList()
        currentUris.remove(uri)
        _uiState.value = _uiState.value.copy(photoUris = currentUris)
        archivosNuevosCreados.remove(uri)
        borrarArchivoFisico(uri)
    }

    fun addVideoUri(uri: String) {
        val currentUris = _uiState.value.videoUris.toMutableList()
        currentUris.add(uri)
        _uiState.value = _uiState.value.copy(videoUris = currentUris)
        archivosNuevosCreados.add(uri)
    }

    fun removeVideoUri(uri: String) {
        val currentUris = _uiState.value.videoUris.toMutableList()
        currentUris.remove(uri)
        _uiState.value = _uiState.value.copy(videoUris = currentUris)
        archivosNuevosCreados.remove(uri)
        borrarArchivoFisico(uri)
    }

    fun addAudioUri(uri: String) {
        val currentUris = _uiState.value.audioUris.toMutableList()
        currentUris.add(uri)
        _uiState.value = _uiState.value.copy(audioUris = currentUris)
        archivosNuevosCreados.add(uri)
    }

    fun removeAudioUri(uri: String) {
        val currentUris = _uiState.value.audioUris.toMutableList()
        currentUris.remove(uri)
        _uiState.value = _uiState.value.copy(audioUris = currentUris)
        archivosNuevosCreados.remove(uri)
        borrarArchivoFisico(uri)
    }

    fun onIsDoneChange(isDone: Boolean) {
        _uiState.value = _uiState.value.copy(isDone = isDone)
    }

    fun onFechaLimiteChange(newFecha: Long?) {
        _uiState.value = _uiState.value.copy(fechaLimite = newFecha)
    }

    // --- NUEVAS FUNCIONES PARA RECORDATORIOS MÚLTIPLES ---

    // Agrega un recordatorio a la lista visual (aún no guardado en BD)
    fun addReminder(time: Long) {
        val safeId = if (noteId == -1) 0 else noteId
        // Creamos un recordatorio con ID 0 (Room generará el ID real al guardar)
        val nuevoRecordatorio = Recordatorio(notaId = safeId, time = time)

        val listaActual = _uiState.value.reminders.toMutableList()
        listaActual.add(nuevoRecordatorio)
        _uiState.value = _uiState.value.copy(reminders = listaActual)
    }

    // Elimina un recordatorio de la lista y cancela su alarma si ya existía
    fun removeReminder(recordatorio: Recordatorio) {
        val listaActual = _uiState.value.reminders.toMutableList()
        listaActual.remove(recordatorio)
        _uiState.value = _uiState.value.copy(reminders = listaActual)

        // Si el recordatorio tenía un ID real (ya estaba en BD), cancelamos la alarma ahora mismo
        if (recordatorio.id > 0) {
            AlarmScheduler.cancelAlarm(context, recordatorio.id)
        }
    }

    // -----------------------------------------------------

    fun onShowDatePickerChange(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDatePicker = show)
    }

    // Manejo de Date/Time Pickers para Recordatorios
    fun onReminderDateChange(newReminderDate: Long?) {
        if (newReminderDate == null) {
            // Si cancela, reseteamos pickers
            _uiState.value = _uiState.value.copy(
                tempReminderDate = null,
                showReminderDatePicker = false,
                showReminderTimePicker = false
            )
        } else {
            // Si selecciona fecha, mostramos TimePicker
            _uiState.value = _uiState.value.copy(
                tempReminderDate = newReminderDate,
                showReminderDatePicker = false,
                showReminderTimePicker = true
            )
        }
    }

    fun onReminderTimeChange(hour: Int, minute: Int) {
        val reminderDate = _uiState.value.tempReminderDate ?: return

        // Lógica de Calendario (igual que antes)
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCalendar.timeInMillis = reminderDate

        val localCalendar = Calendar.getInstance()
        localCalendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
        localCalendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
        localCalendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
        localCalendar.set(Calendar.HOUR_OF_DAY, hour)
        localCalendar.set(Calendar.MINUTE, minute)
        localCalendar.set(Calendar.SECOND, 0)
        localCalendar.set(Calendar.MILLISECOND, 0)

        // CAMBIO 3: En lugar de reemplazar 'reminder', llamamos a addReminder
        addReminder(localCalendar.timeInMillis)

        // Reseteamos estados de los pickers
        _uiState.value = _uiState.value.copy(
            showReminderTimePicker = false,
            tempReminderDate = null
        )
    }

    fun onShowReminderDatePickerChange(show: Boolean) {
        _uiState.value = _uiState.value.copy(showReminderDatePicker = show)
    }

    fun onShowReminderTimePickerChange(show: Boolean) {
        _uiState.value = _uiState.value.copy(showReminderTimePicker = show)
    }

    fun guardarNota(isTask: Boolean, onSuccess: () -> Unit) {

        if (uiState.value.title.isBlank()) return

        val nota = Nota(
            id = if (noteId == -1) 0 else noteId,
            titulo = uiState.value.title,
            contenido = uiState.value.description,
            photoUris = uiState.value.photoUris,
            videoUris = uiState.value.videoUris,
            audioUris = uiState.value.audioUris,
            isTask = isTask,
            isDone = uiState.value.isDone,
            fechaLimite = uiState.value.fechaLimite,
            reminder = null // Ya no usamos este campo en la tabla Notas
        )

        viewModelScope.launch {
            try {
                // 1. Guardar o Actualizar la NOTA principal
                val finalNoteId: Int
                if (noteId == 0 || noteId == -1) {
                    val nuevoIdLong = repository.insertarNota(nota)
                    finalNoteId = nuevoIdLong.toInt()
                } else {
                    repository.actualizarNota(nota)
                    finalNoteId = noteId
                }

                // Creamos una copia de la nota con el ID correcto para pasarla al Scheduler
                val notaFinal = nota.copy(id = finalNoteId)

                // 2. Gestión de RECORDATORIOS (Borrado y Re-inserción)
                // Estrategia: Borrar los recordatorios viejos de la BD para esta nota
                // y guardar los que están actualmente en la lista visual.

                if (noteId != -1) {
                    // Si editamos, limpiamos alarmas viejas de la BD primero
                    // (Opcional: podrías cancelar alarmas viejas aquí si no lo hiciste en removeReminder)
                    repository.borrarRecordatoriosDeNota(finalNoteId)
                }

                // 3. Insertar nuevos recordatorios y programar alarmas
                val recordatoriosParaGuardar = uiState.value.reminders.map {
                    it.copy(id = 0, notaId = finalNoteId) // Aseguramos que tengan el ID de la nota correcto
                }

                recordatoriosParaGuardar.forEach { recordatorio ->
                    // Insertamos en Room y obtenemos el ID generado (ej: 101, 102...)
                    val newReminderId = repository.insertarRecordatorio(recordatorio)

                    // Programamos la alarma usando el ID único del recordatorio
                    val recordatorioGuardado = recordatorio.copy(id = newReminderId.toInt())
                    AlarmScheduler.scheduleAlarm(context, notaFinal, recordatorioGuardado)
                }

                isSaved = true
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!isSaved) {
            Log.d("EditNoteViewModel", "Saliendo sin guardar. Limpiando archivos huerfanos.")
            for (uri in archivosNuevosCreados) {
                borrarArchivoFisico(uri)
            }
        }
    }
}

class EditNoteViewModelFactory(
    private val repository: NotasRepository,
    private val noteId: Int,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditNoteViewModel(repository, noteId, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}