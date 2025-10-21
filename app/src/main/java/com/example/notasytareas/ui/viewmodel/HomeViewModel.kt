package com.example.notasytareas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasytareas.data.repository.NotasRepository
import com.example.notasytareas.data.models.Nota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: NotasRepository) : ViewModel() {

    // Usamos StateFlow para exponer la lista de notas a la UI
    private val _listaNotas = MutableStateFlow<List<Nota>>(emptyList())
    val listaNotas = _listaNotas.asStateFlow()

    init {
        // En cuanto el ViewModel se crea, empieza a observar la base de datos
        viewModelScope.launch {
            repository.todasLasNotas
                .catch { e ->
                    // Manejar errores si es necesario
                    e.printStackTrace()
                }
                .collect { notas ->
                    // Cuando 'todasLasNotas' (el Flow) emite una nueva lista,
                    // actualizamos nuestro StateFlow.
                    _listaNotas.value = notas
                }
        }
    }

    // Esta función la usaremos desde EditNoteScreen
    fun insertarNota(nota: Nota) = viewModelScope.launch {
        repository.insertarNota(nota)
    }

    fun actualizarNota(nota: Nota) = viewModelScope.launch {
        repository.actualizarNota(nota)
    }
}

/**
 * ¡MUY IMPORTANTE!
 * Como nuestro HomeViewModel necesita un 'NotasRepository' en su constructor,
 * no podemos dejar que Android lo cree por defecto.
 *
 * Debemos crear una "Fábrica" (Factory) que le diga a Android
 * cómo construir nuestro HomeViewModel.
 */
class HomeViewModelFactory(private val repository: NotasRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}