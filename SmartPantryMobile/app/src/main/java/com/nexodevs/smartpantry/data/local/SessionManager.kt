package com.nexodevs.smartpantry.data.local

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "smart_pantry_session",
        Context.MODE_PRIVATE
    )

    fun saveSession(
        token: String,
        idUsuario: Int,
        nombre: String,
        correo: String,
        rol: String
    ) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, idUsuario)
            .putString(KEY_NAME, nombre)
            .putString(KEY_EMAIL, correo)
            .putString(KEY_ROLE, rol)
            .apply()
    }

    fun getToken(): String? {
        return preferences.getString(KEY_TOKEN, null)
    }

    fun getUserId(): Int {
        return preferences.getInt(KEY_USER_ID, 0)
    }

    fun getName(): String {
        return preferences.getString(KEY_NAME, "") ?: ""
    }

    fun getEmail(): String {
        return preferences.getString(KEY_EMAIL, "") ?: ""
    }

    fun getRole(): String {
        return preferences.getString(KEY_ROLE, "") ?: ""
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun clearSession() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
    }
}