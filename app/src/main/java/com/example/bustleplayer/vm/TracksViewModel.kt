package com.example.bustleplayer.vm

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bustleplayer.Utils
import com.example.bustleplayer.data.repository.LocalDataRepositoryImpl
import com.example.bustleplayer.data.repository.Resource
import com.example.bustleplayer.di.DispatcherVM
import com.example.bustleplayer.models.TrackExtended
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TracksViewModel @Inject constructor(
    @DispatcherVM private val dispatcher: CoroutineDispatcher,
    private val repositoryImpl: LocalDataRepositoryImpl,
    private val utils: Utils
) : ViewModel() {
    private val _tracks = MutableLiveData<List<TrackExtended>>()
    val tracks: LiveData<List<TrackExtended>> = _tracks

    private val _currentTrack = MutableLiveData<TrackExtended>()
    val currentTrack: LiveData<TrackExtended> = _currentTrack

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentPosition: Int? = null

    fun getTracks(playlistId: Int?) {
        playlistId?.let {
            viewModelScope.launch(dispatcher) {
                val resourceOrder = repositoryImpl.getOrderTracks(playlistId)
                val orderedTracklist = resourceOrder.data?.sortedBy { playListTrackCrossRef ->
                    playListTrackCrossRef.position
                }?.map { playListTrackCrossRef ->
                    playListTrackCrossRef.trackId
                }

                when (val resource = repositoryImpl.getPlaylistTracks(playlistId)) {
                    is Resource.Success -> {
                        // преобразуем модель
                        resource.data?.flatMap {
                            it.trackInfoEntity
                        }?.map { trackInfoEntity ->
                            val uri = Uri.parse(trackInfoEntity.uriPath)
                            TrackExtended(
                                trackId = trackInfoEntity.trackId,
                                uri = uri,
                                artist = trackInfoEntity.artist,
                                title = trackInfoEntity.title,
                                durationMs = trackInfoEntity.duration,
                                duration = utils.getDurationString(trackInfoEntity.duration),
                                isPlaying = false,
                                textColor = repositoryImpl.getTextColorFromTrackState(),
                                imagePlayId = repositoryImpl.getImageIdFromTrackState()
                            )
                        }?.let { items ->
                            currentPosition?.let {
                                if (items.isNotEmpty()) {
                                    items[it].textColor = repositoryImpl.getTextColorFromTrackState(true)
                                    items[it].imagePlayId = repositoryImpl.getImageIdFromTrackState(true)
                                }
                            }

                            orderedTracklist?.map { id ->
                                val track = items.first { track ->
                                    track.trackId == id
                                }
                                track
                            }?.let { tracksOrdered ->
                                if (tracksOrdered.isNotEmpty()) {
                                    _tracks.postValue(tracksOrdered)
                                    return@launch
                                }
                            }

                            _tracks.postValue(items)
                        }
                    }
                    else -> _errorMessage.postValue("ошибка ${resource.message}")
                }
            }
        }
    }

    fun deleteTrack(position: Int, playlistId: Int?) {
        _tracks.value?.let { playlist ->
            val track = playlist[position]
            val trackId = track.trackId
            viewModelScope.launch(dispatcher) {
                val resource = repositoryImpl.deleteTrack(trackId)
                if (resource is Resource.Success) {
                    getTracks(playlistId)
                }
            }
        }
    }

    /**
     * выделить трек цветом, сохранить его как текущий трек в модели
     */
    fun trackPlayTapped(position: Int) {
        viewModelScope.launch {
            var newIsPlaying = false

            _tracks.value?.let { items ->
                val track = items[position]
                newIsPlaying = !track.isPlaying
            }

            val temp = mutableListOf<TrackExtended>()

            _tracks.value?.forEach { track ->
                temp.add(track.copy(isPlaying = false))
            }

//            currentPosition?.let {
//                temp[it].isSelected = false
//            }
//
//            temp[position].isSelected = true
            temp[position].isPlaying = newIsPlaying

            currentPosition = position

            _tracks.value = temp

            _currentTrack.value = if (temp[position].isPlaying) temp[position] else null
        }
    }

//    private fun startMusic(track: TrackExtended) {
//        if (track.isPlaying) _mediaItem.value = MediaItem.fromUri(track.uri)
//        else _mediaItem.value = null
//    }

    /**
     * поменять местами треки
     */
    fun swapTracks(oldPosition: Int, newPosition: Int) {
        _tracks.value?.let { items ->
            Collections.swap(items, oldPosition, newPosition)
        }

        // в БД эти изменения еще не передались!
        // На забыть вызвать saveTrackOrdering
    }

    /**
     * сохранить все перестановки треков в плейлист в БД
     */
    fun saveTrackOrdering(playlistId: Int?) {
        viewModelScope.launch {
            _tracks.value?.let { items ->
                playlistId?.let {
                    for ((index, track) in items.withIndex()) {
                        repositoryImpl.updateTrackOrder(playlistId, track.trackId, index)
                    }
                }

            }
        }
    }

    /**
     * change player state
     */
    /*
    fun togglePlayPause() {
        _currentPlayerState = when (_currentPlayerState) {
            is PlayerState.Stop -> PlayerState.Play()
            else -> PlayerState.Stop()
        }
    }

     */

    /**
     * stop player
     */
    /*
    fun togglePlayStop() {
        _currentPlayerState = PlayerState.Stop()
    }

    fun bindPlayerService() {
        _isMusicServiceBound = true
    }

    fun unbindPlayerService() {
        _isMusicServiceBound = false
    }

     */
}

