package com.example.nicotinetracker.data

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Extension property to create DataStore

val Context.dataStore by preferencesDataStore(name = "nicotine_prefs")

object PrefKeys {
    val SECONDS_TO_ADD = intPreferencesKey("seconds_to_add")
    val INCREMENT_ENABLED = booleanPreferencesKey("increment_enabled")
    val BASE_TIMER_MINUTES = intPreferencesKey("base_timer_minutes")
}