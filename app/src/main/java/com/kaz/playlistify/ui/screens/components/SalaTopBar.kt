package com.kaz.playlistify.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaTopBar(
    nombreUsuario: String,
    rol: String,
    emailUsuario: String?,
    onBuscarClick: () -> Unit,
    onMenuClick: () -> Unit,
    mostrarMenu: Boolean,
    onDismissMenu: () -> Unit,
    onCambiarNombre: () -> Unit,
    onLoginGoogle: (() -> Unit)? = null,
    onEscanearQR: (() -> Unit)? = null,
    onCerrarSesion: (() -> Unit)? = null,
    onSalirSala: () -> Unit
) {
    TopAppBar(
        title = { Text("Playlistify", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        actions = {
            IconButton(onClick = onBuscarClick) {
                Icon(Icons.Default.Search, contentDescription = "Buscar en YouTube")
            }
            Box {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                }
                DropdownMenu(
                    expanded = mostrarMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Usuario: $nombreUsuario", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("Rol: $rol", color = Color.Gray, fontSize = 14.sp)
                        emailUsuario?.let {
                            Text(it, color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Cambiar nombre") },
                        onClick = { onCambiarNombre(); onDismissMenu() }
                    )
                    if (onLoginGoogle != null) {
                        DropdownMenuItem(
                            text = { Text("Iniciar sesión con Google") },
                            onClick = { onLoginGoogle(); onDismissMenu() }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Escanear QR para ser anfitrión persistente") },
                            onClick = { onEscanearQR?.invoke(); onDismissMenu() }
                        )
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión Google") },
                            onClick = { onCerrarSesion?.invoke(); onDismissMenu() }
                        )
                    }
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Salir de sala") },
                        onClick = { onSalirSala(); onDismissMenu() }
                    )
                }
            }
        }
    )
}
