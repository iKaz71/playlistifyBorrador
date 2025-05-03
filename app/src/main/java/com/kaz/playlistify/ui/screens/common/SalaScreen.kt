package com.kaz.playlistify.ui.screens.common

import android.os.Build
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.FirebaseDatabase
import com.kaz.playlistify.model.Cancion
import com.kaz.playlistify.model.UsuarioConectado
import com.kaz.playlistify.network.firebase.FirebaseQueueManager
import com.kaz.playlistify.network.firebase.FirebasePlaybackManager
import com.kaz.playlistify.network.youtube.YouTubeApi
import com.kaz.playlistify.ui.screens.components.VideoItem
import com.kaz.playlistify.util.SessionManager
import androidx.compose.material.icons.filled.ExitToApp



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaScreen(sessionId: String, onLogout: () -> Unit = {}) {
    val cancionesEnCola = remember { mutableStateListOf<Cancion>() }
    val openSheet = remember { mutableStateOf(false) }
    val currentVideo = remember { mutableStateOf<Cancion?>(null) }
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Registrar dispositivo
    LaunchedEffect(Unit) {
        val deviceName = Build.MODEL ?: "Dispositivo"
        val userId = java.util.UUID.randomUUID().toString()
        val usuario = UsuarioConectado(deviceName = deviceName, userId = userId)

        FirebaseDatabase.getInstance()
            .getReference("sessions/$sessionId/connectedUsers/$userId")
            .setValue(usuario)
            .addOnSuccessListener {
                Log.d("SalaScreen", "✅ Usuario conectado registrado")
            }
            .addOnFailureListener {
                Log.e("SalaScreen", "❌ Error al registrar usuario", it)
            }
    }

    // Escuchar cola
    LaunchedEffect(sessionId) {
        FirebaseQueueManager.escucharCola(sessionId) { canciones ->
            cancionesEnCola.clear()
            cancionesEnCola.addAll(canciones)
            currentVideo.value = cancionesEnCola.firstOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlistify") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                    IconButton(onClick = { openSheet.value = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Cuenta")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Reproduciendo ahora:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            currentVideo.value?.let { video ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberAsyncImagePainter(video.thumbnailUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(video.titulo, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text("https://youtube.com/watch?v=${video.id}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("En cola:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (cancionesEnCola.isEmpty()) {
                Text("No hay canciones en la cola todavía.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(cancionesEnCola) { cancion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(cancion.thumbnailUrl),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cancion.titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("Agregado por: ${cancion.usuario}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Divider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    FirebasePlaybackManager.iniciarReproduccion(
                        sessionId = sessionId,
                        onSuccess = { Log.d("SalaScreen", "▶ Reproducción iniciada") },
                        onError = { Log.e("SalaScreen", "❌ Fallo al iniciar reproducción", it) }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("▶ Reproducir Playlist", color = Color.White)
            }
        }
    }

    // BottomSheet para búsqueda
    if (openSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { openSheet.value = false },
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            var query by remember { mutableStateOf("") }
            var resultados by remember { mutableStateOf(listOf<VideoItem>()) }

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar en YouTube") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        YouTubeApi.buscarVideos(
                            query = query,
                            onResult = { videos -> resultados = videos },
                            onError = { e -> Log.e("SalaScreen", "Error al buscar: ${e.message}") }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Buscar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(resultados) { video ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(video.thumbnailUrl),
                                contentDescription = null,
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("Duración: ${formatDuration(video.duration)}")
                                Button(onClick = {
                                    FirebaseQueueManager.agregarCancionAFirebase(sessionId, video)
                                    openSheet.value = false
                                }) {
                                    Text("Agregar a la cola")
                                }
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("¿Estás seguro de que quieres salir de la sala actual?") },
            confirmButton = {
                TextButton(onClick = {
                    SessionManager.limpiarSesion(context)
                    onLogout()
                    showLogoutDialog = false
                }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)

                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

fun formatDuration(isoDuration: String): String {
    val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""")
    val matchResult = regex.matchEntire(isoDuration) ?: return "Desconocida"

    val hours = matchResult.groupValues[1].toIntOrNull() ?: 0
    val minutes = matchResult.groupValues[2].toIntOrNull() ?: 0
    val seconds = matchResult.groupValues[3].toIntOrNull() ?: 0

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
