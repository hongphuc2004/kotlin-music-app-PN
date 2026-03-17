package com.example.musicapp.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object PreferenceHelper {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LOGGED_IN = "isLoggedIn"
    private const val KEY_DARK_MODE = "darkMode"
    private const val KEY_USERNAME = "username"
    private const val KEY_AVATAR = "avatar"

    // --- LOGIN STATE ---
    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_LOGGED_IN, false)
    }

    fun setLoggedIn(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()
    }

    // --- DARK MODE ---
    fun setDarkMode(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
    }
    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DARK_MODE, false)
    }
    fun applyTheme(context: Context) {
        val darkMode = isDarkMode(context)
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    // --- USER INFO ---
    fun setUserInfo(context: Context, username: String?, avatar: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_AVATAR, avatar)
            .apply()
    }

    fun getUsername(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getAvatar(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_AVATAR, null)
    }

    fun clearUserInfo(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_AVATAR)
            .apply()
    }

    fun saveBoolean(context: Context, key: String, value: Boolean) {
        val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        val sharedPref = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
        return sharedPref.getBoolean(key, defaultValue)
    }

}
