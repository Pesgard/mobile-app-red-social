package com.pesgard.social_network_gera.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import com.pesgard.social_network_gera.util.Constants

/**
 * Manager para manejar la sesión del usuario usando DataStore
 * Guarda y recupera el token JWT, userId y estado de login
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME)

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey(Constants.DataStoreKeys.AUTH_TOKEN)
        private val USER_ID_KEY = stringPreferencesKey(Constants.DataStoreKeys.USER_ID) // Cambiado a String para MongoDB ObjectId
        private val USER_EMAIL_KEY = stringPreferencesKey(Constants.DataStoreKeys.USER_EMAIL)
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey(Constants.DataStoreKeys.IS_LOGGED_IN)
    }

    /**
     * Flow que emite el token JWT actual
     */
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }

    /**
     * Flow que emite el ID del usuario actual
     */
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    /**
     * Flow que emite el email del usuario actual
     */
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }

    /**
     * Flow que emite si el usuario está logueado
     */
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN_KEY] ?: false
    }

    /**
     * Guarda el token de autenticación
     */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    /**
     * Guarda el ID del usuario
     */
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    /**
     * Guarda el email del usuario
     */
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    /**
     * Obtiene el token actual de forma síncrona
     */
    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[AUTH_TOKEN_KEY] }.firstOrNull()
    }
    
    /**
     * Obtiene el token de forma bloqueante (solo para uso en interceptores)
     * WARNING: No usar en código normal, solo para interceptores de Retrofit
     */
    fun getTokenBlocking(): String? {
        return try {
            runBlocking {
                context.dataStore.data.map { it[AUTH_TOKEN_KEY] }.firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Limpia toda la sesión (logout)
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
        }
    }
}
