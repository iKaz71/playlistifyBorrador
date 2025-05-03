package com.kaz.playlistify.util

import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey


import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object UserManager {
    private val USERNAME_KEY = stringPreferencesKey("username")


    suspend fun getUsername(context: Context): String {
        val storedName = context.userDataStore.data
            .map { prefs -> prefs[USERNAME_KEY] }
            .first()

        return storedName ?: getDefaultName(context).also {
            saveUsername(context, it)
        }
    }

    suspend fun saveUsername(context: Context, username: String) {
        context.userDataStore.edit { prefs ->
            prefs[USERNAME_KEY] = username
        }
    }

    private fun getDefaultName(context: Context): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return "$manufacturer $model"
    }
}
