package com.example.nicotinetracker.data

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Extension property to create DataStore

val Context.dataStore by preferencesDataStore(name = "nicotine_prefs")

object PrefKeys {
    val MINUTES_TO_ADD = intPreferencesKey("minutes_to_add")
}