package com.client.traveller.data.provider

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferenceProvider(context: Context) {

    private val SEND_LOCATION = "SEND_LOCATION"
    private val appContext = context.applicationContext
    private val preferences: SharedPreferences

    init {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
    }

    fun getPreferenceState(preferenceKey: String): Boolean {
        return preferences.getBoolean(preferenceKey, true)
    }
}