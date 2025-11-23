package com.example.notasytareas.ui

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.R
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.ui.viewmodel.HomeViewModel
import com.example.notasytareas.ui.viewmodel.HomeViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAddClick: (isTask: Boolean) -> Unit, onNoteClick: (Nota) -> Unit) {

    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application.repository)
    )

    val selectedTab by viewModel.selectedTab.collectAsState()
    val showMenu by viewModel.showMenu.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredList by viewModel.listaNotasFiltradas.collectAsState(initial = emptyList())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Nota?>(null) }

    if (showDeleteDialog && noteToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.eliminarNota(noteToDelete!!)
                showDeleteDialog = false
                noteToDelete = null
            },
            onDismiss = {
                showDeleteDialog = false
                noteToDelete = null
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.home_screen_title), fontWeight = FontWeight.SemiBold) }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { viewModel.onShowMenuChanged(!showMenu) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_fab_content_description))
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { viewModel.onShowMenuChanged(false) },
                    modifier = Modifier.width(180.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_note)) },
                        onClick = {
                            viewModel.onShowMenuChanged(false)
                            onAddClick(false) // false = no es tarea
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_task)) },
                        onClick = {
                            viewModel.onShowMenuChanged(false)
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_content_description)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { viewModel.onTabSelected(0) }, text = { Text(stringResource(R.string.notes)) })
                Tab(selected = selectedTab == 1, onClick = { viewModel.onTabSelected(1) }, text = { Text(stringResource(R.string.tasks)) })
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
                val onLongPress = { nota: Nota ->
                    noteToDelete = nota
                    showDeleteDialog = true
                }
                if (selectedTab == 0) {
                    NoteList(filteredList, onNoteClick = onNoteClick, onLongPress = onLongPress)
                } else {
                    TaskList(filteredList, onNoteClick = onNoteClick, onLongPress = onLongPress)
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_dialog_title)) },
        text = { Text(stringResource(R.string.delete_dialog_text)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteList(items: List<Nota>, onNoteClick: (Nota) -> Unit, onLongPress: (Nota) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { note ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onNoteClick(note) },
                        onLongClick = { onLongPress(note) }
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.height(100.dp)) {
                    MediaPreview(note = note)
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = note.titulo,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskList(items: List<Nota>, onNoteClick: (Nota) -> Unit, onLongPress: (Nota) -> Unit) {

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
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onNoteClick(task) },
                        onLongClick = { onLongPress(task) }
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(modifier = Modifier.height(120.dp)) {
                    MediaPreview(note = task)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = task.isDone,
                            onCheckedChange = { newState ->
                                viewModel.actualizarNota(task.copy(isDone = newState))
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
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

                        if (task.fechaLimite != null) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = stringResource(R.string.due_date_content_description),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatFecha(date = Date(task.fechaLimite), format = "dd/MM/yyyy"),
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
}

@Composable
fun MediaPreview(note: Nota) {
    if (note.photoUris.isNotEmpty()) {
        AsyncImage(
            model = Uri.parse(note.photoUris.first()),
            contentDescription = stringResource(R.string.note_image_preview),
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
            contentScale = ContentScale.Crop
        )
    } else if (note.videoUris.isNotEmpty()) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayCircleFilled,
                contentDescription = stringResource(R.string.note_video_preview),
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

fun formatFecha(date: Date, format: String): String {
    return SimpleDateFormat(format, Locale.getDefault()).format(date)
}

@Preview(showSystemUi = true, showBackground = true, name = "Pantalla principal")
@Composable
fun PreviewHomeScreen() {
    // Para que la preview funcione, necesitamos un HomeViewModel de prueba.
    // Aquí puedes crear una instancia con datos de ejemplo si es necesario.
}
