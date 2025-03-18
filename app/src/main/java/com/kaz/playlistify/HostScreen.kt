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
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

@Composable
fun HostScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🌟 Pantalla del Anfitrión 🌟", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("🔙 Volver")
        }
    }
}

// Función para generar un código de sala aleatorio
fun generarCodigoSala(): String {
    return (1000..9999).random().toString()
}

// Función para crear la sala en Firebase
fun crearSalaEnFirebase(db: FirebaseFirestore, codigo: String, onSuccess: () -> Unit) {
    val sala = hashMapOf(
        "anfitrion" to "USER_ID",  // Aquí puedes poner el ID real del anfitrión
        "estado" to "activo",
        "videos" to emptyList<Map<String, Any>>() // Lista vacía inicialmente
    )

    db.collection("salas").document(codigo)
        .set(sala)
        .addOnSuccessListener { onSuccess() }
}
