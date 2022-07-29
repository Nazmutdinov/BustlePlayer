package com.example.bustleplayer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.bustleplayer.models.Track
import com.example.bustleplayer.models.TrackExtended
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : Service(), Player.Listener {
    @Inject
    lateinit var player: ExoPlayer

    private val binder by lazy { MusicBinder() }

    val isPlaying get() = player.isPlaying

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }

//    fun setMediaItems(items: List<MediaItem>) {
//        player.setMediaItems(items)
//    }

    fun setMediaItems(items: List<Track>, tracklistIsEmptyCallback: () -> Unit) {
        // firstly,need to stop all music
        stopMusic()

        // try setup playlist tracks
        items.map { track ->
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

    fun playMusic(position: Int = 0) {
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

    fun addListener(listener: Player.Listener) {
        player.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        player.removeListener(listener)
    }

    inner class MusicBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }
}