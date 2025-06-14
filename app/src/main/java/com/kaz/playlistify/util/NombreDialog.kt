package com.kaz.playlistify.util

import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun NombreDialog(
    visible: Boolean,
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    if (!visible) return

    var nuevoNombre by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personaliza tu nombre") },
        text = {
            TextField(
                value = nuevoNombre,
                onValueChange = { nuevoNombre = it },
                label = { Text("Nombre de usuario") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(nuevoNombre.trim())
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
