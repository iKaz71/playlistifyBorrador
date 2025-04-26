package com.kaz.playlistify.ui.theme

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kaz.playlistify.ui.screens.common.JoinSessionScreen
import com.kaz.playlistify.ui.screens.common.SalaScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "joinSession"
    ) {
        composable("joinSession") {
            JoinSessionScreen(navController)
        }
        composable(
            route = "sala/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SalaScreen(sessionId = sessionId)
        }
    }
}
