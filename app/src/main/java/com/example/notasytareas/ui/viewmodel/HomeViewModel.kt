package com.example.notasytareas.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.data.repository.Notas_repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: Notas_repository) : ViewModel() {

    private val _todasLasNotas: Flow<List<Nota>> = repository.todasLasNotas

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _showMenu = MutableStateFlow(false)
    val showMenu: StateFlow<Boolean> = _showMenu.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val listaNotasFiltradas: Flow<List<Nota>> = combine(
        _todasLasNotas,
        searchQuery,
        selectedTab
    ) { notas, query, tab ->
        notas.filter { nota ->
            val matchesQuery =
                nota.titulo.contains(query, ignoreCase = true) ||
                        nota.contenido.contains(query, ignoreCase = true)
            val matchesTab = if (tab == 0) !nota.isTask else nota.isTask
            matchesQuery && matchesTab
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTabSelected(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun onShowMenuChanged(show: Boolean) {
        _showMenu.value = show
    }

    fun insertarNota(nota: Nota) = viewModelScope.launch {
        repository.insertarNota(nota)
    }

    fun actualizarNota(nota: Nota) = viewModelScope.launch {
        repository.actualizarNota(nota)
    }
}


class HomeViewModelFactory(private val repository: Notas_repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}