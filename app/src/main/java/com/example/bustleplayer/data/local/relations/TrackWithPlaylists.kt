package com.example.bustleplayer.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.bustleplayer.data.local.entities.PlayListInfoEntity
import com.example.bustleplayer.data.local.entities.PlayListTrackCrossRef
import com.example.bustleplayer.data.local.entities.TrackInfoEntity

data class TrackWithPlaylists(
    @Embedded val trackInfoEntity: TrackInfoEntity,
    @Relation(
        parentColumn = "trackId",
        entityColumn = "playlistId",
        associateBy = Junction(PlayListTrackCrossRef::class)
    )
    val playListInfoEntity: List<PlayListInfoEntity>

)
