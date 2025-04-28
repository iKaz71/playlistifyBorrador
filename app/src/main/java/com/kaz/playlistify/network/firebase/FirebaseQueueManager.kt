package com.kaz.playlistify.network.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.kaz.playlistify.model.Cancion
import com.kaz.playlistify.ui.screens.components.VideoItem

object FirebaseQueueManager {

    private val database = FirebaseDatabase.getInstance()

    // Agregar canción a /queues/{sessionId}
    fun agregarCancionAFirebase(sessionId: String, video: VideoItem) {
        val queueRef = database.getReference("sessions")
            .child(sessionId)
            .child("queue")

        val nuevaCancion = mapOf(
            "id" to video.id,
            "titulo" to video.title,
            "usuario" to "Tú",
            "thumbnailUrl" to video.thumbnailUrl,
            "duration" to video.duration
        )

        queueRef.push().setValue(nuevaCancion)
            .addOnSuccessListener {
                Log.d("FirebaseQueueManager", "✅ Canción agregada correctamente a Firebase")
            }
            .addOnFailureListener { error ->
                Log.e("FirebaseQueueManager", "❌ Error al agregar canción: ${error.message}", error)
            }
    }


    // Escuchar cambios en /queues/{sessionId}
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
                    val duration = child.child("duration").getValue(String::class.java) ?: "" // ➡️ Leer duración
                    canciones.add(Cancion(id, titulo, usuario, thumbnailUrl, duration)) // ➡️ Crear Canción completa
                }
                onUpdate(canciones)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {

            }
        })
    }

}
