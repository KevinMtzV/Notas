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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    onBack: () -> Unit,
    type: String     // 👈 Recibido desde AppNav.kt ("note" o "task")
) {
    // ---------- ESTADO ----------
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isDone by remember { mutableStateOf(false) }

    val isTask = type == "task"
    val screenTitle = if (isTask) "Nueva tarea" else "Nueva nota"

    // ---------- INTERFAZ ----------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // TODO: lógica de guardado
                    }) {
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
                onClick = { /* TODO guardar */ },
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
                text = if (isTask) "Detalles de la tarea" else "Detalles de la nota",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // ---------- TITULO ----------
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
                        checked = isDone,
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
                            "Recordatorio (opcional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Más adelante podrás asignar fecha y hora de vencimiento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = { /* TODO: añadir fecha */ }) {
                            Text("Agregar fecha y hora")
                        }
                    }
                }
            } else {
                // ---------- SECCIÓN PARA NOTA SIMPLE ----------
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
                            "Tipo: Nota normal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Aquí puedes escribir ideas, apuntes o información importante.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
        type = "note"
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
        type = "task"
    )
}
