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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.notasytareas.R
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

    // Obtenemos el ViewModel usando la Factory
    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application.repository)
    )


    //    'collectAsState' hace que la UI se redibuje sola cuando la lista cambia.
    val notas by viewModel.listaNotas.collectAsState()



    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home_screen_title), fontWeight = FontWeight.SemiBold) }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_fab_content_description))
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.width(180.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_note)) },
                        onClick = {
                            showMenu = false
                            onAddClick(false) // false = no es tarea
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_task)) },
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
            //Campo de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_content_description)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Pestañas (Notas / Tareas)
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResource(R.string.notes)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResource(R.string.tasks)) })
            }

            // << 7. Filtramos la lista 'notas' del ViewModel >>
            val filteredList = remember(searchQuery, selectedTab, notas) {
                // Usamos 'notas' (del ViewModel) en lugar de 'notes'
                notas.filter {
                    val matchesQuery =
                        // << 8. Usamos los nombres de la Entidad titulo y contenido >>
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
                        text = stringResource(R.string.no_results),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                // << 9. Pasamos la List<Nota> >>
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

// Listado de Notas
@Composable
fun NoteList(items: List<Nota>, onNoteClick: (Nota) -> Unit) {
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
                        // << Cambiamos 'note.title' a 'note.titulo' >>
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


// Listado de Tareas
@Composable
fun TaskList(items: List<Nota>,onNoteClick: (Nota) -> Unit) {

    // --- 1. Obtenemos el ViewModel  ---
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

                    // --- 2. Checkbox ---
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
                                contentDescription = stringResource(R.string.due_date_content_description),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            // Usamos un formato corto
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

}
