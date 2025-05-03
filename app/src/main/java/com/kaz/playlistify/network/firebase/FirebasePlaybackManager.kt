package com.kaz.playlistify.network.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

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
}
