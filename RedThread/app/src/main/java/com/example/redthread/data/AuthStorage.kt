package com.example.redthread.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// top-level extension, como exige DataStore (no meter dentro de clases/objetos)
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object AuthStorage {
    private val KEY_NAME  = stringPreferencesKey("reg_name")
    private val KEY_EMAIL = stringPreferencesKey("reg_email")
    private val KEY_PASS  = stringPreferencesKey("reg_pass")
    private val KEY_LOGGED = booleanPreferencesKey("logged_in")
    private val KEY_TOKEN = stringPreferencesKey("access_token")

    // guarda registro local (nombre, email, pass)
    suspend fun saveRegisteredUser(ctx: Context, name: String, email: String, pass: String) {
        val app = ctx.applicationContext
        app.dataStore.edit { prefs ->
            prefs[KEY_NAME]  = name
            prefs[KEY_EMAIL] = email
            prefs[KEY_PASS]  = pass
        }
    }

    // obtiene el registro guardado (puede devolver nulls si aun no existe)
    suspend fun getRegisteredUser(ctx: Context): Triple<String?, String?, String?> {
        val app = ctx.applicationContext
        val prefs = app.dataStore.data.first()
        return Triple(prefs[KEY_NAME], prefs[KEY_EMAIL], prefs[KEY_PASS])
    }

    // version observable (opcional): escucha cambios en tiempo real
    fun registeredUserFlow(ctx: Context): Flow<Triple<String?, String?, String?>> {
        val app = ctx.applicationContext
        return app.dataStore.data.map { p ->
            Triple(p[KEY_NAME], p[KEY_EMAIL], p[KEY_PASS])
        }
    }

    // setea flag de sesion iniciada
    suspend fun setLoggedIn(ctx: Context, value: Boolean) {
        val app = ctx.applicationContext
        app.dataStore.edit { it[KEY_LOGGED] = value }
    }

    // lee flag de sesion (bloqueante dentro de corrutina)
    suspend fun isLoggedIn(ctx: Context): Boolean {
        val app = ctx.applicationContext
        return app.dataStore.data.map { it[KEY_LOGGED] ?: false }.first()
    }

    // version observable (opcional) del estado de sesion
    fun isLoggedInFlow(ctx: Context): Flow<Boolean> {
        val app = ctx.applicationContext
        return app.dataStore.data.map { it[KEY_LOGGED] ?: false }
    }

    // cierra sesion y limpia datos sensibles
    suspend fun clearLogin(ctx: Context) {
        val app = ctx.applicationContext
        app.dataStore.edit { prefs ->
            prefs[KEY_LOGGED] = false
            prefs.remove(KEY_PASS)   // opcional: limpia pass guardada
            // si quieres “cerrar sesion” pero mantener el registro para re-login,
            // puedes dejar NAME/EMAIL. Si no, descomenta para limpiar todo
            // prefs.remove(KEY_NAME)
            // prefs.remove(KEY_EMAIL)
        }
    }
    suspend fun saveToken(ctx: Context, token: String) {
        val app = ctx.applicationContext
        app.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
    }

    suspend fun getToken(ctx: Context): String? {
        val app = ctx.applicationContext
        return app.dataStore.data.map { prefs ->
            prefs[KEY_TOKEN]
        }.first()
    }

    suspend fun clearToken(ctx: Context) {
        val app = ctx.applicationContext
        app.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
        }
    }
}
