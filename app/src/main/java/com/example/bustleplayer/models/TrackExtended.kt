package com.example.bustleplayer.models

import android.net.Uri

data class TrackExtended(
    val trackId: Int,
    val uri: Uri,
    val artist: String,
    val title: String,
    val durationMs: Long,
    val duration: String,
    var isPlaying: Boolean = false,
    var textColor: Int,
    var imagePlayId: Int
)
