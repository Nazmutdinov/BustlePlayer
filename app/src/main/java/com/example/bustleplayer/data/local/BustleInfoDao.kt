package com.example.bustleplayer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bustleplayer.data.local.entities.TrackInfoEntity

@Dao
interface BustleInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrack(trackInfoEntity: TrackInfoEntity)

    // Для проверки есть ли id перед вставкой
    @Query("SELECT trackId from trackinfoentity WHERE uri_path = :uri")
    fun getIdTrack(uri: String): Int?

    @Query("SELECT * from trackinfoentity WHERE uri_path = :uri")
    fun getTrackByAuthorName(uri: String): TrackInfoEntity?

    @Query("SELECT * from trackinfoentity")
    fun getAllTracks(): List<TrackInfoEntity>

    @Query("UPDATE trackinfoentity SET position = :position WHERE trackId = :trackId")
    fun updateTrackOrder(trackId: Int, position: Int)

    // удалить трек по id
    @Query("DELETE from trackinfoentity WHERE trackId = :trackId")
    fun deleteTrack(trackId: Int)




}