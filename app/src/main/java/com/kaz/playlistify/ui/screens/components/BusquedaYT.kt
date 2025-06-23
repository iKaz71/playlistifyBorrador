package com.kaz.playlistify.ui.screens.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.kaz.playlistify.model.Cancion
import com.kaz.playlistify.network.firebase.FirebaseQueueManager
import com.kaz.playlistify.network.youtube.YouTubeApi
import com.kaz.playlistify.util.formatDuration
import kotlinx.coroutines.launch
import androidx.compose.material3.SheetState
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import com.kaz.playlistify.util.SessionManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusquedaYT(
    sessionId: String,
    bottomSheetState: SheetState,
    onCloseSheet: () -> Unit
) {
    val context = LocalContext.current
    val nombreUsuario = remember { SessionManager.obtenerNombre(context) ?: "Invitado" }

    var query by remember { mutableStateOf(TextFieldValue()) }
    var resultados by remember { mutableStateOf(listOf<Cancion>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    // Solicito foco tras una pequeña demora al iniciar el Composable
    LaunchedEffect(Unit) {
        delay(250) // espera a que se abra el modal visualmente
        focusRequester.requestFocus()
    }

    BackHandler {
        onCloseSheet()
    }

    fun ejecutarBusqueda() {
        keyboardController?.hide()
        focusManager.clearFocus()
        scope.launch {
            if (!bottomSheetState.isVisible) {
                bottomSheetState.show()
            }
            bottomSheetState.expand()
        }

        if (query.text.isNotBlank()) {
            YouTubeApi.buscarVideos(
                query = query.text,
                onResult = { videos ->
                    resultados = videos.map {
                        Cancion(
                            id = it.id,
                            title = it.title,
                            thumbnailUrl = "https://i.ytimg.com/vi/${it.id}/hqdefault.jpg",
                            duration = formatDuration(it.duration),
                            usuario = nombreUsuario
                        )
                    }
                },
                onError = { e -> Log.e("BusquedaYT", "Error al buscar: ${e.message}") }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 480.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Buscar en YouTube",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                if (resultados.isNotEmpty()) resultados = emptyList()
            },
            placeholder = { Text("", color = Color.Gray) },
            trailingIcon = {
                IconButton(onClick = { ejecutarBusqueda() }) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color(0xFFD32F2F))
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { ejecutarBusqueda() }),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .focusRequester(focusRequester),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(color = Color.White),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD32F2F),
                unfocusedBorderColor = Color(0xFFD32F2F),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (resultados.isEmpty()) {
            Text(
                text = "Realiza una búsqueda para ver resultados.",
                color = Color.LightGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                textAlign = TextAlign.Center
            )
        }

        AnimatedVisibility(visible = resultados.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(resultados) { video ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1C1C1E))
                            .padding(12.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = rememberAsyncImagePainter(video.thumbnailUrl),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(width = 96.dp, height = 54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(video.title, maxLines = 2, color = Color.White)
                                Text(
                                    "Duración: ${video.duration}",
                                    style = TextStyle(color = Color.LightGray, fontSize = 12.sp)
                                )

                            }
                            IconButton(
                                onClick = {
                                    FirebaseQueueManager.agregarCancion(context, sessionId, video)
                                    onCloseSheet()
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color(0xFFD32F2F))
                            }

                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
