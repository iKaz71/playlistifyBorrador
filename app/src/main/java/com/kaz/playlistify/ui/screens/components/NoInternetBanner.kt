package com.kaz.playlistify.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NoInternetBanner() {
    Surface(
        color = Color(0xFFD32F2F),
        contentColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp)
            .height(44.dp),
        shadowElevation = 8.dp,
        tonalElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("Sin conexión a Internet", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MobileDataBanner(onSwitchWifi: (() -> Unit)? = null) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("¿Usar datos móviles?") },
            text = { Text("Estás usando datos móviles. ¿Quieres cambiar a WiFi para evitar consumo?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    onSwitchWifi?.invoke()
                }) { Text("Ir a WiFi") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Seguir con datos") }
            }
        )
    }
}
