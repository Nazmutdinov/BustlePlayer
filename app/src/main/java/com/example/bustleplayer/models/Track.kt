package com.example.bustleplayer.models

import android.net.Uri


data class Track(
    val trackId: Int,
    val uri: Uri,
    val artist: String,
    val name: String,
    val durationMs: Long,
    val duration: String,
    var isSelected: Boolean = false

)
