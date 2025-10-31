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
// Imports para el ViewModel
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    onBack: () -> Unit,
    type: String,     //  Recibido desde AppNav.kt ("note" o "task")
    noteId: Int      // Recibido desde AppNav.kt
) {
    // --- 1. Obtenemos el ViewModel ---
    // Ahora le pasamos el noteId a la Factory
    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: EditNoteViewModel = viewModel(
        key = "detail_${noteId}",
        factory = EditNoteViewModelFactory(application.repository, noteId) //  noteId añadido
    )

    // --- 2. Observamos la nota que se carga desde el ViewModel ---
    val notaCargada by viewModel.notaCargada.collectAsState() //  NUEVO

    // ---------- ESTADO ----------
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isDone by remember { mutableStateOf(false) }
    var fechaLimite by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // --- 3. Estado para evitar recargar datos si la UI se recompone ---
    var datosCargados by remember { mutableStateOf(false) } //  NUEVO

    // --- 4. LAUNCHED EFFECT: Rellena los campos cuando la nota se carga ---
    LaunchedEffect(key1 = notaCargada) { //  NUEVO
        // Si hay una nota cargada Y aún no hemos rellenado los campos...
        if (notaCargada != null && !datosCargados) {
            title = notaCargada!!.titulo
            description = notaCargada!!.contenido
            isDone = notaCargada!!.isDone
            fechaLimite = notaCargada!!.fechaLimite
            datosCargados = true // Marcamos como cargados
        }
    }

    val isTask = type == "task"
    // --- 5. Título dinámico (MODIFICADO) ---
    // Ahora comprueba si es una nota nueva o una existente
    val screenTitle = if (viewModel.isNuevaNota) { //  MODIFICADO
        if (isTask) "Nueva tarea" else "Nueva nota"
    } else {
        if (isTask) "Editar tarea" else "Editar nota"
    }

    // --- 6. Función de guardado (se queda igual, pero ahora usa la lógica actualizada del ViewModel) ---
    val onSaveClick = {
        viewModel.guardarNota(
            titulo = title,
            contenido = description,
            isTask = isTask,
            isDone = isDone,
            fechaLimite = fechaLimite
        )
        onBack() // Navegamos hacia atrás
    }

    // --- (El diálogo del DatePicker se queda igual) ---
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
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ---------- INTERFAZ ----------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) }, //  Usa el título dinámico
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onSaveClick) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar")
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
                text = { Text("Guardar ${if (isTask) "tarea" else "nota"}") }
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

            // ---------- ENCABEZADO ----------
            Text(
                text = screenTitle, //  Usa el título dinámico
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // ---------- TITULO ----------
            // (Se rellena solo gracias al LaunchedEffect)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                placeholder = {
                    Text(
                        if (isTask)
                            "Ej. Preparar entrega del proyecto"
                        else
                            "Ej. Ideas para nuevo diseño"
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- DESCRIPCIÓN ----------
            // (Se rellena sola gracias al LaunchedEffect)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                placeholder = { Text("Agrega los detalles aquí...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )

            // ---------- CHECKBOX SOLO PARA TAREAS ----------
            if (isTask) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isDone, //  Se rellena solo
                        onCheckedChange = { isDone = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = if (isDone) "Tarea completada" else "Marcar como completada",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDone)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                // ---------- SECCIÓN DE RECORDATORIO ----------
                // (Se rellena sola gracias al LaunchedEffect)
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
                            "Fecha Límite (opcional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (fechaLimite != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DateRange,
                                    null,
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
                                "Aún no has asignado una fecha.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(onClick = { showDatePicker = true }) {
                            Text(if (fechaLimite == null) "Agregar fecha" else "Cambiar fecha")
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


// --- 7. Arreglamos los Previews (MODIFICADO) ---
// Ahora deben pasar un noteId por defecto (ej: -1 para "nota nueva")

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
        noteId = -1 //  NUEVO
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
        noteId = -1 //  NUEVO
    )
}