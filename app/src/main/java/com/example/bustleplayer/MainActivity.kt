package com.example.bustleplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.bustleplayer.databinding.ActivityMainBinding
import com.example.bustleplayer.services.PlayerService
import com.example.bustleplayer.vm.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    var musicService: PlayerService? = null

    private val boundServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder: PlayerService.MusicBinder = service as PlayerService.MusicBinder
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
        Intent(this, PlayerService::class.java).also { intent ->
            startService(intent)
        }
        Intent(this, PlayerService::class.java).also {
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
        Intent(this, PlayerService::class.java).also { intent ->
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