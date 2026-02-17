package com.example.nicotinetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nicotinetracker.data.AppDatabase
import com.example.nicotinetracker.data.PrefKeys
import com.example.nicotinetracker.data.UseEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import com.example.nicotinetracker.data.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).useDao()
    private val ds = application.dataStore

    // Flow of persisted use events
    val useEvents: Flow<List<UseEvent>> = dao.getAllDescending()

    val incrementEnabled: Flow<Boolean> = ds.data.map { prefs ->
        prefs[PrefKeys.INCREMENT_ENABLED] ?: true
    }

    val baseTimerMinutes: Flow<Int> = ds.data.map { prefs ->
        prefs[PrefKeys.BASE_TIMER_MINUTES] ?: 60
    }

    fun setIncrementEnabled(enabled: Boolean) {
        viewModelScope.launch {
            ds.edit { prefs ->
                prefs[PrefKeys.INCREMENT_ENABLED] = enabled
            }
        }
    }

    fun setBaseTimerMinutes(minutes: Int) {
        viewModelScope.launch {
            ds.edit { prefs ->
                prefs[PrefKeys.BASE_TIMER_MINUTES] = minutes
                // Also reset SECONDS_TO_ADD to the new base value (converted to seconds)
                prefs[PrefKeys.SECONDS_TO_ADD] = minutes * 60
            }
        }
    }

    fun onUseClicked(incrementMinutes: Boolean = true) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val prefs = ds.data.first()
            // Get seconds, defaulting to 60 minutes (3600 seconds) if not set
            val seconds = ds.data.first()[PrefKeys.SECONDS_TO_ADD] ?: 3600
            val shouldIncrement = prefs[PrefKeys.INCREMENT_ENABLED] ?: true
            val nextAt = now + seconds * 1000L

            // run blocking DAO call on IO dispatcher
            withContext(Dispatchers.IO) {
                dao.insert(UseEvent(usedAt = now, nextAt = nextAt))
            }

            if (shouldIncrement) {
                ds.edit { prefs ->
                    // Increment by 20 seconds instead of 1 minute
                    prefs[PrefKeys.SECONDS_TO_ADD] = seconds + 20
                }
            }
        }
    }
}