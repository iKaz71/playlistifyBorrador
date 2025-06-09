package com.kaz.playlistify.ui.screens.common

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

import com.kaz.playlistify.model.Cancion
import com.kaz.playlistify.model.CancionEnCola
import com.kaz.playlistify.network.firebase.FirebasePlaybackManager
import com.kaz.playlistify.network.firebase.FirebaseQueueManager
import com.kaz.playlistify.ui.screens.components.BusquedaYT
import com.kaz.playlistify.util.SessionManager
import com.kaz.playlistify.util.formatDuration
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.PlayArrow
import com.google.firebase.database.FirebaseDatabase

import com.kaz.playlistify.model.PlayNextResponse

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SalaScreen(sessionId: String, onLogout: () -> Unit = {}) {
    val cancionesEnCola = remember { mutableStateListOf<CancionEnCola>() }
    val orderedPushKeys = remember { mutableStateListOf<String>() }
    val currentVideo = remember { mutableStateOf<Cancion?>(null) }
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val rol = remember { mutableStateOf("Anfitrión") }
    val codigoSala = remember { mutableStateOf("----") }

    // Estados de confirmación por canción
    var mostrarConfirmacionPlayNext by remember { mutableStateOf<String?>(null) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf<String?>(null) }

    // Nueva: Estado para forzar reinicio de swipe
    var swipeRefreshId by remember { mutableStateOf(0) }

    // Escuchamos la cola y el orden REAL de Firebase
    LaunchedEffect(sessionId) {
        FirebaseQueueManager.escucharColaOrdenada(sessionId) { nuevasCancionesEnCola, nuevosPushKeys ->
            cancionesEnCola.clear()
            cancionesEnCola.addAll(nuevasCancionesEnCola)
            orderedPushKeys.clear()
            orderedPushKeys.addAll(nuevosPushKeys)
            swipeRefreshId++ // Forzar recomposición de todos los swipe states
        }
    }
    LaunchedEffect(sessionId) {
        FirebasePlaybackManager.escucharEstadoReproduccion(sessionId) { videoActual ->
            currentVideo.value = videoActual
        }
    }
    // 🔐 Obtener código real
    LaunchedEffect(sessionId) {
        val ref = FirebaseDatabase.getInstance().getReference("sessions")
        ref.child(sessionId).child("code").get().addOnSuccessListener {
            codigoSala.value = it.getValue(String::class.java) ?: "----"
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlistify", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                    IconButton(onClick = { showSheet = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Cuenta")
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        val currentlyPlayingPushKey = orderedPushKeys.firstOrNull()
        val enColaFiltrada = orderedPushKeys
            .filter { it != currentlyPlayingPushKey }
            .mapNotNull { pushKey -> cancionesEnCola.find { it.pushKey == pushKey } }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .let { if (enColaFiltrada.size < 3) it.verticalScroll(scrollState) else it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Código: ${codigoSala.value}", color = Color.White)
                Text("Rol: ${rol.value}", color = Color.White)
            }

            Text("Reproduciendo ahora:", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            if (currentlyPlayingPushKey != null) {
                cancionesEnCola.find { it.pushKey == currentlyPlayingPushKey }?.let { cancionEnCola ->
                    val video = cancionEnCola.cancion
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFD32F2F))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = rememberAsyncImagePainter(video.thumbnailUrl),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Agregado por: ${video.usuario}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = if (video.duration.startsWith("PT")) formatDuration(video.duration) else video.duration,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                FirebasePlaybackManager.iniciarReproduccion(
                                    sessionId = sessionId,
                                    onSuccess = { Log.d("SalaScreen", "▶ Reproducción iniciada") },
                                    onError = { Log.e("SalaScreen", "❌ Fallo al iniciar reproducción", it) }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("▶ Reproducir Playlist", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("En cola:", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            if (enColaFiltrada.isEmpty()) {
                Text("No hay canciones en la cola todavía.", color = Color.LightGray)
            } else {
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    itemsIndexed(
                        items = enColaFiltrada,
                        key = { idx, cancionEnCola -> "${cancionEnCola.pushKey}-$swipeRefreshId" }
                    ) { idx, cancionEnCola ->
                        val cancion = cancionEnCola.cancion
                        val pushKey = cancionEnCola.pushKey

                        val positionInOrder = orderedPushKeys
                            .filter { it != currentlyPlayingPushKey }
                            .indexOf(pushKey)
                        if (positionInOrder == -1) return@itemsIndexed

                        // Solo bloqueamos visualmente el primero, pero todos pueden ser Play Next
                        val directions = if (positionInOrder == 0) {
                            setOf(DismissDirection.EndToStart)
                        } else {
                            setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart)
                        }

                        val dismissState = rememberDismissState(
                            confirmStateChange = { dismissValue ->
                                when (dismissValue) {
                                    DismissValue.DismissedToEnd -> {
                                        if (positionInOrder != 0) {
                                            mostrarConfirmacionPlayNext = pushKey
                                        }
                                        false
                                    }
                                    DismissValue.DismissedToStart -> {
                                        mostrarConfirmacionEliminar = pushKey
                                        false
                                    }
                                    else -> false
                                }
                            }
                        )

                        if (mostrarConfirmacionPlayNext == pushKey) {
                            AlertDialog(
                                onDismissRequest = { mostrarConfirmacionPlayNext = null },
                                title = { Text("¿Enviar al frente?") },
                                text = { Text("¿Quieres poner esta canción como la siguiente en la cola?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        FirebaseQueueManager.playNext(
                                            sessionId,
                                            pushKey,
                                            onSuccess = {
                                                mostrarConfirmacionPlayNext = null
                                            },
                                            onError = { _ ->
                                                mostrarConfirmacionPlayNext = null
                                            }
                                        )
                                    }) { Text("Sí", color = Color(0xFF1976D2)) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { mostrarConfirmacionPlayNext = null }) { Text("No") }
                                }
                            )
                        }

                        if (mostrarConfirmacionEliminar == pushKey) {
                            AlertDialog(
                                onDismissRequest = { mostrarConfirmacionEliminar = null },
                                title = { Text("Confirmar eliminación") },
                                text = { Text("¿Estás seguro de que deseas eliminar esta canción?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        mostrarConfirmacionEliminar = null
                                        FirebaseQueueManager.eliminarCancion(sessionId, pushKey)
                                    }) { Text("Eliminar", color = Color.Red) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { mostrarConfirmacionEliminar = null }) { Text("Cancelar") }
                                }
                            )
                        }

                        SwipeToDismiss(
                            state = dismissState,
                            directions = directions,
                            background = {
                                val direction = dismissState.dismissDirection
                                when (direction) {
                                    DismissDirection.StartToEnd -> {
                                        if (positionInOrder != 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFF1976D2))
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Filled.PlayArrow,
                                                        contentDescription = "Play Next",
                                                        tint = Color.White
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Text("Play Next", color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                    DismissDirection.EndToStart -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Red)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar canción",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                    else -> {}
                                }
                            },
                            dismissContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFF1C1C1E))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = rememberAsyncImagePainter(cancion.thumbnailUrl),
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    cancion.title,
                                                    color = Color.White,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    "Agregado por: ${cancion.usuario}",
                                                    color = Color.LightGray,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Text(
                                                text = if (cancion.duration.startsWith("PT")) formatDuration(cancion.duration) else cancion.duration,
                                                color = Color.LightGray,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = bottomSheetState,
            containerColor = Color.Black,
            tonalElevation = 2.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            BusquedaYT(
                sessionId = sessionId,
                bottomSheetState = bottomSheetState,
                onCloseSheet = { showSheet = false }
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
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
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
