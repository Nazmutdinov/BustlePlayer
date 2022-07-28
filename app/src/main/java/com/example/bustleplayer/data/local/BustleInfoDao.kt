package com.example.bustleplayer.data.local

import androidx.room.*
import com.example.bustleplayer.data.local.entities.PlayListInfoEntity
import com.example.bustleplayer.data.local.entities.PlayListTrackCrossRef
import com.example.bustleplayer.data.local.entities.TrackInfoEntity
import com.example.bustleplayer.data.local.relations.PlaylistWithTracks

@Dao
interface BustleInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(trackInfoEntity: TrackInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlaylist(playListInfoEntity: PlayListInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlayListTrackCrossRef(playListTrackCrossRef: PlayListTrackCrossRef)

    // Для проверки есть ли id перед вставкой
    @Query("SELECT * from trackinfoentity WHERE uri_path = :uri")
    fun getTrackByUri(uri: String): TrackInfoEntity?

    @Query("SELECT * from playlistinfoentity WHERE title = :title")
    fun getPlayListByTitle(title: String): PlayListInfoEntity?

//    @Query("SELECT * from trackinfoentity WHERE uri_path = :uri")
//    fun getTrackByAuthorName(uri: String): TrackInfoEntity?

//    @Query("SELECT * from trackinfoentity")
//    fun getAllTracks(): List<TrackInfoEntity>
    @Query("SELECT * from playlistinfoentity")
    fun getAllPlaylists(): List<PlayListInfoEntity>

    // get all tracks for playlist
    @Transaction
    @Query("SELECT * from playlistinfoentity WHERE playlistId = :playlistId")
    fun getPlaylistTracks(playlistId: Int): List<PlaylistWithTracks>

    @Query("SELECT * from playlisttrackcrossref WHERE playlistId = :playlistId ORDER BY position")
    fun getOrderTracks(playlistId: Int): List<PlayListTrackCrossRef>

    // get last position track
    @Query("SELECT MAX(position) FROM playlisttrackcrossref WHERE playlistId = :playlistId")
    fun getLastTrackPosition(playlistId: Int): Int?

    @Query("UPDATE playlisttrackcrossref SET position = :position WHERE trackId = :trackId AND playlistId = :playlistId")
    fun updateTrackOrder(playlistId: Int, trackId: Int, position: Int)

    // удалить трек по id
    @Query("DELETE from trackinfoentity WHERE trackId = :trackId")
    fun deleteTrack(trackId: Int)

    @Query("DELETE from playlistinfoentity WHERE playlistId = :playlistId")
    fun deletePlaylist(playlistId: Int)

    @Query("DELETE from playlisttrackcrossref WHERE trackId = :trackId")
    fun deleteTrackCrossRef(trackId: Int)

    @Query("DELETE from playlisttrackcrossref WHERE playlistId = :playlistId")
    fun deletePlaylistCrossRef(playlistId: Int)
}