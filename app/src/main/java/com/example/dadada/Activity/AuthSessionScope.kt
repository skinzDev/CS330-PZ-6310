package com.example.dadada.Activity

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

object AuthSessionScope {
    fun resolveUserScope(context: Context): String {
        val prefs = context.getSharedPreferences(LoginActivity.PREFS_AUTH, Context.MODE_PRIVATE)
        val storedScope = prefs.getString(LoginActivity.KEY_USER_SCOPE, null)
            ?.trim()
            .orEmpty()

        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?.trim()
            .orEmpty()
        if (uid.isNotBlank()) {
            val scope = "id_$uid"
            if (scope != storedScope) {
                prefs.edit().putString(LoginActivity.KEY_USER_SCOPE, scope).apply()
            }
            return scope
        }

        if (storedScope.isNotBlank()) {
            return storedScope
        }

        val username = prefs.getString(LoginActivity.KEY_USERNAME, null)
            ?.trim()
            .orEmpty()
        if (username.isNotBlank()) {
            val scope = "name_" + username
                .lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9_]"), "_")
                .ifBlank { "guest" }
            prefs.edit().putString(LoginActivity.KEY_USER_SCOPE, scope).apply()
            return scope
        }

        return "guest"
    }
}
