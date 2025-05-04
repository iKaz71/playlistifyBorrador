package com.kaz.playlistify.network.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.kaz.playlistify.model.Cancion

object FirebaseQueueManager {

    private val database = FirebaseDatabase.getInstance()

    // Ahora esta función recibe un objeto Cancion, no VideoItem
    fun agregarCancionAFirebase(sessionId: String, cancion: Cancion) {
        val queueRef = database.getReference("sessions")
            .child(sessionId)
            .child("queue")

        val nuevaCancion = mapOf(
            "id" to cancion.id,
            "titulo" to cancion.title,
            "usuario" to cancion.usuario,
            "thumbnailUrl" to cancion.thumbnailUrl,
            "duration" to cancion.duration
        )

        queueRef.push().setValue(nuevaCancion)
            .addOnSuccessListener {
                Log.d("FirebaseQueueManager", "✅ Canción agregada correctamente a Firebase")
            }
            .addOnFailureListener { error ->
                Log.e("FirebaseQueueManager", "❌ Error al agregar canción: ${error.message}")
            }
    }

    // Escuchar cambios en /sessions/{sessionId}/queue
    fun escucharCola(sessionId: String, onUpdate: (List<Cancion>) -> Unit) {
        val queueRef = database.getReference("sessions")
            .child(sessionId)
            .child("queue")

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
