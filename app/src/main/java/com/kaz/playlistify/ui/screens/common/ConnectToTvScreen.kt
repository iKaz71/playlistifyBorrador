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
import com.kaz.playlistify.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectToTvScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var codeInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conectar a TV") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TextField(
                value = codeInput,
                onValueChange = { if (it.length <= 4) codeInput = it },
                label = { Text("Código de 4 dígitos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (codeInput.length == 4) {
                        isLoading = true
                        errorMessage = null

                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val response = RetrofitInstance.sessionApi.verifyCode(VerifyRequest(codeInput))

                                if (response.isSuccessful) {
                                    val body = response.body()
                                    if (body != null) {
                                        Log.d("ConnectToTvScreen", "✅ Sesión encontrada: ${body.sessionId}")

                                        SessionManager.guardarSessionId(context, body.sessionId)

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
                                Log.e("ConnectToTvScreen", "❌ Error al verificar código", e)
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Código inválido o error de conexión"
                                }
                            } finally {
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                }
                            }
                        }
                    } else {
                        errorMessage = "El código debe tener 4 dígitos"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Conectar")
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
