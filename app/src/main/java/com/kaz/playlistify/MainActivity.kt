package com.kaz.playlistify

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseApp
import com.kaz.playlistify.api.RetrofitInstance
import com.kaz.playlistify.util.SessionManager
import com.kaz.playlistify.ui.screens.WelcomeScreen
import com.kaz.playlistify.ui.screens.common.SalaScreen
import com.kaz.playlistify.ui.theme.PlaylistifyTheme
import androidx.navigation.compose.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)


        setContent {
            PlaylistifyTheme {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf("welcome") }
                var checkedSession by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    val savedSessionId = SessionManager.obtenerSessionIdGuardado(this@MainActivity)
                    if (!savedSessionId.isNullOrBlank()) {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                RetrofitInstance.sessionApi.getSession(savedSessionId)
                            }
                            if (response.isSuccessful) {
                                // Aquí  navegamos manualmente si ya estamos en welcome
                                startDestination = "session/$savedSessionId"
                                // Navegamos directo para evitar glitches
                                navController.navigate("session/$savedSessionId") {
                                    popUpTo(0)
                                }
                            } else {
                                SessionManager.limpiarSesion(this@MainActivity)
                                startDestination = "welcome"
                            }
                        } catch (e: Exception) {
                            SessionManager.limpiarSesion(this@MainActivity)
                            startDestination = "welcome"
                        }
                    } else {
                        startDestination = "welcome"
                    }
                    checkedSession = true
                }

                // Esperamos a que chequee la sesión antes de dibujar la NavHost
                if (checkedSession) {
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("welcome") {
                            WelcomeScreen { sessionId ->
                                // Guardar sesión y navegar
                                SessionManager.guardarSessionId(this@MainActivity, sessionId)
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
