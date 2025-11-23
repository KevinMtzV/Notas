package com.example.notasytareas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasytareas.data.repository.NotasRepository
import com.example.notasytareas.data.models.Nota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    private val repository: NotasRepository,
    private val noteId: Int
) : ViewModel() {

    private val _nota = MutableStateFlow<Nota?>(null)
    val nota: StateFlow<Nota?> = _nota.asStateFlow()

    init {
        viewModelScope.launch {
            // Usamos .collect para escuchar continuamente los cambios
            repository.obtenerNotaPorId(noteId).collect { notaDb ->
                _nota.value = notaDb
            }
        }
    }

    fun addPhotoUri(uri: String) {
        _nota.value?.let { currentNota ->
            val updatedUris = currentNota.photoUris + uri
            val updatedNota = currentNota.copy(photoUris = updatedUris)
            viewModelScope.launch {
                repository.actualizarNota(updatedNota)
            }
        }
    }

    fun addVideoUri(uri: String) {
        _nota.value?.let { currentNota ->
            val updatedUris = currentNota.videoUris + uri
            val updatedNota = currentNota.copy(videoUris = updatedUris)
            viewModelScope.launch {
                repository.actualizarNota(updatedNota)
            }
        }
    }
}

class NoteDetailViewModelFactory(
    private val repository: NotasRepository,
    private val noteId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteDetailViewModel(repository, noteId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
