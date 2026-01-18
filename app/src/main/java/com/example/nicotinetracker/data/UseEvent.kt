package com.example.nicotinetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "use_event")
data class UseEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usedAt: Long,
    val nextAt: Long
)