package com.kaz.playlistify.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaz.playlistify.model.CancionEnCola
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ColaDeCanciones(
    canciones: List<CancionEnCola>,
    orderedPushKeys: List<String>,
    currentlyPlayingPushKey: String?,
    swipeRefreshId: Int,
    onEliminarCancion: (String) -> Unit,
    onPlayNext: (String, () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var playNextDialogPushKey by remember { mutableStateOf<String?>(null) }
    var eliminarDialogPushKey by remember { mutableStateOf<String?>(null) }

    val enColaFiltrada = orderedPushKeys
        .filter { it != currentlyPlayingPushKey }
        .mapNotNull { pushKey -> canciones.find { it.pushKey == pushKey } }

    if (enColaFiltrada.isEmpty()) {
        Text("No hay canciones en la cola todavía.", color = Color.LightGray)
    } else {
        LazyColumn(modifier = modifier) {
            itemsIndexed(
                items = enColaFiltrada,
                key = { _, cancionEnCola -> "${cancionEnCola.pushKey}-$swipeRefreshId" }
            ) { _, cancionEnCola ->
                val cancion = cancionEnCola.cancion
                val pushKey = cancionEnCola.pushKey
                val positionInOrder = orderedPushKeys
                    .filter { it != currentlyPlayingPushKey }
                    .indexOf(pushKey)
                if (positionInOrder == -1) return@itemsIndexed

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
                                    playNextDialogPushKey = pushKey
                                }
                                false
                            }
                            DismissValue.DismissedToStart -> {
                                eliminarDialogPushKey = pushKey
                                false
                            }
                            else -> false
                        }
                    }
                )

                if (playNextDialogPushKey == pushKey) {
                    ConfirmDialog(
                        visible = true,
                        title = "¿Enviar al frente?",
                        message = "¿Quieres poner esta canción como la siguiente en la cola?",
                        confirmText = "Sí",
                        onConfirm = {
                            onPlayNext(pushKey) {
                                playNextDialogPushKey = null
                            }
                        },
                        onDismiss = { playNextDialogPushKey = null }
                    )
                }

                if (eliminarDialogPushKey == pushKey) {
                    ConfirmDialog(
                        visible = true,
                        title = "Confirmar eliminación",
                        message = "¿Estás seguro de que deseas eliminar esta canción?",
                        confirmText = "Eliminar",
                        onConfirm = {
                            onEliminarCancion(pushKey)
                            eliminarDialogPushKey = null
                        },
                        onDismiss = { eliminarDialogPushKey = null }
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
                                            .clip(RoundedCornerShape(16.dp))
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
                                        .clip(RoundedCornerShape(16.dp))
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
                        QueueItem(cancion = cancion)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
