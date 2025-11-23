package com.example.notasytareas.navigation

// Imports necesarios para la lógica de Tablet
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// (El resto de tus imports)
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notasytareas.NotasApplication
import com.example.notasytareas.ui.EditNoteScreen
import com.example.notasytareas.ui.HomeScreen
import com.example.notasytareas.ui.NoteDetailScreen
import com.example.notasytareas.ui.CameraScreen
import com.example.notasytareas.ui.viewmodel.EditNoteViewModel
import com.example.notasytareas.ui.viewmodel.EditNoteViewModelFactory

@Composable
fun AppNav(widthSizeClass: WindowWidthSizeClass) {

    val isCompact = widthSizeClass == WindowWidthSizeClass.Compact

    if (isCompact) {
        PhoneNavigation()
    } else {
        TabletNavigation()
    }
}

@Composable
private fun PhoneNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onAddClick = { isTask ->
                    val type = if (isTask) "task" else "note"
                    navController.navigate("edit/$type?noteId=-1")
                },
                onNoteClick = { nota ->
                    navController.navigate("detail/${nota.id}")
                }
            )
        }

        composable(
            route = "detail/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1

            NoteDetailScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() },
                onEditClick = { type, id ->
                    navController.navigate("edit/$type?noteId=$id")
                }
            )
        }

        composable(
            route = "edit/{type}?noteId={noteId}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType; defaultValue = "note" },
                navArgument("noteId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->

            val type = backStackEntry.arguments?.getString("type") ?: "note"
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            val imageUri = backStackEntry.savedStateHandle.get<String>("image_uri")
            val videoUri = backStackEntry.savedStateHandle.get<String>("video_uri")

            backStackEntry.savedStateHandle.remove<String>("image_uri")
            backStackEntry.savedStateHandle.remove<String>("video_uri")

            EditNoteScreen(
                type = type,
                noteId = noteId,
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onOpenCamera = { navController.navigate("camera") },
                imageUri = imageUri,
                videoUri = videoUri
            )
        }

        composable("camera") {
            CameraScreen(
                onImageCaptured = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("image_uri", it.toString())
                    navController.popBackStack()
                },
                onVideoCaptured = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("video_uri", it.toString())
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun TabletNavigation() {

    var selectedNoteId by rememberSaveable { mutableStateOf<Int?>(null) }
    var noteType by rememberSaveable { mutableStateOf("note") }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var showCamera by rememberSaveable { mutableStateOf(false) }

    val application = LocalContext.current.applicationContext as NotasApplication
    val currentNoteId = selectedNoteId ?: -1
    val editNoteViewModel: EditNoteViewModel = viewModel(
        key = "edit_${currentNoteId}",
        factory = EditNoteViewModelFactory(application.repository, currentNoteId)
    )

    if (showCamera) {
        CameraScreen(
            onImageCaptured = { uri: Uri ->
                editNoteViewModel.addPhotoUri(uri.toString())
                showCamera = false
            },
            onVideoCaptured = { uri: Uri ->
                editNoteViewModel.addVideoUri(uri.toString())
                showCamera = false
            }
        )
    } else {
        Row(Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(0.4f)) {
                HomeScreen(
                    onAddClick = { isTask ->
                        noteType = if (isTask) "task" else "note"
                        selectedNoteId = -1
                        isEditing = true
                    },
                    onNoteClick = { nota ->
                        noteType = if (nota.isTask) "task" else "note"
                        selectedNoteId = nota.id
                        isEditing = false
                    }
                )
            }

            Box(modifier = Modifier.weight(0.6f)) {
                if (selectedNoteId != null) {
                    if (isEditing) {
                        EditNoteScreen(
                            type = noteType,
                            noteId = selectedNoteId!!,
                            onSave = {
                                isEditing = false
                                if (selectedNoteId == -1) {
                                    selectedNoteId = null
                                }
                            },
                            onBack = {
                                isEditing = false
                                if (selectedNoteId == -1) {
                                    selectedNoteId = null
                                }
                            },
                            onOpenCamera = { showCamera = true },
                            imageUri = null,
                            videoUri = null
                        )
                    } else {
                        NoteDetailScreen(
                            noteId = selectedNoteId!!,
                            onBack = { selectedNoteId = null },
                            onEditClick = { type, _ ->
                                noteType = type
                                isEditing = true
                            }
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Selecciona una nota para verla o crea una nueva.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
