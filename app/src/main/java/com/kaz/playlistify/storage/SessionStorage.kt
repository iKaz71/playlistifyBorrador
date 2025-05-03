package com.kaz.playlistify.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "playlistify_prefs")

object SessionStorage {

    private val SESSION_ID_KEY = stringPreferencesKey("session_id")

    suspend fun guardarSessionId(context: Context, sessionId: String) {
        context.dataStore.edit { prefs ->
            prefs[SESSION_ID_KEY] = sessionId
        }
    }

    suspend fun obtenerSessionId(context: Context): String? {
        return context.dataStore.data
            .map { prefs -> prefs[SESSION_ID_KEY] }
            .first()
    }

    suspend fun borrarSessionId(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(SESSION_ID_KEY)
        }
    }
}
