package com.example.bustleplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.bustleplayer.databinding.ActivityMainBinding
import com.example.bustleplayer.vm.MainViewModel
import com.example.bustleplayer.vm.PlayViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

//    var musicService: MusicService? = null

//    private val viewModel: MainViewModel by viewModels()

    // 1

//    private val boundServiceConnection = object : ServiceConnection {
//        // 2
//        override fun onServiceConnected(className: ComponentName, service: IBinder) {
//            val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
//            musicService = binder.getService()
//            viewModel.isMusicServiceBound = true
//            musicService?.addListener()
//        }
//
//        // 3
//        override fun onServiceDisconnected(arg0: ComponentName) {
//            musicService?.stopMusic()
//            musicService = null
//            viewModel.isMusicServiceBound = false
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    override fun onStart() {
        super.onStart()
//        if (!viewModel.isMusicServiceBound) bindToMusicService()
    }

    override fun onDestroy() {
        super.onDestroy()
//        unbindMusicService()
    }

    private fun bindToMusicService() {
        // 1
//        Intent(this, MusicService::class.java).also {
//            // 2
//            bindService(it, boundServiceConnection, Context.BIND_AUTO_CREATE)
//        }
    }

//    private fun unbindMusicService() {
//        if (viewModel.isMusicServiceBound) {
//            // stop the audio
//            // TODO: Call runAction() from MusicService
//
//            // disconnect the service and save state
//            // TODO: Call unbindService()
//
//            viewModel.isMusicServiceBound = false
//            musicService?.stopMusic()
//            unbindService(boundServiceConnection)
//        }
//    }

//    fun playMusic() {
//        musicService?.playMusic(0)
//    }

    /**
     * настройка UI элементов окна
     */
    private fun setupUI() {
        // настройка nav controller
        setupNavigationController()
    }

    private fun setupNavigationController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }
}