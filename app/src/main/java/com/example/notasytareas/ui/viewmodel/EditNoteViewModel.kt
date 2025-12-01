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
    val reminder: Long? = null,
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
    private var isSaved = false // Para saber si guardamos o cancelamos
    private val archivosNuevosCreados = mutableListOf<String>() // Lista de archivos
    // ------------------------------------------------

    init {
        if (!isNuevaNota) {
            viewModelScope.launch {
                repository.obtenerNotaPorId(noteId).first { notaDb ->
                    if (notaDb != null) {
                        notaActual = notaDb
                        _uiState.value = EditNoteUiState(
                            title = notaDb.titulo,
                            description = notaDb.contenido,
                            photoUris = notaDb.photoUris,
                            videoUris = notaDb.videoUris,
                            audioUris = notaDb.audioUris,
                            isDone = notaDb.isDone,
                            fechaLimite = notaDb.fechaLimite,
                            reminder = notaDb.reminder
                        )
                    }
                    true
                }
            }
        }
    }

    // Función auxiliar para borrar archivos del disco
    private fun borrarArchivoFisico(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            // Solo borramos si es un archivo local (scheme "file")
            // Los archivos de la galería (scheme "content") no los tocamos por seguridad
            if (uri.scheme == "file") {
                val file = File(uri.path!!)
                if (file.exists()) {
                    val deleted = file.delete()
                    Log.d("Archivo", "Archivo eliminado: $uriString -> Exito: $deleted")
                }
            }
        } catch (e: Exception) {
            Log.e("Archivo", "Error al borrar archivo: ${e.message}")
        }
    }
    // -------------------------------------------------------------

    fun onTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun addPhotoUri(uri: String) {
        val currentUris = _uiState.value.photoUris.toMutableList()
        currentUris.add(uri)
        _uiState.value = _uiState.value.copy(photoUris = currentUris)

        // Agregamos a la lista de archivos nuevos de esta sesión
        archivosNuevosCreados.add(uri)
    }

    fun removePhotoUri(uri: String) {
        val currentUris = _uiState.value.photoUris.toMutableList()
        currentUris.remove(uri)
        _uiState.value = _uiState.value.copy(photoUris = currentUris)

        // Limpiamos de la lista de nuevos y borramos físicamente
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

    fun onShowDatePickerChange(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDatePicker = show)
    }

    fun onReminderDateChange(newReminderDate: Long?) {
        if (newReminderDate == null) {
            _uiState.value = _uiState.value.copy(
                reminder = null,
                tempReminderDate = null,
                showReminderDatePicker = false,
                showReminderTimePicker = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                tempReminderDate = newReminderDate,
                showReminderDatePicker = false,
                showReminderTimePicker = true
            )
        }
    }

    fun onReminderTimeChange(hour: Int, minute: Int) {
        // Obtenemos la fecha cruda que seleccionó el DatePicker (que viene en UTC)
        val reminderDate = _uiState.value.tempReminderDate ?: return

        // Creamos un calendario en UTC para LEER qué día seleccionó el usuario
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCalendar.timeInMillis = reminderDate

        // Creamos un calendario LOCAL para ESTABLECER la alarma
        val localCalendar = Calendar.getInstance()

        // Copiamos Año, Mes y Día del UTC al Local
        localCalendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
        localCalendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
        localCalendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))

        // Ponemos la hora que eligió el usuario en el TimePicker
        localCalendar.set(Calendar.HOUR_OF_DAY, hour)
        localCalendar.set(Calendar.MINUTE, minute)
        localCalendar.set(Calendar.SECOND, 0)
        localCalendar.set(Calendar.MILLISECOND, 0)

        // Guardamos el resultado final
        _uiState.value = _uiState.value.copy(
            reminder = localCalendar.timeInMillis,
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

        // Si el título está vacío, no guardamos y NO cerramos la pantalla
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
            reminder = uiState.value.reminder
        )

        viewModelScope.launch {
            try {
                if (noteId == 0 || noteId == -1) {
                    // INSERTAR
                    val nuevoIdLong = repository.insertarNota(nota)
                    val nuevoId = nuevoIdLong.toInt()

                    // Programar alarma con el ID real
                    val notaGuardada = nota.copy(id = nuevoId)
                    AlarmScheduler.scheduleAlarm(context, notaGuardada)
                } else {
                    // ACTUALIZAR
                    repository.actualizarNota(nota)

                    // Reprogramar alarma
                    AlarmScheduler.cancelAlarm(context, nota.id)
                    AlarmScheduler.scheduleAlarm(context, nota)
                }

                // --- NUEVO: Marcamos que se guardó exitosamente ---
                isSaved = true
                // -------------------------------------------------

                // Llamamos a onSuccess() cuando termina
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Limpieza automática al salir sin guardar ---
    override fun onCleared() {
        super.onCleared()
        // Si el usuario sale (back button) y NO guardó (isSaved es false)
        if (!isSaved) {
            Log.d("EditNoteViewModel", "Saliendo sin guardar. Limpiando archivos huerfanos.")

            // Borramos solo los archivos creados en esta sesión
            for (uri in archivosNuevosCreados) {
                borrarArchivoFisico(uri)
            }
        }
    }
    // -------------------------------------------------------

    private fun scheduleReminder(noteId: Int, reminderTime: Long, title: String, content: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra("note_id", noteId)
            putExtra("note_title", title)
            putExtra("note_content", content)
        }
        val pendingIntent = PendingIntent.getBroadcast(context, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }

    private fun cancelReminder(noteId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
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