package com.project.beam.data.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "beam_prefs")

object TokenManager {
    private val TOKEN_KEY = stringPreferencesKey("access_token")

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
        AuthInterceptor.token = token
    }

    suspend fun getToken(context: Context): String? {
        return context.dataStore.data
            .map { it[TOKEN_KEY] }
            .firstOrNull()
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
        AuthInterceptor.token = null
    }
}