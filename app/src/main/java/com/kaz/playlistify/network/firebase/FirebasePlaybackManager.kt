package com.kaz.playlistify.network.firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.kaz.playlistify.model.Cancion
import com.google.firebase.database.ValueEventListener


object FirebasePlaybackManager {

    fun iniciarReproduccion(sessionId: String, onSuccess: () -> Unit = {}, onError: (Exception) -> Unit = {}) {
        val ref = FirebaseDatabase.getInstance().getReference("playbackState/$sessionId/playing")
        ref.setValue(true)
            .addOnSuccessListener {
                Log.d("PlaybackManager", "✅ Playback activado")
                onSuccess()
            }
            .addOnFailureListener {
                Log.e("PlaybackManager", "❌ Error al activar reproducción", it)
                onError(it)
            }
    }
    fun escucharEstadoReproduccion(sessionId: String, onUpdate: (Cancion?) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("playbackState").child(sessionId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playing = snapshot.child("playing").getValue(Boolean::class.java) ?: false
                val videoSnapshot = snapshot.child("currentVideo")

                if (!videoSnapshot.exists() || !playing) {
                    onUpdate(null)
                    return
                }

                val id = videoSnapshot.child("id").getValue(String::class.java) ?: return
                val titulo = videoSnapshot.child("titulo").getValue(String::class.java) ?: ""
                val usuario = videoSnapshot.child("usuario").getValue(String::class.java) ?: ""
                val thumbnailUrl = videoSnapshot.child("thumbnailUrl").getValue(String::class.java) ?: ""
                val duration = videoSnapshot.child("duration").getValue(String::class.java) ?: ""

                onUpdate(Cancion(id, titulo, usuario, thumbnailUrl, duration))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlaybackManager", "❌ Error al escuchar playbackState", error.toException())
            }
        })
    }

}
