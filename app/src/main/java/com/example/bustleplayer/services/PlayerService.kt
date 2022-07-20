package com.example.bustleplayer.services

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : Service() {

    init {
        Log.d("myTag", "service started...")
    }

    @Inject
    lateinit var player: ExoPlayer

    private val binder by lazy { MusicBinder() }

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        /*
        val data = intent?.getStringExtra("EXTRA_URI")
        data?.let { uriStr ->
            val mediaItem = MediaItem.fromUri(uriStr)

            player.setMediaItems(listOf(mediaItem))
            player.prepare()
            player.play()
        }
         */
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        player.stop()
        player.release()
    }


    fun setMediaItems(items: List<MediaItem>) {
        player.setMediaItems(items)
    }

    fun playMusic(position: Int) {
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

    inner class MusicBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }
}