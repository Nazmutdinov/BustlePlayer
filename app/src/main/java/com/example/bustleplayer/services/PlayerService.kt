package com.example.bustleplayer.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : Service() {
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

    fun setMediaItems(items: List<MediaItem>) {
        player.setMediaItems(items)
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