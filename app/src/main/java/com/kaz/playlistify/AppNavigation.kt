package com.kaz.playlistify.ui.theme


import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaz.playlistify.ui.HostScreen
import com.kaz.playlistify.ui.JoinScreen
import com.kaz.playlistify.ui.RoleSelectionScreen
import com.kaz.playlistify.ui.SalaScreen
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
            HostScreen(navController)
        }
        composable("joinScreen") {
            JoinScreen(navController)
        }
        composable(
            route = "sala/{codigoSala}",
            arguments = listOf(navArgument("codigoSala") { type = NavType.StringType })
        ) { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigoSala") ?: ""
            SalaScreen(codigoSala = codigo, esAnfitrion = true)
        }
    }
}