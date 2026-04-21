package com.example.courierapp.data.pref

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("courier_app_prefs", Context.MODE_PRIVATE)

    fun saveRole(role: String) {
        prefs.edit().putString("role", role).apply()
    }

    fun getRole(): String {
        return prefs.getString("role", "") ?: ""
    }

    fun saveUserId(uid: String) {
        prefs.edit().putString("uid", uid).apply()
    }

    fun getUserId(): String {
        return prefs.getString("uid", "") ?: ""
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}