package com.example.bustleplayer.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class PlayerService: Service() {

    init {
        Log.d("myTag","service started...")
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //return super.onStartCommand(intent, flags, startId)
        val data = intent?.getStringExtra("EXTRA_URI")
        data?.let {
            Log.d("myTag","получил URi $it")
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}