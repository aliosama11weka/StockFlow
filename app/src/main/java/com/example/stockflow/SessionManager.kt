package com.example.stockflow

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_EMAIL = "email"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveLoginSession(email: String) {
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUserEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun logout() {
        prefs.edit().clear().apply()
    }
}
