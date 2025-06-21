package com.kaz.playlistify.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(
    visible: Boolean,
    nombreUsuario: String,
    rol: String,
    emailUsuario: String?,
    onDismiss: () -> Unit,
    onCambiarNombre: () -> Unit,
    onLoginGoogle: (() -> Unit)? = null,
    onEscanearQR: (() -> Unit)? = null,
    onCerrarSesion: (() -> Unit)? = null,
    onSalirSala: () -> Unit
) {
    if (!visible) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF202124),
        tonalElevation = 8.dp,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null
    ) {
        // HEADER: Usuario + botón cerrar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(nombreUsuario, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Text(rol, color = Color(0xFFAAAAAA), fontSize = 14.sp)
                emailUsuario?.let {
                    Text(it, color = Color(0xFFBBBBBB), fontSize = 13.sp)
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
            }
        }

        Divider(color = Color(0x22FFFFFF))

        MenuItem("Cambiar nombre") { onCambiarNombre(); onDismiss() }
        if (onLoginGoogle != null) {
            MenuItem("Iniciar sesión con Google") { onLoginGoogle(); onDismiss() }
        }

        if (onEscanearQR != null) {
            MenuItem("Escanear QR para ser Admin") { onEscanearQR(); onDismiss() }
        }
        if (onCerrarSesion != null) {
            MenuItem("Cerrar sesión Google") { onCerrarSesion(); onDismiss() }
        }

        Divider(color = Color(0x22FFFFFF))
        MenuItem("Salir de sala", color = Color.Red) { onSalirSala(); onDismiss() }

        Spacer(Modifier.height(12.dp))
    }
}
