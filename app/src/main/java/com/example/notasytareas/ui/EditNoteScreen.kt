package com.example.notasytareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.R
import com.example.notasytareas.ui.viewmodel.EditNoteViewModel
import com.example.notasytareas.ui.viewmodel.EditNoteViewModelFactory
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    onBack: () -> Unit,
    type: String,     //  Recibido desde AppNav.kt ("note" o "task")
    noteId: Int      // Recibido desde AppNav.kt
) {
    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: EditNoteViewModel = viewModel(
        key = "detail_${noteId}",
        factory = EditNoteViewModelFactory(application.repository, noteId)
    )

    val uiState by viewModel.uiState.collectAsState()

    val isTask = type == "task"
    val screenTitle = if (viewModel.isNuevaNota) {
        if (isTask) stringResource(R.string.new_task_title) else stringResource(R.string.new_note_title)
    } else {
        if (isTask) stringResource(R.string.edit_task_title) else stringResource(R.string.edit_note_title)
    }

    val onSaveClick = {
        viewModel.guardarNota(isTask = isTask)
        onBack()
    }

    if (uiState.showDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = uiState.fechaLimite ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.onShowDatePickerChange(false) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onShowDatePickerChange(false)
                    viewModel.onFechaLimiteChange(datePickerState.selectedDateMillis)
                }) {
                    Text(stringResource(R.string.accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onShowDatePickerChange(false) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onSaveClick) {
                        Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSaveClick,
                icon = { Icon(Icons.Default.Save, null) },
                text = { Text(if (isTask) stringResource(R.string.save_task) else stringResource(R.string.save_note)) }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = screenTitle,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text(stringResource(R.string.title)) },
                placeholder = {
                    Text(
                        if (isTask)
                            stringResource(R.string.task_title_placeholder)
                        else
                            stringResource(R.string.note_title_placeholder)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text(stringResource(R.string.description)) },
                placeholder = { Text(stringResource(R.string.description_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )

            if (isTask) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = uiState.isDone,
                        onCheckedChange = { viewModel.onIsDoneChange(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = if (uiState.isDone) stringResource(R.string.task_completed) else stringResource(R.string.task_pending),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (uiState.isDone)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            stringResource(R.string.due_date_optional),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (uiState.fechaLimite != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = stringResource(R.string.due_date_content_description),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                uiState.fechaLimite?.let {
                                    Text(
                                        text = formatFecha(it, "dd/MM/yyyy"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Text(
                                stringResource(R.string.no_date_assigned),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(onClick = { viewModel.onShowDatePickerChange(true) }) {
                            Text(if (uiState.fechaLimite == null) stringResource(R.string.add_date) else stringResource(R.string.change_date))
                        }
                    }
                }
            } else {
                // ... (La sección de "Nota simple" se queda igual) ...
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Vista previa: Editar Nota"
)
@Composable
fun PreviewEditNoteScreenNote() {
    EditNoteScreen(
        onBack = {},
        type = "note",
        noteId = -1
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Vista previa: Editar Tarea"
)
@Composable
fun PreviewEditNoteScreenTask() {
    EditNoteScreen(
        onBack = {},
        type = "task",
        noteId = -1
    )
}
