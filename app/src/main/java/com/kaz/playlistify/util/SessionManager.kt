package com.kaz.playlistify.util

import android.content.Context
import android.util.Log

object SessionManager {
    private const val PREFS_NAME = "playlistify_prefs"
    private const val SESSION_ID_KEY = "session_id"
    private const val USERNAME_KEY = "nombre_usuario"

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

    // --- NUEVO: Métodos para guardar/obtener nombre de usuario ---
    fun guardarNombre(context: Context, nombre: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(USERNAME_KEY, nombre).apply()
        Log.d("SessionManager", "💾 Nombre de usuario guardado: $nombre")
    }

    fun obtenerNombre(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USERNAME_KEY, null)
    }

    fun limpiarNombre(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(USERNAME_KEY).apply()
        Log.d("SessionManager", "🧹 Nombre de usuario eliminado")
    }
}
