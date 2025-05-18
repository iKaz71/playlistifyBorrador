package com.kaz.playlistify.network.firebase

import android.util.Log
import com.kaz.playlistify.api.RetrofitInstance
import com.kaz.playlistify.model.Cancion
import com.kaz.playlistify.model.CancionRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirebaseQueueManager {

    fun agregarCancionAFirebase(sessionId: String, cancion: Cancion) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = CancionRequest(
                    sessionId = sessionId,
                    id = cancion.id,
                    titulo = cancion.title,
                    usuario = cancion.usuario,
                    thumbnailUrl = cancion.thumbnailUrl,
                    duration = cancion.duration
                )

                Log.d("FirebaseQueueManager", "🔎 Enviando: $body")

                val response = RetrofitInstance.queueApi.agregarCancion(body)

                if (response.isSuccessful) {
                    Log.d("FirebaseQueueManager", "✅ Canción enviada correctamente al backend")
                } else {
                    Log.e("FirebaseQueueManager", "❌ Error HTTP: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FirebaseQueueManager", "❌ Error de red al enviar canción: ${e.message}")
            }
        }
    }

    // Escuchar cambios en /sessions/{sessionId}/queue
    fun escucharCola(sessionId: String, onUpdate: (List<Cancion>) -> Unit) {
        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
        val queueRef = database.getReference("queues")
            .child(sessionId)


        queueRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val canciones = mutableListOf<Cancion>()
                snapshot.children.forEach { child ->
                    val id = child.child("id").getValue(String::class.java) ?: ""
                    val titulo = child.child("titulo").getValue(String::class.java) ?: ""
                    val usuario = child.child("usuario").getValue(String::class.java) ?: ""
                    val thumbnailUrl = child.child("thumbnailUrl").getValue(String::class.java) ?: ""
                    val duration = child.child("duration").getValue(String::class.java) ?: ""
                    canciones.add(Cancion(id, titulo, usuario, thumbnailUrl, duration))
                }
                onUpdate(canciones)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("FirebaseQueueManager", "❌ Error al escuchar la cola: ${error.message}")
            }
        })
    }
}
