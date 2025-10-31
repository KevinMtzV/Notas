package com.example.notasytareas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasytareas.data.repository.NotasRepository
import com.example.notasytareas.data.models.Nota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// 1. El ViewModel ahora toma el noteId
class EditNoteViewModel(
    private val repository: NotasRepository,
    private val noteId: Int
) : ViewModel() {

    // 2. Estado para la nota que se carga desde la BD
    private val _notaCargada = MutableStateFlow<Nota?>(null)
    val notaCargada = _notaCargada.asStateFlow()

    // 3. Variable para saber si es una nota nueva
    val isNuevaNota: Boolean = (noteId == -1)

    init {
        // 4. Si NO es una nota nueva, la cargamos
        if (!isNuevaNota) {
            viewModelScope.launch {
                // Usamos .first() para obtener el valor una sola vez
                repository.obtenerNotaPorId(noteId).first { nota ->
                    _notaCargada.value = nota
                    true // Detiene la recolección
                }
            }
        }
    }

    /**
     * Guarda la nota. Sabe si debe insertar o actualizar.
     */
    fun guardarNota(
        titulo: String,
        contenido: String,
        isTask: Boolean,
        isDone: Boolean,
        fechaLimite: Long?
    ) {
        viewModelScope.launch {
            if (isNuevaNota) {
                // Es NUEVA: Insertar
                val nuevaNota = Nota(
                    titulo = titulo,
                    contenido = contenido,
                    isTask = isTask,
                    isDone = isDone,
                    fechaLimite = fechaLimite
                )
                repository.insertarNota(nuevaNota)
            } else {
                // Es EXISTENTE: Actualizar
                // Creamos una copia de la nota cargada con los datos nuevos
                val notaActualizada = _notaCargada.value?.copy(
                    titulo = titulo,
                    contenido = contenido,
                    isTask = isTask,
                    isDone = isDone,
                    fechaLimite = fechaLimite
                )
                if (notaActualizada != null) {
                    repository.actualizarNota(notaActualizada)
                }
            }
        }
    }
}


// 5. La Factory ahora también necesita el noteId
class EditNoteViewModelFactory(
    private val repository: NotasRepository,
    private val noteId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 6. Pasamos el noteId al constructor del ViewModel
            return EditNoteViewModel(repository, noteId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}