package com.example.notasytareas.navigation

import androidx.compose.runtime.Composable
import com.example.notasytareas.ui.EditNoteScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notasytareas.ui.HomeScreen

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // Pantalla principal
        composable("home") {
            HomeScreen(onAddClick = { isTask ->
                val type = if (isTask) "task" else "note"
                navController.navigate("edit/$type")
            })
        }

        // Pantalla de edición con parámetro tipo
        composable(
            route = "edit/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "note"
            EditNoteScreen(
                onBack = { navController.popBackStack() },
                type = type
            )
        }
    }
}
