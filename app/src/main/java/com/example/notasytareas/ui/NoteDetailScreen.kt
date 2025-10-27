package com.example.notasytareas.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.ui.viewmodel.EditNoteViewModel
import com.example.notasytareas.ui.viewmodel.EditNoteViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    onBack: () -> Unit,
    // Devuelve el tipo y el ID para la navegación
    onEditClick: (type: String, id: Int) -> Unit
) {
    // 1. Reutilizamos el ViewModel para cargar la nota
    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: EditNoteViewModel = viewModel(
        key = "detail_${noteId}",
        factory = EditNoteViewModelFactory(application.repository, noteId)
    )

    // 2. Observamos la nota cargada
    val nota by viewModel.notaCargada.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (nota?.isTask == true) "Detalle Tarea" else "Detalle Nota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (nota != null) {
                        val type = if (nota!!.isTask) "task" else "note"
                        onEditClick(type, nota!!.id)
                    }
                }
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    ) { innerPadding ->
        // Mostramos un indicador de carga mientras la nota es nula
        if (nota == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Cuando la nota carga, mostramos los detalles (solo lectura)
            LoadedDetailContent(nota = nota!!, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun LoadedDetailContent(nota: Nota, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título
        Text(
            text = nota.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Fecha Límite (si es tarea y existe)
        if (nota.isTask && nota.fechaLimite != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Fecha Límite",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatFecha(nota.fechaLimite, "dd 'de' MMMM, yyyy"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Estado (si es tarea)
        if (nota.isTask) {
            val estado = if (nota.isDone) "Completada" else "Pendiente"
            Text(
                text = "Estado: $estado",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Divider
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Contenido
        Text(
            text = nota.contenido,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}