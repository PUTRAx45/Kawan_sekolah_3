package com.example.kawansekolah.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.kawansekolah.data.model.UserRole
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "kawan_sekolah_session_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_DEMO_MODE = "is_demo_mode"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ROLE = "user_role"
    }

    fun saveSession(userName: String, role: UserRole) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_IS_DEMO_MODE, false)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_ROLE, role.name)
            apply()
        }
    }

    fun saveDemoSession(userName: String, role: UserRole) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_IS_DEMO_MODE, true)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_ROLE, role.name)
            apply()
        }
    }

    fun isDemoMode(): Boolean {
        return prefs.getBoolean(KEY_IS_DEMO_MODE, false) || Firebase.auth.currentUser == null
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Ahmad Budi") ?: "Ahmad Budi"
    }

    fun getUserRole(): UserRole {
        val roleName = prefs.getString(KEY_USER_ROLE, UserRole.SISWA.name)
        return try {
            UserRole.valueOf(roleName ?: UserRole.SISWA.name)
        } catch (_: Exception) {
            UserRole.SISWA
        }
    }

    fun clearSession() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }
}
