package com.example.bustleplayer.vm

import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    private var _isMusicServiceBound = false
    val isMusicServiceBound get() = _isMusicServiceBound

    fun bindService() {
        _isMusicServiceBound = true
    }

    fun unbindService() {
        _isMusicServiceBound = true
    }

}