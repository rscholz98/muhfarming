package com.mobile.sap.data.auth

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the authenticated session (JWT + role) across app launches and
 * exposes the current token to the network layer.
 *
 * A single process-wide instance is shared via [get] so the OkHttp auth
 * interceptor and the ViewModels observe the same token without needing a DI
 * framework.
 */
class SessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        private set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_TOKEN) else putString(KEY_TOKEN, value)
            }.apply()
        }

    var role: String?
        get() = prefs.getString(KEY_ROLE, null)
        private set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_ROLE) else putString(KEY_ROLE, value)
            }.apply()
        }

    val isLoggedIn: Boolean get() = !token.isNullOrBlank()

    val isAdmin: Boolean get() = role.equals("Admin", ignoreCase = true)

    fun save(token: String, role: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_ROLE).apply()
    }

    companion object {
        private const val PREFS_NAME = "muhfarming_session"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_ROLE = "auth_role"

        @Volatile
        private var instance: SessionManager? = null

        fun get(context: Context): SessionManager =
            instance ?: synchronized(this) {
                instance ?: SessionManager(context).also { instance = it }
            }

        /**
         * Builds a fresh, un-cached instance for tests, backed by whatever
         * [SharedPreferences] the supplied [context] provides. Never touches the
         * process-wide singleton, so tests stay isolated.
         */
        @androidx.annotation.VisibleForTesting
        fun getForTest(context: Context): SessionManager = SessionManager(context)
    }
}
