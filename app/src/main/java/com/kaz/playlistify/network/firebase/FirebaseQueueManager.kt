package com.kaz.playlistify.network.firebase

import android.util.Log
import com.google.firebase.database.*
import com.kaz.playlistify.api.RetrofitInstance
import com.kaz.playlistify.model.Cancion
import com.kaz.playlistify.model.CancionEnCola
import com.kaz.playlistify.model.CancionRequest
import com.kaz.playlistify.model.PlayNextRequest
import com.kaz.playlistify.model.PlayNextResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirebaseQueueManager {

    fun escucharColaOrdenada(sessionId: String, onUpdate: (List<CancionEnCola>) -> Unit) {
        val db = FirebaseDatabase.getInstance()
        val queueRef = db.getReference("queues").child(sessionId)
        val orderRef = db.getReference("queuesOrder").child(sessionId)

        orderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(orderSnap: DataSnapshot) {
                val value = orderSnap.value
                val orderList: List<String> = when (value) {
                    is List<*> -> value.filterIsInstance<String>()
                    is Map<*, *> -> value.entries.sortedBy { (it.key as String).toIntOrNull() ?: 0 }
                        .mapNotNull { it.value as? String }
                    else -> emptyList()
                }

                queueRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(queueSnap: DataSnapshot) {
                        val allCanciones = mutableMapOf<String, CancionEnCola>()
                        queueSnap.children.forEach { child ->
                            val id = child.child("id").getValue(String::class.java) ?: return@forEach
                            val titulo = child.child("titulo").getValue(String::class.java) ?: ""
                            val usuario = child.child("usuario").getValue(String::class.java) ?: ""
                            val thumbnailUrl = child.child("thumbnailUrl").getValue(String::class.java) ?: ""
                            val duration = child.child("duration").getValue(String::class.java) ?: ""
                            val pushKey = child.key ?: return@forEach
                            allCanciones[pushKey] = CancionEnCola(pushKey, Cancion(id, titulo, usuario, thumbnailUrl, duration))
                        }
                        val ordered = orderList.mapNotNull { allCanciones[it] }
                        onUpdate(ordered)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseQueueManager", "Error leyendo queue: ${error.message}")
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseQueueManager", "Error leyendo order: ${error.message}")
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

    fun playNext(
        sessionId: String,
        pushKey: String,
        onSuccess: (PlayNextResponse) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = PlayNextRequest(sessionId, pushKey)
                val response = RetrofitInstance.queueApi.playNext(request)
                if (response.isSuccessful && response.body() != null) {
                    onSuccess(response.body()!!)
                } else {
                    onError(Exception("HTTP ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun agregarCancion(sessionId: String, cancion: Cancion) {
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
                val response = RetrofitInstance.queueApi.agregarCancion(body)
                if (response.isSuccessful) {
                    Log.d("FirebaseQueueManager", "✅ Canción agregada correctamente vía backend")
                } else {
                    Log.e("FirebaseQueueManager", "❌ Error HTTP: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("FirebaseQueueManager", "❌ Error al agregar canción: ${e.message}")
            }
        }
    }

    fun eliminarCancion(sessionId: String, pushKey: String) {
        val queueRef = FirebaseDatabase.getInstance()
            .getReference("queues")
            .child(sessionId)
            .child(pushKey)

        queueRef.removeValue()
            .addOnSuccessListener {
                Log.d("FirebaseQueueManager", "✅ Canción eliminada correctamente")
            }
            .addOnFailureListener {
                Log.e("FirebaseQueueManager", "❌ Error al eliminar canción", it)
            }
    }
}
