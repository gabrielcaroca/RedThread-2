package com.example.redthread.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session_prefs")

class SessionPrefs(private val context: Context) {

    companion object {
        private val KEY_LOGGED = booleanPreferencesKey("logged_in")
        private val KEY_EMAIL  = stringPreferencesKey("email")
        private val KEY_NAME   = stringPreferencesKey("name")
        private val KEY_USERID = stringPreferencesKey("user_id")
        private val KEY_ROLE   = stringPreferencesKey("user_role")

        // ⭐ CLAVE NUEVA → TOKEN PARA EL JWT
        private val KEY_TOKEN  = stringPreferencesKey("access_token")
    }

    val isLoggedInFlow: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_LOGGED] ?: false }

    val userEmailFlow: Flow<String?> =
        context.dataStore.data.map { it[KEY_EMAIL] }

    val userNameFlow: Flow<String?> =
        context.dataStore.data.map { it[KEY_NAME] }

    val userRoleFlow: Flow<String?> =
        context.dataStore.data.map { it[KEY_ROLE] }

    // ⭐ NUEVO: TOKEN FLOW
    val tokenFlow: Flow<String?> =
        context.dataStore.data.map { it[KEY_TOKEN] }

    suspend fun setSession(
        logged: Boolean,
        email: String?,
        name: String?,
        userId: String?,
        role: String?,
        token: String?        // ⭐ AGREGADO
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOGGED] = logged

            if (email != null) prefs[KEY_EMAIL] = email else prefs.remove(KEY_EMAIL)
            if (name != null) prefs[KEY_NAME] = name else prefs.remove(KEY_NAME)
            if (userId != null) prefs[KEY_USERID] = userId else prefs.remove(KEY_USERID)
            if (role != null) prefs[KEY_ROLE] = role else prefs.remove(KEY_ROLE)

            // ⭐ Guardar token
            if (token != null) prefs[KEY_TOKEN] = token else prefs.remove(KEY_TOKEN)
        }
    }

    suspend fun clearSession() =
        setSession(false, null, null, null, null, null)
}
