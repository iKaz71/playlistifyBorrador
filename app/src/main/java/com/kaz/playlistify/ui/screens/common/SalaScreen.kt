package com.kaz.playlistify.ui.screens.common

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kaz.playlistify.network.firebase.UserRepository
import com.kaz.playlistify.util.SessionManager
import com.kaz.playlistify.util.formatDuration

import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.kaz.playlistify.ui.screens.components.BusquedaYT
import com.kaz.playlistify.ui.screens.components.QRScanner
import kotlinx.coroutines.launch
import com.kaz.playlistify.api.PlaylistifyApi
import com.kaz.playlistify.api.RetrofitInstance
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kaz.playlistify.BuildConfig
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat







@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SalaScreen(
    sessionId: String,
    onLogout: () -> Unit = {}
) {
    val cancionesEnCola = remember { mutableStateListOf<CancionEnCola>() }
    val orderedPushKeys = remember { mutableStateListOf<String>() }
    val currentVideo = remember { mutableStateOf<Cancion?>(null) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val rol = remember { mutableStateOf("Invitado") }
    val codigoSala = remember { mutableStateOf("----") }

    val userGoogle = remember { mutableStateOf<GoogleSignInAccount?>(null) }
    val context = LocalContext.current
    var nombreUsuario by remember { mutableStateOf(SessionManager.obtenerNombre(context) ?: generarNombreAleatorio()) }
    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarDialogoNombre by remember { mutableStateOf(false) }
    var confirmarCerrarSesionGoogle by remember { mutableStateOf(false) }
    var confirmarSalirSala by remember { mutableStateOf(false) }
    var mostrarEscanerQR by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // ---- Permiso cámara ----
    var solicitarPermisoCamara by remember { mutableStateOf(false) }
    var permisoCamaraConcedido by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permisoCamaraConcedido = isGranted
        if (isGranted) {
            mostrarEscanerQR = true
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara para escanear QR", Toast.LENGTH_LONG).show()
        }
        solicitarPermisoCamara = false
    }

    val nombresHawaianos = listOf(
        "Luau", "Lani", "Moana", "Hoku", "Makani", "Nalu", "Pua", "Lilo", "Koa",
        "Leilani", "Paniolo", "Honi", "Laka", "Nohea", "Kailani", "Kai",
        "Aloha", "Keanu", "Mana", "Malie"
    )

    // Google sign-in setup
    LaunchedEffect(Unit) {
        val cuenta = GoogleSignIn.getLastSignedInAccount(context)
        if (cuenta != null) {
            userGoogle.value = cuenta
            val nombreGuardado = SessionManager.obtenerNombre(context)
            val esNombreAleatorio = nombreGuardado.isNullOrBlank() ||
                    nombresHawaianos.any { nombreGuardado.startsWith(it) }
            val nuevoNombre = if (esNombreAleatorio) {
                cuenta.displayName ?: cuenta.email ?: generarNombreAleatorio()
            } else {
                nombreGuardado!!
            }
            nombreUsuario = nuevoNombre
            SessionManager.guardarNombre(context, nuevoNombre)

            val firebaseUser = FirebaseAuth.getInstance().currentUser
            firebaseUser?.uid?.let { SessionManager.guardarUid(context, it) }
        }
    }

    // Registrar usuario en la sesión cada vez que entramos
    LaunchedEffect(sessionId, nombreUsuario) {
        val uid = SessionManager.obtenerUid(context)
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: return@LaunchedEffect // Si no hay UID, no seguimos (no usar nombre)
        coroutineScope.launch {
            val res = UserRepository.registrarUsuarioEnSesion(
                sessionId = sessionId,
                uid = uid,
                nombre = nombreUsuario,
                dispositivo = "android",
                rol = "invitado",
                api = RetrofitInstance.playlistifyApi
            )
            if (res.isFailure) {
                Log.e("SalaScreen", "Registro usuario fallido: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    var mostrarConfirmacionPlayNext by remember { mutableStateOf<String?>(null) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf<String?>(null) }
    var swipeRefreshId by remember { mutableStateOf(0) }

    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            userGoogle.value = account
            val esNombreProvisional = nombreUsuario.isBlank() || nombresHawaianos.any { nombreUsuario.startsWith(it) }
            if (esNombreProvisional) {
                nombreUsuario = account.displayName ?: account.email ?: generarNombreAleatorio()
                SessionManager.guardarNombre(context, nombreUsuario)
            }

            // --- 🔥 Autenticar con Firebase y guardar UID ---
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        firebaseUser?.uid?.let { SessionManager.guardarUid(context, it) }
                        Toast.makeText(context, "¡Registro exitoso! Bienvenido/a $nombreUsuario", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error autenticando con Firebase", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("SalaScreen", "Login Google error", e)
            Toast.makeText(context, "Error en login de Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Escuchar rol con DisposableEffect (en vivo)
    DisposableEffect(sessionId) {
        val usuariosRef = FirebaseDatabase.getInstance().getReference("sessions/$sessionId/usuarios")
        val uidActual = SessionManager.obtenerUid(context)

        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val usuarios = snapshot.value as? Map<*, *>
                val usuarioActual = usuarios?.get(uidActual) as? Map<*, *>
                rol.value = (usuarioActual?.get("rol") as? String)?.replaceFirstChar { it.uppercase() } ?: "Invitado"
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("SalaScreen", "Error al leer usuarios: ${error.message}")
            }
        }

        usuariosRef.addValueEventListener(listener)
        onDispose { usuariosRef.removeEventListener(listener) }
    }

    // Sincronizar cola ordenada y playback
    LaunchedEffect(sessionId) {
        FirebaseQueueManager.escucharColaOrdenada(sessionId) { nuevasCancionesEnCola, nuevosPushKeys ->
            cancionesEnCola.clear()
            cancionesEnCola.addAll(nuevasCancionesEnCola)
            orderedPushKeys.clear()
            orderedPushKeys.addAll(nuevosPushKeys)
            swipeRefreshId++
        }
    }
    LaunchedEffect(sessionId) {
        FirebasePlaybackManager.escucharEstadoReproduccion(sessionId) { videoActual ->
            currentVideo.value = videoActual
        }
    }
    LaunchedEffect(sessionId) {
        val ref = FirebaseDatabase.getInstance().getReference("sessions")
        ref.child(sessionId).child("code").get().addOnSuccessListener {
            codigoSala.value = it.getValue(String::class.java) ?: "----"
        }
    }

    // --- Bloque para pedir permiso y mostrar escáner ---
    if (solicitarPermisoCamara) {
        val permissionCheck = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            mostrarEscanerQR = true
            solicitarPermisoCamara = false
        } else {
            LaunchedEffect(Unit) {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlistify", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showSheet = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar en YouTube")
                    }
                    Box {
                        IconButton(onClick = { mostrarMenu = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                        DropdownMenu(
                            expanded = mostrarMenu,
                            onDismissRequest = { mostrarMenu = false }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text("Usuario: $nombreUsuario", fontWeight = FontWeight.Bold)
                                Text("Rol: ${rol.value}", color = Color.Gray, fontSize = 14.sp)
                                userGoogle.value?.email?.let {
                                    Text(it, color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Cambiar nombre") },
                                onClick = { mostrarDialogoNombre = true; mostrarMenu = false }
                            )
                            if (userGoogle.value == null) {
                                DropdownMenuItem(
                                    text = { Text("Iniciar sesión con Google") },
                                    onClick = {
                                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                                            .requestEmail()
                                            .build()
                                        Log.d("GoogleClientID", BuildConfig.GOOGLE_CLIENT_ID)
                                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                        val signInIntent = googleSignInClient.signInIntent
                                        googleSignInLauncher.launch(signInIntent)
                                        mostrarMenu = false
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Escanear QR para ser anfitrión persistente") },
                                    onClick = {
                                        // Revisar permiso antes de pedirlo
                                        val tienePermiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                        if (tienePermiso) {
                                            mostrarEscanerQR = true
                                        } else {
                                            solicitarPermisoCamara = true
                                        }
                                        mostrarMenu = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Cerrar sesión Google") },
                                    onClick = {
                                        confirmarCerrarSesionGoogle = true
                                        mostrarMenu = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Salir de sala") },
                                onClick = {
                                    confirmarSalirSala = true
                                    mostrarMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val currentlyPlayingPushKey = orderedPushKeys.firstOrNull()
        val enColaFiltrada = orderedPushKeys
            .filter { it != currentlyPlayingPushKey }
            .mapNotNull { pushKey -> cancionesEnCola.find { it.pushKey == pushKey } }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Código: ${codigoSala.value}", color = Color.White)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Usuario: $nombreUsuario", color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Divider(color = Color(0x22FFFFFF))
            Spacer(modifier = Modifier.height(10.dp))

            Text("Reproduciendo ahora:", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(20.dp))
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

    if (mostrarDialogoNombre) {
        var nuevoNombre by remember { mutableStateOf(nombreUsuario) }
        AlertDialog(
            onDismissRequest = { mostrarDialogoNombre = false },
            title = { Text("Personaliza tu nombre") },
            text = {
                TextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    label = { Text("Nombre de usuario") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    nombreUsuario = nuevoNombre.trim().ifEmpty { generarNombreAleatorio() }
                    SessionManager.guardarNombre(context, nombreUsuario)
                    mostrarDialogoNombre = false
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNombre = false }) { Text("Cancelar") }
            }
        )
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

    if (confirmarCerrarSesionGoogle) {
        AlertDialog(
            onDismissRequest = { confirmarCerrarSesionGoogle = false },
            title = { Text("¿Cerrar sesión de Google?") },
            text = { Text("¿Estás seguro de que quieres cerrar tu sesión de Google?") },
            confirmButton = {
                TextButton(onClick = {
                    GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                    userGoogle.value = null
                    confirmarCerrarSesionGoogle = false
                    Toast.makeText(context, "Sesión de Google cerrada.", Toast.LENGTH_SHORT).show()
                }) { Text("Cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCerrarSesionGoogle = false }) { Text("Cancelar") }
            }
        )
    }

    if (confirmarSalirSala) {
        AlertDialog(
            onDismissRequest = { confirmarSalirSala = false },
            title = { Text("¿Salir de la sala?") },
            text = { Text("¿Estás seguro de que quieres salir de la sala actual?") },
            confirmButton = {
                TextButton(onClick = {
                    SessionManager.limpiarSesion(context)
                    onLogout()
                    confirmarSalirSala = false
                    Toast.makeText(context, "Has salido de la sala.", Toast.LENGTH_SHORT).show()
                }) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalirSala = false }) { Text("Cancelar") }
            }
        )
    }

    // Mostrar el scanner si el flag está activo
    if (mostrarEscanerQR) {
        QRScanner(
            onResult = { qrContent ->
                mostrarEscanerQR = false
                if (qrContent == sessionId) {
                    val uid = SessionManager.obtenerUid(context)
                        ?: FirebaseAuth.getInstance().currentUser?.uid
                    if (uid.isNullOrEmpty()) {
                        Toast.makeText(
                            context,
                            "Inicia sesión con Google antes de ascender a anfitrión.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@QRScanner
                    }
                    coroutineScope.launch {
                        val result = UserRepository.ascenderAAnfitrionPersistente(
                            sessionId,
                            uid,
                            RetrofitInstance.playlistifyApi
                        )
                        result.onSuccess {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(context, it.message ?: "Error desconocido", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "QR inválido", Toast.LENGTH_SHORT).show()
                }
            },
            onCancel = {
                mostrarEscanerQR = false
            }
        )
    }

}

fun generarNombreAleatorio(): String {
    val adj = listOf(
        "Luau", "Lani", "Moana", "Hoku", "Makani", "Nalu", "Pua", "Lilo", "Koa",
        "Leilani", "Paniolo", "Honi", "Laka", "Nohea", "Kailani", "Kai",
        "Aloha", "Keanu", "Mana", "Malie"
    )
    val num = (1000..9999).random()
    return "${adj.random()}$num"
}


