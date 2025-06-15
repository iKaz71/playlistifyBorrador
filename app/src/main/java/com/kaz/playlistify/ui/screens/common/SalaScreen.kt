package com.kaz.playlistify.ui.screens.common

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaz.playlistify.model.Cancion
import com.kaz.playlistify.model.CancionEnCola
import com.kaz.playlistify.network.firebase.FirebasePlaybackManager
import com.kaz.playlistify.network.firebase.FirebaseQueueManager
import com.kaz.playlistify.network.firebase.UserRepository
import com.kaz.playlistify.util.SessionManager
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
import com.kaz.playlistify.api.RetrofitInstance
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.kaz.playlistify.ui.screens.components.ColaDeCanciones
import com.kaz.playlistify.ui.screens.components.ConfirmDialog
import com.kaz.playlistify.ui.screens.components.NowPlayingCard
import com.kaz.playlistify.util.generarNombreAleatorio
import com.kaz.playlistify.ui.screens.components.SalaTopBar
import com.kaz.playlistify.util.NombreDialog
import com.kaz.playlistify.ui.screens.components.MenuBottomSheet
import com.kaz.playlistify.BuildConfig



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
    val showMenuSheet = remember { mutableStateOf(false) }
    val userGoogle = remember { mutableStateOf<GoogleSignInAccount?>(null) }
    val context = LocalContext.current
    var nombreUsuario by remember { mutableStateOf(SessionManager.obtenerNombre(context) ?: generarNombreAleatorio()) }
    var mostrarDialogoNombre by remember { mutableStateOf(false) }
    var confirmarCerrarSesionGoogle by remember { mutableStateOf(false) }
    var confirmarSalirSala by remember { mutableStateOf(false) }
    var mostrarEscanerQR by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var swipeRefreshId by remember { mutableStateOf(0) }



    // Permiso cámara
    var solicitarPermisoCamara by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
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
            FirebaseAuth.getInstance().currentUser?.uid?.let { SessionManager.guardarUid(context, it) }
        }
    }

    // Registrar usuario en la sesión cada vez que entramos
    LaunchedEffect(sessionId, nombreUsuario) {
        val uid = SessionManager.obtenerUid(context)
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: return@LaunchedEffect
        val ref = FirebaseDatabase.getInstance().getReference("sessions/$sessionId/usuarios/$uid")
        ref.get().addOnSuccessListener { snapshot ->
            val data = snapshot.value as? Map<*, *>
            val rolActual = data?.get("rol") as? String ?: "invitado"
            val rolFinal = if (rolActual == "admin") "admin" else "invitado"

            coroutineScope.launch {
                val res = UserRepository.registrarUsuarioEnSesion(
                    sessionId = sessionId,
                    uid = uid,
                    nombre = nombreUsuario,
                    dispositivo = "android",
                    rol = rolFinal,
                    api = RetrofitInstance.playlistifyApi
                )
                if (res.isFailure) {
                    Log.e("SalaScreen", "Registro usuario fallido: ${res.exceptionOrNull()?.message}")
                }
            }
        }

    }

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
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        FirebaseAuth.getInstance().currentUser?.uid?.let { SessionManager.guardarUid(context, it) }
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

    // Escuchar rol con DisposableEffect
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
            SalaTopBar(
                onBuscarClick = { showSheet = true },
                onMenuClick = { showMenuSheet.value = true }
            )
        }
    ) { padding ->
        val currentlyPlayingPushKey = orderedPushKeys.firstOrNull()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header: Código y Usuario
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
                    NowPlayingCard(
                        cancion = cancionEnCola.cancion,
                        onPlayClick = {
                            FirebasePlaybackManager.iniciarReproduccion(
                                sessionId = sessionId,
                                onSuccess = { Log.d("SalaScreen", "▶ Reproducción iniciada") },
                                onError = { Log.e("SalaScreen", "❌ Fallo al iniciar reproducción", it) }
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("En cola:", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            ColaDeCanciones(
                canciones = cancionesEnCola,
                orderedPushKeys = orderedPushKeys,
                currentlyPlayingPushKey = currentlyPlayingPushKey,
                swipeRefreshId = swipeRefreshId,
                onEliminarCancion = { pushKey ->
                    FirebaseQueueManager.eliminarCancion(sessionId, pushKey)
                },
                onPlayNext = { pushKey, onFinish ->
                    FirebaseQueueManager.playNext(
                        sessionId,
                        pushKey,
                        onSuccess = { onFinish() },
                        onError = { onFinish() }
                    )
                },
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }

    NombreDialog(
        visible = mostrarDialogoNombre,
        currentName = nombreUsuario,
        onDismiss = { mostrarDialogoNombre = false },
        onSave = { nuevoNombre ->
            nombreUsuario = nuevoNombre.ifEmpty { generarNombreAleatorio() }
            SessionManager.guardarNombre(context, nombreUsuario)
            mostrarDialogoNombre = false
        }
    )

    MenuBottomSheet(
        visible = showMenuSheet.value,
        nombreUsuario = nombreUsuario,
        rol = rol.value,
        emailUsuario = userGoogle.value?.email,
        onDismiss = { showMenuSheet.value = false },
        onCambiarNombre = { mostrarDialogoNombre = true },
        onLoginGoogle = if (userGoogle.value == null) {
            {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        } else null,
        onEscanearQR = if (userGoogle.value != null) { { mostrarEscanerQR = true } } else null,
        onCerrarSesion = if (userGoogle.value != null) { { confirmarCerrarSesionGoogle = true } } else null,
        onSalirSala = { confirmarSalirSala = true }
    )

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

    ConfirmDialog(
        visible = confirmarCerrarSesionGoogle,
        title = "¿Cerrar sesión de Google?",
        message = "¿Estás seguro de que quieres cerrar tu sesión de Google?",
        confirmText = "Cerrar sesión",
        onConfirm = {
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            userGoogle.value = null
            Toast.makeText(context, "Sesión de Google cerrada.", Toast.LENGTH_SHORT).show()
            confirmarCerrarSesionGoogle = false
        },
        onDismiss = { confirmarCerrarSesionGoogle = false }
    )

    ConfirmDialog(
        visible = confirmarSalirSala,
        title = "¿Salir de la sala?",
        message = "¿Estás seguro de que quieres salir de la sala actual?",
        confirmText = "Salir",
        onConfirm = {
            SessionManager.limpiarSesion(context)
            confirmarSalirSala = false
            onLogout()
        },
        onDismiss = { confirmarSalirSala = false }
    )

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




