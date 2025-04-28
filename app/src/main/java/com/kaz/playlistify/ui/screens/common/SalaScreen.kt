package com.kaz.playlistify.ui.screens.common

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kaz.playlistify.model.Cancion
import com.kaz.playlistify.network.youtube.YouTubeApi
import com.kaz.playlistify.ui.screens.components.VideoItem
import com.kaz.playlistify.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaScreen(sessionId: String) {
    val cancionesEnCola = remember { mutableStateListOf<Cancion>() }
    val openSheet = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentVideo = remember { mutableStateOf<Cancion?>(null) }

    // Carga inicial simulada de canciones
    LaunchedEffect(Unit) {
        cancionesEnCola.addAll(
            listOf(
                Cancion("hTWKbfoikeg", "Nirvana - Smells Like Teen Spirit", "anfitrión", "https://img.youtube.com/vi/hTWKbfoikeg/0.jpg"),
                Cancion("Ckom3gf57Yw", "Muse - Uprising", "anfitrión", "https://img.youtube.com/vi/Ckom3gf57Yw/0.jpg"),
                Cancion("ktvTqknDobU", "Linkin Park - Burn It Down", "invitado", "https://img.youtube.com/vi/ktvTqknDobU/0.jpg")
            )
        )
        currentVideo.value = cancionesEnCola.firstOrNull()
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
                    IconButton(onClick = { }) {
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
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
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
        }
    }

    // BottomSheet para buscar canciones
    if (openSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { openSheet.value = false },
            modifier = Modifier.fillMaxHeight(0.85f)
        ) {
            var query by remember { mutableStateOf("") }
            var results by remember { mutableStateOf<List<VideoItem>>(emptyList()) }

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Buscar canciones", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Escribe el nombre del video") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        YouTubeApi.buscarVideos(
                            query = query,
                            onResult = { videos -> results = videos.take(3) },
                            onError = { e -> Log.e("SalaScreen", "Error al buscar: ${e.message}") }
                        )
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Buscar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                results.forEach { video ->
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
                                cancionesEnCola.add(
                                    Cancion(video.id, video.title, "Tú", video.thumbnailUrl)
                                )
                                openSheet.value = false
                            }) {
                                Text("Agregar a la cola")
                            }
                        }
                    }
                }
            }
        }
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

