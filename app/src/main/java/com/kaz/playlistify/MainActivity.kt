package com.kaz.playlistify

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.*
import com.google.firebase.FirebaseApp
import com.kaz.playlistify.api.RetrofitInstance
import com.kaz.playlistify.util.SessionManager
import com.kaz.playlistify.ui.screens.WelcomeScreen
import com.kaz.playlistify.ui.screens.common.SalaScreen
import com.kaz.playlistify.ui.theme.PlaylistifyTheme
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

class MainActivity : ComponentActivity() {

    private var initialRoute = "welcome"

    private fun solicitarPermisosUbicacion() {
        val permisos = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permisos, 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        solicitarPermisosUbicacion()

        lifecycleScope.launch {
            val savedSessionId = SessionManager.obtenerSessionIdGuardado(this@MainActivity)
            if (!savedSessionId.isNullOrBlank()) {
                try {
                    val response = RetrofitInstance.sessionApi.getSession(savedSessionId)
                    if (response.isSuccessful) {
                        initialRoute = "session/$savedSessionId"
                    } else {
                        SessionManager.limpiarSesion(this@MainActivity)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            setContent {
                PlaylistifyTheme {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = initialRoute) {
                        composable("welcome") {
                            WelcomeScreen { sessionId ->
                                lifecycleScope.launch {
                                    SessionManager.guardarSessionId(this@MainActivity, sessionId)
                                }
                                navController.navigate("session/$sessionId") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        }

                        composable("session/{code}") { backStackEntry ->
                            val sessionCode = backStackEntry.arguments?.getString("code") ?: ""

                            SalaScreen(
                                sessionId = sessionCode,
                                onLogout = {
                                    SessionManager.limpiarSesion(this@MainActivity)
                                    navController.navigate("welcome") {
                                        popUpTo("session/{$sessionCode}") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
