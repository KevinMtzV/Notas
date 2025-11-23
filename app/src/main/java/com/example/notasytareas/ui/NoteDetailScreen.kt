package com.example.notasytareas.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.data.models.Nota
import com.example.notasytareas.ui.viewmodel.NoteDetailViewModel
import com.example.notasytareas.ui.viewmodel.NoteDetailViewModelFactory
import java.io.File

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
            LoadedDetailContent(nota = nota!!, viewModel = viewModel, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun LoadedDetailContent(nota: Nota, viewModel: NoteDetailViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.addPhotoUri(it.toString())
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.addVideoUri(it.toString())
        }
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        if (perms.values.all { it }) {
            // For simplicity, we just assume we're picking an image here. A more robust implementation might
            // track which button was clicked.
            imagePickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
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

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { permissionLauncher.launch(permissions) }) {
                Icon(Icons.Default.AddAPhoto, contentDescription = "Agregar Foto")
                Spacer(Modifier.width(8.dp))
                Text("Foto")
            }
            Button(onClick = { videoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }) {
                Icon(Icons.Default.VideoCall, contentDescription = "Agregar Video")
                Spacer(Modifier.width(8.dp))
                Text("Video")
            }
        }

        if (nota.photoUris.isNotEmpty()) {
            Text("Fotos", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(nota.photoUris) { uri ->
                    val parsedUri = Uri.parse(uri)
                    AsyncImage(
                        model = parsedUri,
                        contentDescription = "Imagen de la nota",
                        modifier = Modifier
                            .height(200.dp)
                            .width(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    val finalUri = if (parsedUri.scheme == "file") {
                                        FileProvider.getUriForFile(
                                            context,
                                            context.packageName + ".provider",
                                            File(parsedUri.path!!)
                                        )
                                    } else {
                                        parsedUri
                                    }
                                    setDataAndType(finalUri, context.contentResolver.getType(finalUri))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        if (nota.videoUris.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Videos", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(nota.videoUris) { uri ->
                    val parsedUri = Uri.parse(uri)
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .width(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color.Black)
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    val finalUri = if (parsedUri.scheme == "file") {
                                        FileProvider.getUriForFile(
                                            context,
                                            context.packageName + ".provider",
                                            File(parsedUri.path!!)
                                        )
                                    } else {
                                        parsedUri
                                    }
                                    setDataAndType(finalUri, context.contentResolver.getType(finalUri))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayCircleOutline,
                            contentDescription = "Reproducir video",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = nota.contenido,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}