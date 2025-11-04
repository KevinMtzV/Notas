package com.example.notasytareas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun HomeScreen(onAddClick: (isTask: Boolean) -> Unit,
               onNoteClick: (Nota) -> Unit) {

    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(application.repository)
    )

    val selectedTab by viewModel.selectedTab.collectAsState()
    val showMenu by viewModel.showMenu.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredList by viewModel.listaNotasFiltradas.collectAsState(initial = emptyList())

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
                if (selectedTab == 0) {
                    NoteList(filteredList, onNoteClick = onNoteClick)
                } else {
                    TaskList(filteredList, onNoteClick = onNoteClick)
                }
            }
        }
    }
}

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

@Composable
fun TaskList(items: List<Nota>,onNoteClick: (Nota) -> Unit) {

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

fun formatFecha(date: Date, format: String): String {
    return SimpleDateFormat(format, Locale.getDefault()).format(date)
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    name = "Pantalla principal"
)
@Composable
fun PreviewHomeScreen() {

}
