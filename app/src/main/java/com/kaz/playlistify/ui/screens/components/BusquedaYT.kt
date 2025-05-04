package com.kaz.playlistify.ui.screens.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.rememberAsyncImagePainter
import com.kaz.playlistify.model.Cancion
import com.kaz.playlistify.network.firebase.FirebaseQueueManager
import com.kaz.playlistify.network.youtube.YouTubeApi
import com.kaz.playlistify.util.formatDuration

@Composable
fun BusquedaYT(openSheet: MutableState<Boolean>, sessionId: String) {
    var query by remember { mutableStateOf(TextFieldValue()) }
    var resultados by remember { mutableStateOf(listOf<Cancion>()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Cierre con botón de retroceso
    BackHandler(enabled = openSheet.value) {
        openSheet.value = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Buscar en YouTube",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Ej: Dido") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                YouTubeApi.buscarVideos(
                    query = query.text,
                    onResult = { videos ->
                        resultados = videos.map {
                            Cancion(
                                id = it.id,
                                title = it.title,
                                thumbnailUrl = it.thumbnailUrl,
                                duration = it.duration,
                                usuario = "Usuario actual"
                            )
                        }
                    },
                    onError = { e -> Log.e("BusquedaYT", "Error al buscar: ${e.message}") }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50)
        ) {
            Text("Buscar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(resultados) { video ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(video.thumbnailUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(video.title, maxLines = 2)
                            Text("Duración: ${formatDuration(video.duration)}", style = TextStyle(color = Color.Gray))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            FirebaseQueueManager.agregarCancionAFirebase(sessionId, video)
                            openSheet.value = false
                        },
                        shape = RoundedCornerShape(30.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Agregar a la cola")
                    }
                    Divider(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
