package com.example.bustleplayer.data.repository

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import com.example.bustleplayer.R
import com.example.bustleplayer.data.local.BustleDatabase
import com.example.bustleplayer.data.local.entities.PlayListInfoEntity
import com.example.bustleplayer.data.local.entities.PlayListTrackCrossRef
import com.example.bustleplayer.data.local.entities.TrackInfoEntity
import com.example.bustleplayer.data.local.relations.PlaylistWithTracks
import com.example.bustleplayer.di.DispatcherDb
import com.example.bustleplayer.vm.PlayerState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataRepositoryImpl @Inject constructor(
    private val db: BustleDatabase,
    @DispatcherDb private val dispatcher: CoroutineDispatcher
) {
    /**
     * fetch all playlist from db
     */
    suspend fun getAllPlaylists(): Resource<List<PlayListInfoEntity>> =
        withContext(dispatcher) {
            val dao = db.getDao()

            val result = dao.getAllPlaylists()
            return@withContext Resource.Success(result)
        }


    /**
     * сохранить playlist into database
     */
    suspend fun addPlaylist(
        title: String
    ): Resource<PlayListInfoEntity> =
        withContext(dispatcher) {
            val dao = db.getDao()

            val playListInfoEntity = PlayListInfoEntity(title = title)

            dao.insertPlaylist(playListInfoEntity)

            // достанем полный результат вставки
            dao.getPlayListByTitle(title)?.let { result ->
                return@withContext Resource.Success(result)
            }

            return@withContext Resource.Error("ошибка встаки что-то с БД")
//            } else {
//                // такой трек уже есть
//                return@withContext Resource.Error("уже есть")
//            }

        }

    /**
     * сохранить трек в БД
     */
    suspend fun addTrack(
        playlistId: Int,
        uri: String,
        artist: String,
        title: String,
        duration: Long
    ): Resource<TrackInfoEntity> =
        withContext(dispatcher) {
            val dao = db.getDao()

            // val trackId = dao.getIdTrack(uri)

            //if (trackId == null) {
            // такого трека нет в БД
            // найдем position для него
            val position = (dao.getLastTrackPosition(playlistId) ?: -1) + 1

            val trackInfoEntity = TrackInfoEntity(
                uriPath = uri,
                artist = artist,
                title = title,
                duration = duration
            )

            dao.insertTrack(trackInfoEntity)

            // get trackId for next crossRef insert
            dao.getTrackByUri(uri)?.let { trackInfoEntityWithId ->
                val trackId = trackInfoEntityWithId.trackId

                val playListTrackCrossRef = PlayListTrackCrossRef(
                    playlistId, trackId, position
                )


                dao.insertPlayListTrackCrossRef(playListTrackCrossRef)
                return@withContext Resource.Success(trackInfoEntityWithId)
            }

            return@withContext Resource.Error("ошибка встаки что-то с БД")
//            } else {
//                // такой трек уже есть
//                return@withContext Resource.Error("уже есть")
//            }

        }

    /**
     * fetch tracks for this playlist
     */
    suspend fun getPlaylistTracks(playlistId: Int): Resource<List<PlaylistWithTracks>> =
        withContext(dispatcher) {
            val dao = db.getDao()

            val result = dao.getPlaylistTracks(playlistId)
            return@withContext Resource.Success(result)
        }

    /**
     * fetch tracks for this playlist
     */
    suspend fun getOrderTracks(playlistId: Int): Resource<List<PlayListTrackCrossRef>> =
        withContext(dispatcher) {
            val dao = db.getDao()

            val result = dao.getOrderTracks(playlistId)
            return@withContext Resource.Success(result)
        }

    /**
     * delete track
     */
    suspend fun deleteTrack(trackId: Int): Resource<Boolean> = withContext(dispatcher) {
        val dao = db.getDao()

        dao.deleteTrackCrossRef(trackId)
        dao.deleteTrack(trackId)

        return@withContext Resource.Success(true)
    }

    /**
     * delete playlist
     */
    suspend fun deletePlaylist(playlistId: Int): Resource<Boolean> = withContext(dispatcher) {
        val dao = db.getDao()

        dao.deletePlaylistCrossRef(playlistId)
        dao.deletePlaylist(playlistId)

        return@withContext Resource.Success(true)
    }

    /**
     * сохранить порядковый номер трека в БД
     */
    suspend fun updateTrackOrder(playlistId: Int, trackId: Int, position: Int): Resource<Boolean> =
        withContext(dispatcher) {
            val dao = db.getDao()

            dao.updateTrackOrder(playlistId, trackId, position)

            return@withContext Resource.Success(true)
        }

    /**
     * get color text for track which is playing or not
     */
    fun getTextColorFromTrackState(isTrackNowPlaying: Boolean = false): Int =
        if (isTrackNowPlaying) R.color.play else R.color.black


    /**
     * get icon for track which is playing or not
     */
    fun getImageIdFromTrackState(isTrackNowPlaying: Boolean = false): Int =
        if (isTrackNowPlaying) R.drawable.ic_stop else R.drawable.ic_play

    /**
     * get icon for play button play OR pause
     */
    fun getPlayButtonImageId(isTrackNowPlaying: Boolean = false): Int =
        if (isTrackNowPlaying) R.drawable.ic_pause else R.drawable.ic_play

    /**
     * get icon for expand \ collapsed bottom sheet
     */
//    fun getImageIdButtonBottomSheet(isBottomSheetCollapsed: Boolean = true): Int =
//        if (isBottomSheetCollapsed) R.drawable.ic_expand_less else R.drawable.ic_expand_more


}