package com.example.bustleplayer.vm

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bustleplayer.data.repository.LocalDataRepositoryImpl
import com.example.bustleplayer.data.repository.Resource
import com.example.bustleplayer.di.DispatcherVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MetadataViewModel @Inject constructor(
    @DispatcherVM private val dispatcher: CoroutineDispatcher,
    private val repositoryImpl: LocalDataRepositoryImpl,
    private val mediaMetadataRetriever: MediaMetadataRetriever
) : ViewModel() {
    private val _eventSave = MutableLiveData<Boolean>()
    val eventSave: LiveData<Boolean> = _eventSave

    private var _artist: String = ""
    val artist get() = _artist

    private var _title: String = ""
    val title get() = _title

    //private var _uri: Uri? = null

//    private var _playlistId: Int = -1

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun getMetadataFromUri(context: Context, uri: Uri?) {
        // clear event
        _eventSave.value = false
        uri?.let {
            _artist = getMediaData(
                context,
                uri,
                MediaMetadataRetriever.METADATA_KEY_ARTIST
            )

            _title = getMediaData(
                context,
                uri,
                MediaMetadataRetriever.METADATA_KEY_TITLE
            )
        }
    }

    fun saveTrack(context: Context, playlistId: Int?, uri: Uri?, artist: String, title: String) {
        viewModelScope.launch(dispatcher) {
            playlistId?.let {
                uri?.let {
                    val duration = getMediaData(
                        context,
                        uri,
                        MediaMetadataRetriever.METADATA_KEY_DURATION
                    ).toLong()

                    when (val resource = repositoryImpl.addTrack(
                        playlistId,
                        uri.toString(),
                        artist,
                        title,
                        duration
                    )) {
                        is Resource.Success -> _eventSave.postValue(true)
                        else -> _errorMessage.postValue("ошибка ${resource.message}")
                    }
                }
            }
        }
    }

    private fun getMediaData(context: Context, uri: Uri, metaDataId: Int): String {
        mediaMetadataRetriever.setDataSource(context, uri)

        return mediaMetadataRetriever.extractMetadata(metaDataId) ?: ""
    }


}