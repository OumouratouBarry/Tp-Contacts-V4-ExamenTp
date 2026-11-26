package com.example.contacts

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

object PreferencesManager {
    fun getFontSize(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val sizeStr = prefs.getString("taille_police", "16") ?: "16"
        return sizeStr.toFloatOrNull() ?: 16f
    }

    fun getTextColor(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val colorStr = prefs.getString("couleur_preferee", "#000000") ?: "#000000"
        return try {
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            Color.BLACK
        }
    }

    fun isCallFromListEnabled(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean("appel_depuis_liste", false)
    }

    fun getSortOrder(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("ordre_tri", "nom") ?: "nom"
    }
}
