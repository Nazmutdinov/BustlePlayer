package com.example.bustleplayer.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlayListInfoEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Int = 0,
    val title: String
)