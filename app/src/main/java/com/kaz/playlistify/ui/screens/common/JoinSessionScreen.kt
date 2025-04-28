package com.kaz.playlistify.ui.screens.common

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kaz.playlistify.api.RetrofitInstance
import com.kaz.playlistify.model.VerifyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinSessionScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unirse a una sala") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Ingresa el código de la sala", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = code,
                onValueChange = { if (it.length <= 4) code = it },
                placeholder = { Text("Ej: 1234") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            withContext(Dispatchers.Main) {
                                isLoading = true
                                errorMessage = null
                            }

                            val response = RetrofitInstance.sessionApi.verifyCode(VerifyRequest(code))

                            if (response.isSuccessful) {
                                val body = response.body()
                                if (body != null) {
                                    Log.d("JoinSession", "✅ Código válido, sessionId: ${body.sessionId}")
                                    withContext(Dispatchers.Main) {
                                        navController.navigate("sala/${body.sessionId}")
                                    }
                                } else {
                                    throw Exception("Respuesta vacía del servidor")
                                }
                            } else {
                                throw Exception("Código inválido o no encontrado. HTTP ${response.code()}")
                            }

                        } catch (e: Exception) {
                            Log.e("JoinSession", "❌ Error verificando código: ${e.message}")
                            Log.e("JoinSession", "❌ LocalizedMessage: ${e.localizedMessage}")
                            Log.e("JoinSession", "❌ Cause: ${e.cause}")
                            Log.e("JoinSession", "❌ Exception completa: $e")
                            withContext(Dispatchers.Main) {
                                errorMessage = "Código inválido o error de conexión"
                            }
                        } finally {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading && code.length == 4,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Verificar y Unirse")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val request = VerifyRequest(code)
                            val response = RetrofitInstance.sessionApi.verifyCode(request)

                            if (response.isSuccessful) {
                                val body = response.body()
                                if (body != null) {
                                    Log.d("TestConnection", "✅ API responde: ${body.sessionId}")
                                } else {
                                    throw Exception("Respuesta vacía al probar conexión")
                                }
                            } else {
                                throw Exception("Error HTTP al probar conexión: ${response.code()}")
                            }
                        } catch (e: Exception) {
                            Log.e("TestConnection", "❌ Error al conectar con API: ${e.message}")
                            Log.e("TestConnection", "❌ LocalizedMessage: ${e.localizedMessage}")
                            Log.e("TestConnection", "❌ Cause: ${e.cause}")
                            Log.e("TestConnection", "❌ Exception completa: $e")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("🧪 Probar conexión API")
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
