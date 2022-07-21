package com.example.bustleplayer.data.repository

import android.content.Context
import com.example.bustleplayer.data.local.BustleDatabase
import com.example.bustleplayer.data.local.entities.TrackInfoEntity
import com.example.bustleplayer.di.DispatcherDb
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataRepositoryImpl @Inject constructor (
    @ApplicationContext private val context: Context,
     private val db: BustleDatabase,
    @DispatcherDb private val dispatcher: CoroutineDispatcher
) {
    /**
     * сохранить трек в БД
     */
    suspend fun addTrack(
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
                val position = dao.getAllTracks().size

                val trackInfoEntity = TrackInfoEntity(
                    uriPath = uri,
                    artist = artist,
                    title = title,
                    duration = duration,
                    position = position
                )

                dao.insertTrack(trackInfoEntity)

                // достанем полный результат вставки
                dao.getTrackByAuthorName(uri)?.let { result ->
                    return@withContext Resource.Success(result)
                }

                return@withContext Resource.Error("ошибка встаки что-то с БД")
//            } else {
//                // такой трек уже есть
//                return@withContext Resource.Error("уже есть")
//            }

        }

    /**
     * достать все треки из БД
     */
    suspend fun getAllTracks(): Resource<List<TrackInfoEntity>> =
        withContext(dispatcher) {
            val dao = db.getDao()

            val result = dao.getAllTracks()
            return@withContext Resource.Success(result)
        }

    /**
     * удалить трек
     */
    suspend fun deleteTrack(trackId: Int): Resource<Boolean> = withContext(dispatcher) {
        val dao = db.getDao()

        dao.deleteTrack(trackId)

        return@withContext Resource.Success(true)
    }

    /**
     * сохранить порядковый номер трека в БД
     */
    suspend fun updateTrackOrder(trackId: Int, position: Int): Resource<Boolean> = withContext(dispatcher) {
        val dao = db.getDao()

        dao.updateTrackOrder(trackId, position)

        return@withContext Resource.Success(true)
    }
}