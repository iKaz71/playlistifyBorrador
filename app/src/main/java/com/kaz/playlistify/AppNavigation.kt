package com.kaz.playlistify.ui.theme

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kaz.playlistify.ui.HostScreen
import com.kaz.playlistify.ui.JoinScreen
import com.kaz.playlistify.ui.RoleSelectionScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "roleSelection"
    ) {
        composable("roleSelection") {
            RoleSelectionScreen(navController)
        }
        composable("hostScreen") {
            HostScreen(navController) // Asegúrate de que HostScreen recibe navBack correctamente
        }
        composable("joinScreen") {
            JoinScreen(navController) // Asegúrate de que JoinScreen recibe navBack correctamente
        }
    }
}
