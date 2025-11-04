package com.example.notasytareas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.ui.viewmodel.NoteDetailViewModel
import com.example.notasytareas.ui.viewmodel.NoteDetailViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    onBack: () -> Unit,
    onEditClick: (type: String, id: Int) -> Unit
) {
    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: NoteDetailViewModel = viewModel(
        key = "detail_${noteId}",
        factory = NoteDetailViewModelFactory(application.repository, noteId)
    )

    val nota by viewModel.nota.collectAsState()

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
        if (nota == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
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
        Text(
            text = nota.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

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

        if (nota.isTask) {
            val estado = if (nota.isDone) "Completada" else "Pendiente"
            Text(
                text = "Estado: $estado",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = nota.contenido,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
