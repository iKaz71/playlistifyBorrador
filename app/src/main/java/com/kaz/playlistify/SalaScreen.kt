package com.kaz.playlistify.ui

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
import com.kaz.playlistify.YouTubeApi
import com.kaz.playlistify.VideoItem

import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext

import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import android.net.Uri
import androidx.mediarouter.app.MediaRouteButton
import androidx.mediarouter.media.MediaRouteSelector
import com.google.android.gms.cast.CastMediaControlIntent
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import androidx.mediarouter.app.MediaRouteChooserDialogFragment
import com.google.android.material.R as MaterialR
import com.kaz.playlistify.CustomMediaRouteChooserDialogFragment




data class Cancion(
    val id: String,
    val titulo: String,
    val usuario: String,
    val thumbnailUrl: String
)

fun mostrarCastDialog(activity: FragmentActivity) {
    val selector = MediaRouteSelector.Builder()
        .addControlCategory(
            CastMediaControlIntent.categoryForCast(
                CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
            )
        ).build()

    val dialogFragment = CustomMediaRouteChooserDialogFragment().apply {
        routeSelector = selector
    }


    dialogFragment.show(activity.supportFragmentManager, "cast-dialog")
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaScreen(codigoSala: String, esAnfitrion: Boolean = false) {
    val cancionesEnCola = remember {
        mutableStateListOf(
            Cancion("d8ekz_CSBVg", "Three Days Grace - I Hate Everything About You", "usuarioA", "https://img.youtube.com/vi/d8ekz_CSBVg/0.jpg"),
            Cancion("3YxaaGgTQYM", "Evanescence - Bring Me To Life", "usuarioB", "https://img.youtube.com/vi/3YxaaGgTQYM/0.jpg"),
            Cancion("kXYiU_JCYtU", "Linkin Park - Numb", "usuarioC", "https://img.youtube.com/vi/kXYiU_JCYtU/0.jpg"),
            Cancion("YlUKcNNmywk", "Red Hot Chili Peppers - Californication", "usuarioD", "https://img.youtube.com/vi/YlUKcNNmywk/0.jpg"),
            Cancion("gGdGFtwCNBE", "The Killers - Mr. Brightside", "usuarioE", "https://img.youtube.com/vi/gGdGFtwCNBE/0.jpg")

        )
    }

    val openSheet = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentIndex = remember { mutableIntStateOf(0) }

    fun reproducirEnCast(cancion: Cancion) {
        val castSession = CastContext.getSharedInstance(context).sessionManager.currentCastSession

        val remoteMediaClient = castSession?.remoteMediaClient

        if (remoteMediaClient != null) {
            val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
                putString(MediaMetadata.KEY_TITLE, cancion.titulo)
                addImage(com.google.android.gms.common.images.WebImage(android.net.Uri.parse(cancion.thumbnailUrl)))
            }

            val videoUrl = "https://www.youtube.com/watch?v=${cancion.id}" // Este es informativo, YouTube no permite cast directo

            val mediaInfo = MediaInfo.Builder(videoUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("video/mp4") // simulación
                .setMetadata(metadata)
                .build()

            remoteMediaClient.load(mediaInfo, true, 0)

            remoteMediaClient.registerCallback(object : com.google.android.gms.cast.framework.media.RemoteMediaClient.Callback() {
                override fun onStatusUpdated() {
                    val status = remoteMediaClient.playerState
                    if (status == com.google.android.gms.cast.MediaStatus.PLAYER_STATE_IDLE) {
                        val siguiente = currentIndex.intValue + 1
                        if (siguiente < cancionesEnCola.size) {
                            currentIndex.intValue = siguiente
                            reproducirEnCast(cancionesEnCola[siguiente])
                        }
                    }
                }
            })
        } else {
            Log.e("SalaScreen", "No hay una sesión de Cast activa")
        }
    }


    val castContext = remember { CastContext.getSharedInstance(context) }
    val castSession = castContext.sessionManager.currentCastSession

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlistify") },
                actions = {
                    IconButton(onClick = { /* TODO: Peticiones de ingreso */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                    IconButton(onClick = { openSheet.value = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { /* TODO: Perfil */ }) {
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter("https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(100.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "Rick Astley - Never Gonna Give You Up",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "https://youtube.com/watch?v=dQw4w9WgXcQ",
                        style = MaterialTheme.typography.bodySmall
                    )

                    val context = LocalContext.current
                    val activity = context as? FragmentActivity

                    if (esAnfitrion && activity != null) {
                        Button(
                            onClick = {
                                mostrarCastDialog(activity)
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Enviar a Chromecast")
                        }
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

    // BottomSheet para búsqueda
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
                            onResult = { videos ->
                                results = videos.take(3)
                            },
                            onError = { e ->
                                Log.e("SalaScreen", "Error al buscar: ${e.message}")
                            }
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
                            Button(onClick = {
                                cancionesEnCola.add(
                                    Cancion(
                                        id = video.id,
                                        titulo = video.title,
                                        usuario = "Tú",
                                        thumbnailUrl = video.thumbnailUrl
                                    )
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
