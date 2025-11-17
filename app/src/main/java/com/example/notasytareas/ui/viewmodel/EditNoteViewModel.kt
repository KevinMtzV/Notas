package com.example.notasytareas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.data.repository.Notas_repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EditNoteUiState(
    val title: String = "",
    val description: String = "",
    val photoUris: List<String> = emptyList(),
    val isDone: Boolean = false,
    val fechaLimite: Long? = null,
    val showDatePicker: Boolean = false
)

class EditNoteViewModel(
    private val repository: Notas_repository,
    private val noteId: Int
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
                            isDone = notaDb.isDone,
                            fechaLimite = notaDb.fechaLimite
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

    fun onPhotoUrisChange(newPhotoUris: List<String>) {
        _uiState.value = _uiState.value.copy(photoUris = newPhotoUris)
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

    fun onIsDoneChange(isDone: Boolean) {
        _uiState.value = _uiState.value.copy(isDone = isDone)
    }

    fun onFechaLimiteChange(newFecha: Long?) {
        _uiState.value = _uiState.value.copy(fechaLimite = newFecha)
    }

    fun onShowDatePickerChange(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDatePicker = show)
    }

    fun guardarNota(isTask: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (isNuevaNota) {
                val nuevaNota = Nota(
                    titulo = currentState.title,
                    contenido = currentState.description,
                    photoUris = currentState.photoUris,
                    isTask = isTask,
                    isDone = currentState.isDone,
                    fechaLimite = currentState.fechaLimite
                )
                repository.insertarNota(nuevaNota)
            } else {
                val notaActualizada = notaActual?.copy(
                    titulo = currentState.title,
                    contenido = currentState.description,
                    photoUris = currentState.photoUris,
                    isTask = isTask,
                    isDone = currentState.isDone,
                    fechaLimite = currentState.fechaLimite
                )
                if (notaActualizada != null) {
                    repository.actualizarNota(notaActualizada)
                }
            }
        }
    }
}

class EditNoteViewModelFactory(
    private val repository: Notas_repository,
    private val noteId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditNoteViewModel(repository, noteId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
