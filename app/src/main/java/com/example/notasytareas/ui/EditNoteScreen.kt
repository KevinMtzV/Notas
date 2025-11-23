
package com.example.notasytareas.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.R
import com.example.notasytareas.ui.viewmodel.EditNoteUiState
import com.example.notasytareas.ui.viewmodel.EditNoteViewModel
import com.example.notasytareas.ui.viewmodel.EditNoteViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.io.File
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    type: String,
    noteId: Int,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onOpenCamera: () -> Unit,
    imageUri: String?,
    videoUri: String?
) {
    val isTask = type == "task"
    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: EditNoteViewModel = viewModel(
        key = "edit_${noteId}",
        factory = EditNoteViewModelFactory(application.repository, noteId)
    )
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentlyPlaying by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaRecorder?.release()
        }
    }


    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecording = true
        }
    }


    LaunchedEffect(isRecording) {
        if (isRecording) {
            val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.3gp")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(file.absolutePath)
                try {
                    prepare()
                    start()
                    audioFile = file
                    mediaRecorder = this
                } catch (e: Exception) {
                    // Handle exception
                }
            }
        } else {
            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                } catch (e: Exception) {
                   // Handle exception
                }
            }
            audioFile?.let {
                viewModel.addAudioUri(it.toURI().toString())
            }
            mediaRecorder = null
            audioFile = null
        }
    }

    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            viewModel.addPhotoUri(imageUri)
        }
    }

    LaunchedEffect(videoUri) {
        if (videoUri != null) {
            viewModel.addVideoUri(videoUri)
        }
    }

    val uiState by viewModel.uiState.collectAsState()


    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        uris.forEach { uri ->
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flag)
            viewModel.addPhotoUri(uri.toString())
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        uris.forEach { uri ->
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flag)
            viewModel.addVideoUri(uri.toString())
        }
    }

    val imagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        }
    }

    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            videoPickerLauncher.launch("video/*")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isTask) stringResource(R.string.edit_task) else stringResource(R.string.edit_note)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button_content_description))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.guardarNota(isTask)
                    onSave()
                },
                content = { Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save_note_content_description)) }
            )
        }
    ) { innerPadding ->
        EditNoteContent(
            modifier = Modifier.padding(innerPadding),
            isTask = isTask,
            uiState = uiState,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onAddImageClick = {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    imagePickerLauncher.launch("image/*")
                } else {
                    imagePermissionLauncher.launch(permission)
                }
            },
            onRemoveImageClick = viewModel::removePhotoUri,
            onAddVideoClick = {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_VIDEO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    videoPickerLauncher.launch("video/*")
                } else {
                    videoPermissionLauncher.launch(permission)
                }
            },
            onRemoveVideoClick = viewModel::removeVideoUri,
            isRecording = isRecording,
            onRecordAudioClick = {
                 val permission = Manifest.permission.RECORD_AUDIO
                 if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                    isRecording = !isRecording
                } else {
                    audioPermissionLauncher.launch(permission)
                }
            },
            onRemoveAudioClick = viewModel::removeAudioUri,
            onIsDoneChange = viewModel::onIsDoneChange,
            onShowDatePicker = { viewModel.onShowDatePickerChange(it) },
            onDateSelected = { viewModel.onFechaLimiteChange(it) },
            onOpenCameraClick = onOpenCamera,
            currentlyPlayingAudioUri = currentlyPlaying,
            onPlayAudioClick = { uri ->
                 if (currentlyPlaying == uri) {
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    mediaPlayer = null
                    currentlyPlaying = null
                } else {
                    mediaPlayer?.release()
                    val newPlayer = MediaPlayer().apply {
                        setDataSource(context, Uri.parse(uri))
                        prepare()
                        start()
                        setOnCompletionListener {
                            it.release()
                            currentlyPlaying = null
                        }
                    }
                    mediaPlayer = newPlayer
                    currentlyPlaying = uri
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteContent(
    modifier: Modifier = Modifier,
    isTask: Boolean,
    uiState: EditNoteUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddImageClick: () -> Unit,
    onRemoveImageClick: (String) -> Unit,
    onAddVideoClick: () -> Unit,
    onRemoveVideoClick: (String) -> Unit,
    isRecording: Boolean,
    onRecordAudioClick: () -> Unit,
    onRemoveAudioClick: (String) -> Unit,
    onIsDoneChange: (Boolean) -> Unit,
    onShowDatePicker: (Boolean) -> Unit,
    onDateSelected: (Long?) -> Unit,
    onOpenCameraClick: () -> Unit,
    currentlyPlayingAudioUri: String?,
    onPlayAudioClick: (String) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.title_label)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.description_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.photoUris.isNotEmpty()) {
            Text("Fotos", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.photoUris) { uri ->
                    Box(contentAlignment = Alignment.TopEnd) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .height(120.dp)
                                .width(120.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    val parsedUri = Uri.parse(uri)
                                    val viewUri = if (parsedUri.scheme == "file") {
                                        val file = java.io.File(parsedUri.path!!)
                                        val authority = "${context.packageName}.provider"
                                        androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                                    } else {
                                        parsedUri
                                    }

                                    val type = context.contentResolver.getType(viewUri)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(viewUri, type)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                },
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onRemoveImageClick(uri) },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = "Remove image", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.videoUris.isNotEmpty()) {
            Text("Videos", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.videoUris) { uri ->
                    Box(contentAlignment = Alignment.TopEnd) {
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .width(120.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    val parsedUri = Uri.parse(uri)
                                    val viewUri = if (parsedUri.scheme == "file") {
                                        val file = java.io.File(parsedUri.path!!)
                                        val authority = "${context.packageName}.provider"
                                        androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                                    } else {
                                        parsedUri
                                    }

                                    val type = context.contentResolver.getType(viewUri)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(viewUri, type)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video", modifier = Modifier.size(48.dp))
                        }
                        IconButton(
                            onClick = { onRemoveVideoClick(uri) },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = "Remove video", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.audioUris.isNotEmpty()) {
            Text("Audios", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.audioUris) { uri ->
                    Box(contentAlignment = Alignment.TopEnd) {
                        Box(
                            modifier = Modifier
                                .height(120.dp)
                                .width(120.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onPlayAudioClick(uri) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (currentlyPlayingAudioUri == uri) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Audio",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(
                            onClick = { onRemoveAudioClick(uri) },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = "Remove audio", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }


        Row {
            Button(onClick = onAddImageClick) {
                Text("Fotos")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onAddVideoClick) {
                Text("Videos")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onOpenCameraClick) {
                Text("Cámara")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRecordAudioClick) {
            Icon(if (isRecording) Icons.Default.Stop else Icons.Default.Mic, contentDescription = if(isRecording) "Stop recording" else "Record audio" )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isRecording) "Detener grabación" else "Grabar audio")
        }


        if (isTask) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Marcar como terminada")
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(checked = uiState.isDone, onCheckedChange = onIsDoneChange)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(uiState.fechaLimite?.let { SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(Date(it)) } ?: "Sin fecha límite")
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { onShowDatePicker(true) }) {
                    Text(if (uiState.fechaLimite == null) "Añadir fecha límite" else "Cambiar fecha")
                }
            }
        }

        if (uiState.showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.fechaLimite)
            DatePickerDialog(
                onDismissRequest = { onShowDatePicker(false) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDateSelected(datePickerState.selectedDateMillis)
                            onShowDatePicker(false)
                        }
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onShowDatePicker(false) }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
