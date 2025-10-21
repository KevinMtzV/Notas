package com.example.notasytareas.navigation

import androidx.compose.runtime.Composable
import com.example.notasytareas.ui.EditNoteScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notasytareas.ui.HomeScreen
import com.example.notasytareas.ui.EditNoteScreen


@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        // --- Ruta HomeScreen (MODIFICADA) ---
        composable("home") {
            HomeScreen(
                onAddClick = { isTask ->
                    val type = if (isTask) "task" else "note"
                    // 1. Al crear una nueva, pasamos noteId = -1
                    navController.navigate("edit/$type?noteId=-1")
                },
                // 2. Añadimos un nuevo callback para el clic
                onNoteClick = { nota ->
                    val type = if (nota.isTask) "task" else "note"
                    // 3. Al editar, pasamos el ID real de la nota
                    navController.navigate("edit/$type?noteId=${nota.id}")
                }
            )
        }

        // --- Ruta EditNoteScreen (MODIFICADA) ---
        composable(
            // 4. Actualizamos la ruta para aceptar el ID
            route = "edit/{type}?noteId={noteId}",
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    defaultValue = "note"
                },
                // 5. Definimos el nuevo argumento noteId
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = -1 // -1 significa "nota nueva"
                }
            )
        ) { backStackEntry ->

            val type = backStackEntry.arguments?.getString("type") ?: "note"
            // 6. Leemos el noteId de la ruta
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1

            EditNoteScreen(
                type = type,
                noteId = noteId, // 7. Pasamos el ID a la pantalla
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
