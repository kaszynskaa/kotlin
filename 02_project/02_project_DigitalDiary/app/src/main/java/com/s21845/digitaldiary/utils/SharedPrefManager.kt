package com.s21845.digitaldiary.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefManager {

    private const val PREF_NAME = "DiaryAppPrefs"
    private const val KEY_PASSWORD = "password"

    fun setPassword(context: Context, password: String) {
        val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString(KEY_PASSWORD, password)
        editor.apply()
    }

    fun getPassword(context: Context): String? {
        val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_PASSWORD, null)
    }

    fun isPasswordSet(context: Context): Boolean {
        return getPassword(context) != null
    }
}
