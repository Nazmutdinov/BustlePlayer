package com.example.bustleplayer.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackInfoEntity(
    @PrimaryKey(autoGenerate = true) val trackId: Int = 0,
    @ColumnInfo(name ="uri_path") val uriPath: String,
    val position: Int
)
