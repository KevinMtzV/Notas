package com.example.notasytareas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

// Modelo temporal (simula base de datos)
data class NoteItem(
    val id: Int,
    val title: String,
    val description: String,
    val isTask: Boolean,
    val isDone: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAddClick: (isTask: Boolean) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = notas, 1 = tareas
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Datos de prueba
    val notes = remember {
        mutableStateListOf(
            NoteItem(1, "Ideas del proyecto", "Definir estructura y base de datos", false),
            NoteItem(2, "Comprar componentes", "Cables, resistencias, protoboard", true, false),
            NoteItem(3, "Revisar app", "Agregar pantalla de tareas", true, true),
            NoteItem(4, "Reunión semanal", "Hablar sobre nuevas funciones", false),
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notas y Tareas", fontWeight = FontWeight.SemiBold) }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.width(180.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Nueva nota") },
                        onClick = {
                            showMenu = false
                            onAddClick(false)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Nueva tarea") },
                        onClick = {
                            showMenu = false
                            onAddClick(true)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 🔹 Campo de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por título o descripción...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 🔹 Pestañas (Notas / Tareas)
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Notas") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Tareas") })
            }

            val filteredList = remember(searchQuery, selectedTab) {
                notes.filter {
                    val matchesQuery =
                        it.title.contains(searchQuery, ignoreCase = true) ||
                                it.description.contains(searchQuery, ignoreCase = true)
                    val matchesTab = if (selectedTab == 0) !it.isTask else it.isTask
                    matchesQuery && matchesTab
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "No se encontraron resultados 😔",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                if (selectedTab == 0) {
                    NoteList(filteredList)
                } else {
                    TaskList(filteredList)
                }
            }
        }
    }
}

// 🔹 Listado de Notas
@Composable
fun NoteList(items: List<NoteItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { note ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// 🔹 Listado de Tareas
@Composable
fun TaskList(items: List<NoteItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { task ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var checked by remember { mutableStateOf(task.isDone) }
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (checked)
                                MaterialTheme.colorScheme.outline
                            else
                                MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    name = "Pantalla principal"
)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(onAddClick = {})
}
