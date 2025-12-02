package com.example.notasytareas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasytareas.data.repository.NotasRepository
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.data.models.Recordatorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.util.Log

class NoteDetailViewModel(
    private val repository: NotasRepository,
    private val noteId: Int
) : ViewModel() {

    private val _nota = MutableStateFlow<Nota?>(null)
    val nota: StateFlow<Nota?> = _nota.asStateFlow()

    private val _recordatorios = MutableStateFlow<List<Recordatorio>>(emptyList())
    val recordatorios: StateFlow<List<Recordatorio>> = _recordatorios.asStateFlow()

    init {
        viewModelScope.launch {
            // Usamos .collect para escuchar continuamente los cambios
            repository.obtenerNotaPorId(noteId).collect { notaDb ->
                _nota.value = notaDb
            }
        }
        viewModelScope.launch {
            Log.d("DetailDebug", "Buscando recordatorios para nota ID: $noteId") // <--- LOG 1

            // Escuchar cambios en los Recordatorios
            repository.obtenerRecordatorios(noteId).collect { lista ->
                Log.d("DetailDebug", "Encontrados: ${lista.size} recordatorios") // <--- LOG 2
                _recordatorios.value = lista
            }
        }
    }

    fun onTaskStatusChanged(isChecked: Boolean) {
        val currentNota = _nota.value ?: return
        val updatedNota = currentNota.copy(isDone = isChecked)

        viewModelScope.launch {
            repository.actualizarNota(updatedNota)
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
