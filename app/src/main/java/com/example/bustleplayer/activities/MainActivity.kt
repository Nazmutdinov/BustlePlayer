package com.example.bustleplayer.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.bustleplayer.R
import com.example.bustleplayer.databinding.ActivityMainBinding
import com.example.bustleplayer.services.MusicService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    var musicService: MusicService? = null

    private val boundServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
            musicService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            musicService?.stopMusic()
            musicService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()

        setupService()
    }

    private fun setupService() {
        startMusicService()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusicService()
    }

    private fun startMusicService() {
        // 1
        Intent(this, MusicService::class.java).also { intent ->
            startService(intent)
        }
        Intent(this, MusicService::class.java).also {
            // 2
            bindService(it, boundServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun stopMusicService() {
        // stop the audio
        // TODO: Call runAction() from MusicService

        // disconnect the service and save state
        // TODO: Call unbindService()

        // stop the service
        Intent(this, MusicService::class.java).also { intent ->
            stopService(intent)
        }

        unbindService(boundServiceConnection)
    }

    /**
     * настройка UI элементов окна
     */
    private fun setupUI() {
        // настройка nav controller
        setupNavigationController()
    }

    private fun setupNavigationController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }
}