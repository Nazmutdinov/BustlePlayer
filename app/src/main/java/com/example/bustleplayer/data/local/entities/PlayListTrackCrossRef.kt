package com.example.bustleplayer.data.local.entities

import androidx.room.Entity

@Entity(primaryKeys = ["playlistId","trackId"])
data class PlayListTrackCrossRef(
    val playlistId: Int,
    val trackId: Int,
    val position: Int
)
