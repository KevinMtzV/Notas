package com.example.notasytareas.ui

import androidx.compose.foundation.clickable
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
import com.example.notasytareas.data.models.Nota
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.ui.viewmodel.HomeViewModel
import com.example.notasytareas.ui.viewmodel.HomeViewModelFactory
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAddClick: (isTask: Boolean) -> Unit,
               onNoteClick: (Nota) -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = notas, 1 = tareas
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // << 4. Obtenemos el ViewModel usando la Factory (como vimos antes) >>
    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application.repository)
    )

    // << 5. Observamos la lista de notas REAL de la base de datos >>
    //    'collectAsState' hace que la UI se redibuje sola cuando la lista cambia.
    val notas by viewModel.listaNotas.collectAsState()

    // << 6. Eliminamos la lista 'notes' harcodeada >>
    /*
    val notes = remember {
        mutableStateListOf(...)
    }
    */

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
                            onAddClick(false) // false = no es tarea
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Nueva tarea") },
                        onClick = {
                            showMenu = false
                            onAddClick(true) // true = es tarea
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

            // << 7. Filtramos la lista 'notas' del ViewModel >>
            val filteredList = remember(searchQuery, selectedTab, notas) {
                // Usamos 'notas' (del ViewModel) en lugar de 'notes' (harcodeada)
                notas.filter {
                    val matchesQuery =
                        // << 8. Usamos los nombres de la Entidad: 'titulo' y 'contenido' >>
                        it.titulo.contains(searchQuery, ignoreCase = true) ||
                                it.contenido.contains(searchQuery, ignoreCase = true)

                    // 'it.isTask' ahora viene de la base de datos
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
                        text = "No se encontraron resultados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                // << 9. Pasamos la 'filteredList' (que ahora es List<Nota>) >>
                if (selectedTab == 0) {
                    // 2. Pasa el callback a NoteList
                    NoteList(filteredList, onNoteClick = onNoteClick)
                } else {
                    // 3. Pasa el callback a TaskList
                    TaskList(filteredList, onNoteClick = onNoteClick)
                }
            }
        }
    }
}

// 🔹 Listado de Notas
@Composable
fun NoteList(items: List<Nota>, onNoteClick: (Nota) -> Unit) { // << 10. Actualizamos el tipo de parámetro a List<Nota> >>
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { note ->
            Card(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onNoteClick(note) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        // << 11. Cambiamos 'note.title' a 'note.titulo' >>
                        text = note.titulo,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        // << 12. Cambiamos 'note.description' a 'note.contenido' >>
                        text = note.contenido,
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
fun TaskList(items: List<Nota>,onNoteClick: (Nota) -> Unit) {

    // --- 1. Obtenemos el ViewModel (lo necesitamos para el Checkbox) ---
    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application.repository)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { task ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNoteClick(task) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // --- Fila Principal ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp), // Padding ajustado
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // --- 2. Checkbox (Funcional) ---
                    Checkbox(
                        checked = task.isDone, // Lee el estado directo de la BD
                        onCheckedChange = { nuevoEstado ->
                            // Llama al ViewModel para guardar el nuevo estado
                            viewModel.actualizarNota(task.copy(isDone = nuevoEstado))
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    // --- 3. Columna de Título y Descripción (Izquierda/Centro) ---
                    Column(
                        modifier = Modifier
                            .weight(1f) // Ocupa el espacio restante
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = task.titulo,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = task.contenido,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (task.isDone)
                                MaterialTheme.colorScheme.outline
                            else
                                MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // --- 4. Columna de Fecha (Derecha) ---
                    if (task.fechaLimite != null) {
                        Column(
                            horizontalAlignment = Alignment.End, // Alinea el texto a la derecha
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Fecha Límite",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            // Usamos un formato corto (ej: 30 oct)
                            Text(
                                text = formatFecha(task.fechaLimite, "dd MMM"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
    // El preview seguirá sin funcionar bien porque depende del ViewModel
    // HomeScreen(onAddClick = {})
    // Para arreglar el preview, tendrías que pasarle una lista falsa
    // o crear un ViewModel falso, pero no te preocupes por eso ahora.
}
