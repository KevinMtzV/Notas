package com.example.notasytareas.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Save
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.R
import com.example.notasytareas.ui.viewmodel.EditNoteUiState
import com.example.notasytareas.ui.viewmodel.EditNoteViewModel
import com.example.notasytareas.ui.viewmodel.EditNoteViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(type: String, noteId: Int, onSave: () -> Unit, onBack: () -> Unit) {
    val isTask = type == "task"
    val application = LocalContext.current.applicationContext as NotasApplication
    val viewModel: EditNoteViewModel = viewModel(
        key = "edit_${noteId}",
        factory = EditNoteViewModelFactory(application.repository, noteId)
    )

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        uris.forEach { uri ->
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flag)
            viewModel.addPhotoUri(uri.toString())
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
            onAddImageClick = { imagePickerLauncher.launch("image/*") },
            onRemoveImageClick = viewModel::removePhotoUri,
            onIsDoneChange = viewModel::onIsDoneChange,
            onShowDatePicker = { viewModel.onShowDatePickerChange(it) },
            onDateSelected = { viewModel.onFechaLimiteChange(it) }
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
    onIsDoneChange: (Boolean) -> Unit,
    onShowDatePicker: (Boolean) -> Unit,
    onDateSelected: (Long?) -> Unit
) {
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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.photoUris) { uri ->
                    Box(contentAlignment = Alignment.TopEnd) {
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(R.string.selected_image_content_description),
                            modifier = Modifier
                                .height(120.dp)
                                .width(120.dp)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { onRemoveImageClick(uri) },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = stringResource(R.string.remove_image_content_description), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = onAddImageClick) {
            Icon(Icons.Default.AddAPhoto, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_images_button))
        }

        if (isTask) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.mark_as_completed))
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(checked = uiState.isDone, onCheckedChange = onIsDoneChange)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(uiState.fechaLimite?.let { SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(Date(it)) } ?: stringResource(R.string.no_due_date))
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { onShowDatePicker(true) }) {
                    Text(if (uiState.fechaLimite == null) stringResource(R.string.add_due_date_button) else stringResource(R.string.change_due_date_button))
                }
            }
        }

        if (uiState.showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.fechaLimite)
            DatePickerDialog(
                onDismissRequest = { onShowDatePicker(false) },
                confirmButton = {
                    TextButton(onClick = {
                        onDateSelected(datePickerState.selectedDateMillis)
                        onShowDatePicker(false)
                    }) {
                        Text(stringResource(R.string.confirm_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onShowDatePicker(false) }) {
                        Text(stringResource(R.string.cancel_button))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
