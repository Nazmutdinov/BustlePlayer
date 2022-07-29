package com.example.bustleplayer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bustleplayer.models.Track
import com.example.bustleplayer.models.TrackExtended
import com.example.bustleplayer.models.TrackTextData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service(), Player.Listener {

    @Inject
    lateinit var player: ExoPlayer

    private val _trackTextData = MutableLiveData<TrackTextData>()
    val trackTextData: LiveData<TrackTextData> = _trackTextData

    private val _eventPlaylistCompleted = MutableLiveData<Boolean>()
    val eventPlaylistCompleted: LiveData<Boolean> = _eventPlaylistCompleted

    private val binder by lazy { MusicBinder() }

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        player.addListener(this)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }

    fun setMediaItems(tracks: List<Track>, tracklistIsEmptyCallback: () -> Unit) {
        // firstly,need to stop all music
        stopMusic()

        // try setup playlist tracks
        tracks.map { track ->
            val mediaMetadata = MediaMetadata.Builder()
                .setArtist(track.artist)
                .setTitle(track.title)
                .setDescription(track.duration)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(track.uri)
                .setMediaMetadata(mediaMetadata)
                .build()

            mediaItem
        }.let { mediaItems ->
            // установка Media Items в player сервисе
            if (mediaItems.isNotEmpty()) {
                player.setMediaItems(mediaItems)
                playMusic()
            } else {
                tracklistIsEmptyCallback()
            }
        }
    }

    fun setMediaItem(track: TrackExtended) {
        // firstly,need to stop all music
        stopMusic()
        _eventPlaylistCompleted.value = false

        // try setup playlist tracks
        val mediaMetadata = MediaMetadata.Builder()
            .setArtist(track.artist)
            .setTitle(track.title)
            .setDescription(track.duration)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(track.uri)
            .setMediaMetadata(mediaMetadata)
            .build()

        player.setMediaItem(mediaItem)
        playMusic()
    }

    private fun playMusic(position: Int = 0) {
        player.prepare()
        player.seekToDefaultPosition(position)
        player.play()
    }

    fun continuePlayMusic() {
        player.play()
    }

    fun pauseMusic() {
        player.pause()
    }

    fun stopMusic() {
        player.stop()
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)

        _trackTextData.value = TrackTextData(
            artist = mediaMetadata.artist.toString(),
            title = mediaMetadata.title.toString(),
            duration = mediaMetadata.description.toString()
        )
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        _eventPlaylistCompleted.value = playbackState == ExoPlayer.STATE_ENDED
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}