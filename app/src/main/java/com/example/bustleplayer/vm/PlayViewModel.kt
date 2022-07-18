package com.example.bustleplayer.vm

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bustleplayer.Utils
import com.example.bustleplayer.data.repository.LocalDataRepositoryImpl
import com.example.bustleplayer.data.repository.Resource
import com.example.bustleplayer.di.DispatcherVM
import com.example.bustleplayer.models.Track
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    @DispatcherVM private val dispatcher: CoroutineDispatcher,
    private val repositoryImpl: LocalDataRepositoryImpl,
    private val utils: Utils,
    private val mediaMetadataRetriever: MediaMetadataRetriever
) : ViewModel() {
    private val _playList = MutableLiveData<List<Track>>()
    val playList: LiveData<List<Track>> = _playList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    var currentPosition: Int = 0

    private var _currentPlayerState: PlayerState = PlayerState.Stop()
    val currentPlayerState: PlayerState get() = _currentPlayerState

    fun getAllTracks(context: Context) {
        viewModelScope.launch(dispatcher) {
            when (val resource = repositoryImpl.getAllTracks()) {
                is Resource.Success -> {
                    // преобразуем модель
                    resource.data?.map { trackInfoEntity ->
                        val uri = Uri.parse(trackInfoEntity.uriPath)
                        Track(
                            trackId = trackInfoEntity.trackId,
                            uri = uri,
                            artist = getMediaData(
                                context,
                                uri,
                                MediaMetadataRetriever.METADATA_KEY_ARTIST
                            ),
                            name = getMediaData(
                                context,
                                uri,
                                MediaMetadataRetriever.METADATA_KEY_TITLE
                            ),
                            durationMs = getMediaData(
                                context,
                                uri,
                                MediaMetadataRetriever.METADATA_KEY_DURATION
                            ).toLong(),
                            duration = utils.getDurationString(
                                getMediaData(
                                    context,
                                    uri,
                                    MediaMetadataRetriever.METADATA_KEY_DURATION
                                ).toLong()
                            ),
                            isSelected = false
                        )
                    }?.let { items ->
                        if (items.isNotEmpty()) items[currentPosition].isSelected = true
                        _playList.postValue(items)
                    }
                }
                else -> _errorMessage.postValue("ошибка ${resource.message}")
            }
        }
    }

    private fun getMediaData(context: Context, uri: Uri, metaDataId: Int): String {
        mediaMetadataRetriever.setDataSource(context, uri)

        return mediaMetadataRetriever.extractMetadata(metaDataId) ?: ""
    }

    fun saveTrack(context: Context, uri: Uri) {
        viewModelScope.launch(dispatcher) {
            // зададим позицию, которую нужно присвоить
            var newPosition = 0
            _playList.value?.size?.let { size ->
                newPosition = size
            }

            when (val resource =
                repositoryImpl.addTrack(uri.toString(), newPosition)) {
                is Resource.Success -> {
                    // загрузим плейлист по новой
                    getAllTracks(context)
                }
                else -> _errorMessage.postValue("ошибка ${resource.message}")
            }
        }
    }

    fun deleteTrack(context: Context, position: Int) {
        _playList.value?.let { playlist ->
            val track = playlist[position]
            val trackId = track.trackId
            viewModelScope.launch(dispatcher) {
                val resource = repositoryImpl.deleteTrack(trackId)
                if (resource is Resource.Success) {
                    getAllTracks(context)
                }
            }
        }
    }

    /**
     * выделить трек цветом, сохранить его как текущий трек в модели
     */
    fun selectTrack(position: Int) {
        viewModelScope.launch {
            val temp = mutableListOf<Track>()

            _playList.value?.forEach { track ->
                temp.add(track.copy(isSelected = false))
            }


            temp[currentPosition].isSelected = false


            temp[position].isSelected = true

            currentPosition = position

            _playList.value = temp
        }

    }

    /**
     * поменять местами треки
     */
    fun swapTracks(oldPosition: Int, newPosition: Int) {
        _playList.value?.let { items ->
            Collections.swap(items, oldPosition, newPosition)
        }

        // в БД эти изменения еще не передались!
        // На забыть вызвать saveTrackOrdering
    }

    /**
     * сохранить все перестановки треков в плейлист в БД
     */
    fun saveTrackOrdering() {
        viewModelScope.launch {
            _playList.value?.let { items ->
                for ((index, track) in items.withIndex()) {
                    repositoryImpl.updateTrackOrder(track.trackId, index)
                }
            }
        }
    }

    /**
     * change player state
     */
    fun togglePlayPause() {
        _currentPlayerState = when (_currentPlayerState) {
            is PlayerState.Stop -> PlayerState.Play()
            is PlayerState.ContinuePlay -> PlayerState.Pause()
            is PlayerState.Pause -> PlayerState.ContinuePlay()
            is PlayerState.Play -> PlayerState.Pause()
        }
    }

    /**
     * stop player
     */
    fun togglePlayStop() {
        _currentPlayerState = PlayerState.Stop()
    }
}

