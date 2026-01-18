package com.example.nicotinetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UseDao {
    @Query("SELECT * FROM use_event ORDER BY usedAt DESC")
    fun getAllDescending(): Flow<List<UseEvent>>

    @Insert
    fun insert(event: UseEvent): Long
}