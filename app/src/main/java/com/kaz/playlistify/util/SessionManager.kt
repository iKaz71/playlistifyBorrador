package com.kaz.playlistify.util

import android.content.Context
import android.util.Log
import com.kaz.playlistify.api.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

object SessionManager {

    private const val PREFS_NAME = "playlistify_prefs"
    private const val SESSION_ID_KEY = "session_id"

    fun obtenerOcrearSesion(context: Context, uid: String, onResult: (String) -> Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sessionIdGuardado = prefs.getString(SESSION_ID_KEY, null)

        if (sessionIdGuardado != null) {
            Log.d("SessionManager", "📦 Reutilizando sessionId local: $sessionIdGuardado")
            onResult(sessionIdGuardado)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.sessionApi.createSession(mapOf("uid" to uid))
                val sessionId = response.sessionId

                guardarSessionId(context, sessionId)

                Log.d("SessionManager", "🎉 Sesión creada: $sessionId")

                onResult(sessionId)
            } catch (e: HttpException) {
                Log.e("SessionManager", "❌ Error HTTP creando sesión", e)
            } catch (e: Exception) {
                Log.e("SessionManager", "❌ Error creando sesión", e)
            }
        }
    }

    fun guardarSessionId(context: Context, sessionId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(SESSION_ID_KEY, sessionId).apply()
        Log.d("SessionManager", "💾 sessionId guardado: $sessionId")
    }

    fun obtenerSessionIdGuardado(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(SESSION_ID_KEY, null)
    }

    fun limpiarSesion(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(SESSION_ID_KEY).apply()
        Log.d("SessionManager", "🧹 Sesión eliminada")
    }
}
