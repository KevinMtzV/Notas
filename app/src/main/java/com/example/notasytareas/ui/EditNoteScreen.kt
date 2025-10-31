package com.example.notasytareas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.ui.viewmodel.EditNoteViewModel
import com.example.notasytareas.ui.viewmodel.EditNoteViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import com.example.notasytareas.R

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

    val notaCargada by viewModel.notaCargada.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isDone by remember { mutableStateOf(false) }
    var fechaLimite by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var datosCargados by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = notaCargada) { 
        if (notaCargada != null && !datosCargados) {
            title = notaCargada!!.titulo
            description = notaCargada!!.contenido
            isDone = notaCargada!!.isDone
            fechaLimite = notaCargada!!.fechaLimite
            datosCargados = true
        }
    }

    val isTask = type == "task"
    val screenTitle = if (viewModel.isNuevaNota) { 
        if (isTask) stringResource(R.string.new_task_title) else stringResource(R.string.new_note_title)
    } else {
        if (isTask) stringResource(R.string.edit_task_title) else stringResource(R.string.edit_note_title)
    }

    val onSaveClick = {
        viewModel.guardarNota(
            titulo = title,
            contenido = description,
            isTask = isTask,
            isDone = isDone,
            fechaLimite = fechaLimite
        )
        onBack()
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fechaLimite ?: System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    fechaLimite = datePickerState.selectedDateMillis
                }) {
                    Text(stringResource(R.string.accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
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
                value = title,
                onValueChange = { title = it },
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
                value = description,
                onValueChange = { description = it },
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
                        checked = isDone, 
                        onCheckedChange = { isDone = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = if (isDone) stringResource(R.string.task_completed) else stringResource(R.string.task_pending),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDone)
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

                        if (fechaLimite != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = stringResource(R.string.due_date_content_description),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = formatFecha(fechaLimite!!),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                stringResource(R.string.no_date_assigned),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(onClick = { showDatePicker = true }) {
                            Text(if (fechaLimite == null) stringResource(R.string.add_date) else stringResource(R.string.change_date))
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