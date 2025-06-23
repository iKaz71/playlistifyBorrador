package com.kaz.playlistify


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue

import androidx.compose.ui.platform.LocalContext
import com.kaz.playlistify.ui.screens.components.MobileDataBanner
import com.kaz.playlistify.ui.screens.components.NoInternetBanner
import com.kaz.playlistify.util.ConnectivityObserver
import com.kaz.playlistify.util.NetworkStatus
import com.kaz.playlistify.util.NetworkType


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

                // --- CONECTIVIDAD ---
                val context = LocalContext.current
                val connectivityObserver = remember { ConnectivityObserver(context) }
                val networkStatus by connectivityObserver.networkStatus.collectAsState()
                val networkType by connectivityObserver.networkType.collectAsState()

                LaunchedEffect(Unit) {
                    val savedSessionId = SessionManager.obtenerSessionIdGuardado(this@MainActivity)
                    if (!savedSessionId.isNullOrBlank()) {
                        try {
                            val response = withContext(Dispatchers.IO) {
                                RetrofitInstance.sessionApi.getSession(savedSessionId)
                            }
                            if (response.isSuccessful) {
                                startDestination = "session/$savedSessionId"
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

                if (checkedSession) {
                    Box {
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("welcome") {
                                WelcomeScreen { sessionId ->
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
                                    isOnline = (networkStatus == NetworkStatus.Available),
                                    onLogout = {
                                        SessionManager.limpiarSesion(this@MainActivity)
                                        navController.navigate("welcome") {
                                            popUpTo("session/{$sessionCode}") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }

                        // --- TOPSHEET conexión ---
                        // 1. Forzar modo de prueba para simular red móvil
                        val fakeMobileMode = false // PONLO EN false cuando termines de probar

                        if (networkStatus != NetworkStatus.Available) {
                            NoInternetBanner()
                        }
                        // Simulamos banner de datos moviles si fakeMobileMode == true
                        else if (fakeMobileMode || networkType == NetworkType.Mobile) {
                            MobileDataBanner()
                        }
                    }

                }
            }
        }
    }
}
