package com.example.bustleplayer.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.bustleplayer.data.local.entities.PlayListInfoEntity
import com.example.bustleplayer.data.local.entities.PlayListTrackCrossRef
import com.example.bustleplayer.data.local.entities.TrackInfoEntity

data class PlaylistWithTracks(
    @Embedded val playListInfoEntity: PlayListInfoEntity,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "trackId",
        associateBy = Junction(PlayListTrackCrossRef::class)
    )
    val trackInfoEntity: List<TrackInfoEntity>
)
