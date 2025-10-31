package com.example.notasytareas.navigation

// Imports necesarios para la lógica de Tablet
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
import androidx.compose.ui.unit.dp
// (El resto de tus imports)
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notasytareas.ui.EditNoteScreen
import com.example.notasytareas.ui.HomeScreen
import com.example.notasytareas.ui.NoteDetailScreen // 👈 1. Importa la nueva pantalla

@Composable
fun AppNav(widthSizeClass: WindowWidthSizeClass) {

    // 1. Decide qué interfaz usar
    val isCompact = widthSizeClass == WindowWidthSizeClass.Compact

    if (isCompact) {
        PhoneNavigation() //Llama a la navegación de teléfono
    } else {
        TabletNavigation() //Llama a la navegación de tablet
    }
}

/**
 * Navegación para pantallas compactas (teléfonos).
 * Flujo: HomeScreen -> NoteDetailScreen -> EditNoteScreen
 */
@Composable
private fun PhoneNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        // --- Ruta HomeScreen (Teléfono) ---
        composable("home") {
            HomeScreen(
                onAddClick = { isTask ->
                    val type = if (isTask) "task" else "note"
                    // Al añadir, vamos directo a editar
                    navController.navigate("edit/$type?noteId=-1")
                },
                onNoteClick = { nota ->
                    // CAMBIO: Al hacer clic, vamos a "detail"
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
                onBack = {
                    navController.popBackStack()
                },
                onEditClick = { type, id ->
                    // Desde "detail", navegamos a "edit"
                    navController.navigate("edit/$type?noteId=$id")
                }
            )
        }

        // --- Ruta EditNoteScreen (Teléfono) ---

        composable(
            route = "edit/{type}?noteId={noteId}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "note"
                },
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->

            val type = backStackEntry.arguments?.getString("type") ?: "note"
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1

            EditNoteScreen(
                type = type,
                noteId = noteId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}


/**
 * Interfaz para pantallas expandidas (tablets).
 * Muestra la lista y el detalle/edición lado a lado.
 */
@Composable
private fun TabletNavigation() {

    // --- 1. Estado para saber qué nota está seleccionada ---
    var selectedNoteId by rememberSaveable { mutableStateOf<Int?>(null) }
    var noteType by rememberSaveable { mutableStateOf("note") } // "note" o "task"
    // NUEVO ESTADO: ¿Estamos editando o solo viendo?
    var isEditing by rememberSaveable { mutableStateOf(false) }

    Row(Modifier.fillMaxSize()) {

        // --- 2. Panel Izquierdo (La Lista) ---
        Box(modifier = Modifier.weight(0.4f)) {
            HomeScreen(
                onAddClick = { isTask ->
                    noteType = if (isTask) "task" else "note"
                    selectedNoteId = -1
                    isEditing = true //Al añadir, vamos directo a editar
                },
                onNoteClick = { nota ->
                    noteType = if (nota.isTask) "task" else "note"
                    selectedNoteId = nota.id
                    isEditing = false // Al hacer clic, vamos a vista previa
                }
            )
        }

        // --- 3. Panel Derecho (El Detalle) ---
        Box(modifier = Modifier.weight(0.6f)) {
            if (selectedNoteId != null) {

                // LÓGICA MODIFICADA
                if (isEditing) {
                    // Si estamos editando, muestra EditNoteScreen
                    EditNoteScreen(
                        type = noteType,
                        noteId = selectedNoteId!!,
                        onBack = {
                            // "Atrás" nos devuelve a la vista previa (si no era una nota nueva)
                            isEditing = false
                            if (selectedNoteId == -1) {
                                selectedNoteId = null // Cierra el panel si era una nota nueva
                            }
                        }
                    )
                } else {
                    // Si no, muestra la Vista Previa (NoteDetailScreen)
                    NoteDetailScreen(
                        noteId = selectedNoteId!!,
                        onBack = {
                            // "Atrás" limpia la selección
                            selectedNoteId = null
                        },
                        onEditClick = { _, _ ->
                            // El botón de editar solo cambia el estado
                            isEditing = true
                        }
                    )
                }

            } else {
                // Si no hay nada seleccionado, muestra un placeholder
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