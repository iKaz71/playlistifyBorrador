package com.kaz.playlistify.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase

@Composable
fun HostScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🌟 Pantalla del Anfitrión 🌟", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        val context = LocalContext.current
        val db = FirebaseDatabase.getInstance()

        Button(onClick = {
            val codigo = generarCodigoSala()
            crearSalaEnRealtimeDB(codigo) {
                Toast.makeText(context, "Sala $codigo creada", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("🎉 Crear Sala")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.popBackStack()
        }) {
            Text("🔙 Volver")
        }

    }
}

// Función para generar un código de sala aleatorio
fun generarCodigoSala(): String {
    return (1000..9999).random().toString()
}


fun crearSalaEnRealtimeDB(codigo: String, onSuccess: () -> Unit) {
    val sala = mapOf(
        "anfitrion" to "USER_ID",
        "estado" to "activo",
        "videos" to emptyList<Map<String, Any>>() // Lista vacía inicialmente
    )

    val db = FirebaseDatabase.getInstance()
    db.getReference("salas").child(codigo).setValue(sala)
        .addOnSuccessListener { onSuccess() }
}


