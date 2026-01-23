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

    fun setIncrementEnabled(enabled: Boolean) {
        viewModelScope.launch {
            ds.edit { prefs ->
                prefs[PrefKeys.INCREMENT_ENABLED] = enabled
            }
        }
    }

    fun onUseClicked(incrementMinutes: Boolean = true) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val prefs = ds.data.first()
            val minutes = ds.data.first()[PrefKeys.MINUTES_TO_ADD] ?: 60
            val shouldIncrement = prefs[PrefKeys.INCREMENT_ENABLED] ?: true
            val nextAt = now + minutes * 60_000L

            // run blocking DAO call on IO dispatcher
            withContext(Dispatchers.IO) {
                dao.insert(UseEvent(usedAt = now, nextAt = nextAt))
            }

            if (shouldIncrement) {
                ds.edit { prefs ->
                    prefs[PrefKeys.MINUTES_TO_ADD] = minutes + 1
                }
            }
        }
    }
}