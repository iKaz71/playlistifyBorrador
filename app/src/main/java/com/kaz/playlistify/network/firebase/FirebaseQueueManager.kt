package com.kaz.playlistify.network.firebase

import android.util.Log
import com.google.firebase.database.*
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

                Log.d("FirebaseQueueManager", "🔎 Enviando al backend: $body")
                val response = RetrofitInstance.queueApi.agregarCancion(body)

                if (response.isSuccessful) {
                    Log.d("FirebaseQueueManager", "✅ Canción enviada correctamente al backend")

                    // 🔁 También escribir en sessions/{sessionId}/queue (opcional)
                    val db = FirebaseDatabase.getInstance()
                    val ref = db.getReference("sessions").child(sessionId).child("queue")
                    val key = ref.push().key ?: return@launch

                    val map = mapOf(
                        "id" to cancion.id,
                        "titulo" to cancion.title,
                        "usuario" to cancion.usuario,
                        "thumbnailUrl" to cancion.thumbnailUrl,
                        "duration" to cancion.duration
                    )

                    ref.child(key).setValue(map)
                        .addOnSuccessListener {
                            Log.d("FirebaseQueueManager", "✅ También escrito en sessions/{sessionId}/queue")
                        }
                        .addOnFailureListener {
                            Log.e("FirebaseQueueManager", "❌ Error al escribir en sessions/.../queue", it)
                        }

                } else {
                    Log.e("FirebaseQueueManager", "❌ Error HTTP: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FirebaseQueueManager", "❌ Error de red al enviar canción: ${e.message}")
            }
        }
    }

    fun escucharCola(sessionId: String, onUpdate: (List<Cancion>) -> Unit) {
        val queueRef = FirebaseDatabase.getInstance()
            .getReference("queues")
            .child(sessionId)

        queueRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val canciones = mutableListOf<Cancion>()
                snapshot.children.forEach { child ->
                    val id = child.child("id").getValue(String::class.java) ?: return@forEach
                    val titulo = child.child("titulo").getValue(String::class.java) ?: ""
                    val usuario = child.child("usuario").getValue(String::class.java) ?: ""
                    val thumbnailUrl = child.child("thumbnailUrl").getValue(String::class.java) ?: ""
                    val duration = child.child("duration").getValue(String::class.java) ?: ""
                    canciones.add(Cancion(id, titulo, usuario, thumbnailUrl, duration))
                }
                onUpdate(canciones)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseQueueManager", "❌ Error al escuchar la cola: ${error.message}")
            }
        })
    }

    fun escucharPlaybackState(sessionId: String, onUpdate: (Cancion?) -> Unit) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("playbackState")
            .child(sessionId)
            .child("currentVideo")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val id = snapshot.child("id").getValue(String::class.java) ?: return
                val titulo = snapshot.child("titulo").getValue(String::class.java) ?: ""
                val usuario = snapshot.child("usuario").getValue(String::class.java) ?: ""
                val thumbnailUrl = snapshot.child("thumbnailUrl").getValue(String::class.java) ?: ""
                val duration = snapshot.child("duration").getValue(String::class.java) ?: ""

                onUpdate(Cancion(id, titulo, usuario, thumbnailUrl, duration))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseQueueManager", "❌ Error en playbackState", error.toException())
            }
        })
    }

    fun eliminarCancion(sessionId: String, cancionId: String) {
        val queueRef = FirebaseDatabase.getInstance()
            .getReference("queues")
            .child(sessionId)

        queueRef.get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                val id = child.child("id").getValue(String::class.java)
                if (id == cancionId) {
                    child.ref.removeValue()
                        .addOnSuccessListener {
                            Log.d("FirebaseQueueManager", "✅ Canción eliminada correctamente")
                        }
                        .addOnFailureListener {
                            Log.e("FirebaseQueueManager", "❌ Error al eliminar canción", it)
                        }
                    break
                }
            }
        }.addOnFailureListener {
            Log.e("FirebaseQueueManager", "❌ Error al obtener la cola para eliminar", it)
        }
    }
}
