package com.example.bustleplayer.vm

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bustleplayer.R
import com.example.bustleplayer.Utils
import com.example.bustleplayer.data.repository.LocalDataRepositoryImpl
import com.example.bustleplayer.data.repository.Resource
import com.example.bustleplayer.di.DispatcherVM
import com.example.bustleplayer.models.Playlist
import com.example.bustleplayer.models.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @DispatcherVM private val dispatcher: CoroutineDispatcher,
    private val repositoryImpl: LocalDataRepositoryImpl,
    private val utils: Utils
): ViewModel() {
    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private var _playlistId: Int? = null
    val playlistId get() = _playlistId

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    private val _currentPlayerState = MutableLiveData<PlayerState>()
    val currentPlayerState: LiveData<PlayerState> = _currentPlayerState

    private val _playButtonImageResourceId = MutableLiveData<Int>()
    val playButtonImageResourceId: LiveData<Int> = _playButtonImageResourceId

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        _currentPlayerState.value = PlayerState.Initial()
        _playButtonImageResourceId.value = repositoryImpl.getPlayButtonImageId()
    }

    fun getPlaylists() {
        viewModelScope.launch(dispatcher) {
            when(val resource = repositoryImpl.getAllPlaylists()) {
                is Resource.Success -> {
                    resource.data?.let { items ->
                        val result = items.map { playListInfoEntity ->
                            Playlist(playListInfoEntity.playlistId, playListInfoEntity.title)
                        }

                        _playlists.postValue(result)
                    }
                }
                else -> _errorMessage.postValue("ошибка ${resource.message}")
            }
        }
    }

    fun addPlaylist(title: String) {
        viewModelScope.launch(dispatcher) {
            when(val resource = repositoryImpl.addPlaylist(title)) {
                is Resource.Success -> getPlaylists()
                else -> _errorMessage.postValue("ошибка ${resource.message}")
            }
        }
    }

    fun deletePlaylist(playlistId: Int?) {
        playlistId?.let {
            viewModelScope.launch(dispatcher) {
                when (val resource = repositoryImpl.deletePlaylist(playlistId)) {
                    is Resource.Success -> getPlaylists()
                    else -> _errorMessage.postValue("ошибка ${resource.message}")
                }
            }
        }
    }

    fun setPlaylistId(playlistId: Int) {
        _playlistId = playlistId
    }

    /**
     * get all tracks for playlist
     */
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
                            Track(
                                trackId = trackInfoEntity.trackId,
                                uri = uri,
                                artist = trackInfoEntity.artist,
                                title = trackInfoEntity.title,
                                durationMs = trackInfoEntity.duration,
                                duration = utils.getDurationString(trackInfoEntity.duration)
                            )
                        }?.let { items ->

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

    /**
     * change player state b/t play/pause
     */
    fun togglePlayPause() {
        currentPlayerState.value?.let { playerState ->
            when(playerState) {
                is PlayerState.Pause -> {
                    _currentPlayerState.value = PlayerState.ContinuePlay()
                    _playButtonImageResourceId.value = repositoryImpl.getPlayButtonImageId(true)
                }
                is PlayerState.Play -> {
                    _currentPlayerState.value = PlayerState.Pause()
                    _playButtonImageResourceId.value = repositoryImpl.getPlayButtonImageId()
                }
                is PlayerState.ContinuePlay -> {
                    _currentPlayerState.value = PlayerState.Pause()
                    _playButtonImageResourceId.value = repositoryImpl.getPlayButtonImageId()
                }
                else -> {
                    _currentPlayerState.value = PlayerState.Play()
                    _playButtonImageResourceId.value = repositoryImpl.getPlayButtonImageId(true)
                }
            }
        }
    }

    /**
     * set player state to stop
     */
    fun toggleStop() {
        _currentPlayerState.value = PlayerState.Stop()
        _playButtonImageResourceId.value = repositoryImpl.getPlayButtonImageId()
    }

    /**
     * expand bottom sheet player manager
     */
    fun getImageIdButtonBottomSheet(newState: Int): Int =
        if (newState == BottomSheetBehavior.STATE_EXPANDED) R.drawable.ic_expand_more
        else R.drawable.ic_expand_less
}
