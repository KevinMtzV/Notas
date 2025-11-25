package com.example.notasytareas.ui.viewmodel

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasytareas.ReminderBroadcastReceiver
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.data.repository.NotasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    }

    fun removePhotoUri(uri: String) {
        val currentUris = _uiState.value.photoUris.toMutableList()
        currentUris.remove(uri)
        _uiState.value = _uiState.value.copy(photoUris = currentUris)
    }

    fun addVideoUri(uri: String) {
        val currentUris = _uiState.value.videoUris.toMutableList()
        currentUris.add(uri)
        _uiState.value = _uiState.value.copy(videoUris = currentUris)
    }

    fun removeVideoUri(uri: String) {
        val currentUris = _uiState.value.videoUris.toMutableList()
        currentUris.remove(uri)
        _uiState.value = _uiState.value.copy(videoUris = currentUris)
    }

    fun addAudioUri(uri: String) {
        val currentUris = _uiState.value.audioUris.toMutableList()
        currentUris.add(uri)
        _uiState.value = _uiState.value.copy(audioUris = currentUris)
    }

    fun removeAudioUri(uri: String) {
        val currentUris = _uiState.value.audioUris.toMutableList()
        currentUris.remove(uri)
        _uiState.value = _uiState.value.copy(audioUris = currentUris)
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
        val reminderDate = _uiState.value.tempReminderDate ?: return

        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCalendar.timeInMillis = reminderDate

        val localCalendar = Calendar.getInstance()
        localCalendar.set(
            utcCalendar.get(Calendar.YEAR),
            utcCalendar.get(Calendar.MONTH),
            utcCalendar.get(Calendar.DAY_OF_MONTH),
            hour,
            minute,
            0
        )
        localCalendar.set(Calendar.MILLISECOND, 0)

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

    fun guardarNota(isTask: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (isNuevaNota) {
                val newNota = Nota(
                    titulo = currentState.title,
                    contenido = currentState.description,
                    photoUris = currentState.photoUris,
                    videoUris = currentState.videoUris,
                    audioUris = currentState.audioUris,
                    isTask = isTask,
                    isDone = currentState.isDone,
                    fechaLimite = currentState.fechaLimite,
                    reminder = currentState.reminder
                )
                val newId = repository.insertarNota(newNota)
                if (currentState.reminder != null) {
                    scheduleReminder(newId.toInt(), currentState.reminder, currentState.title, currentState.description)
                }
            } else {
                val updatedNota = notaActual?.copy(
                    titulo = currentState.title,
                    contenido = currentState.description,
                    photoUris = currentState.photoUris,
                    videoUris = currentState.videoUris,
                    audioUris = currentState.audioUris,
                    isTask = isTask,
                    isDone = currentState.isDone,
                    fechaLimite = currentState.fechaLimite,
                    reminder = currentState.reminder
                )!!
                repository.actualizarNota(updatedNota)
                if (currentState.reminder != null) {
                    scheduleReminder(noteId, currentState.reminder, currentState.title, currentState.description)
                } else {
                    cancelReminder(noteId)
                }
            }
        }
    }


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
