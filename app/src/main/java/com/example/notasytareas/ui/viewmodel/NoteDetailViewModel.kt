package com.example.notasytareas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasytareas.data.repository.NotasRepository
import com.example.notasytareas.data.models.Nota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    private val repository: NotasRepository,
    private val noteId: Int
) : ViewModel() {

    private val _nota = MutableStateFlow<Nota?>(null)
    val nota: StateFlow<Nota?> = _nota.asStateFlow()

    init {
        viewModelScope.launch {
            repository.obtenerNotaPorId(noteId).first { notaDb ->
                _nota.value = notaDb
                true
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
