package com.example.bustleplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bustleplayer.databinding.FragmentPlayBinding
import com.example.bustleplayer.vm.PlayViewModel
import com.example.bustleplayer.vm.PlayerState
import com.google.android.exoplayer2.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayFragment : Fragment(), Player.Listener {
    private lateinit var binding: FragmentPlayBinding

    private val adapter: TrackAdapter by lazy {
        TrackAdapter(requireContext(), ::trackClick)
    }

    private val viewModel: PlayViewModel by viewModels()

    @Inject
    lateinit var player: ExoPlayer


//    private var musicService: MusicService? = null

    // 1
    /*
    private val boundServiceConnection = object : ServiceConnection {

        // 2
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder: MusicService.MusicBinder = service as MusicService.MusicBinder
            musicService = binder.getService()
            Log.d("myTag","musicService $musicService")
//            mainViewModel.isMusicServiceBound = true
        }

        // 3
        override fun onServiceDisconnected(arg0: ComponentName) {
//            musicService?.runAction(MusicState.STOP)
            musicService = null
//            mainViewModel.isMusicServiceBound = false
        }
    }

     */


    private val itemTouchHelperCallback = ItemTouchHelperCallback(
        ::deleteTrack,
        ::moveTrack,
        ::saveTrackOrdering
    )

    private val getContentFileViewer =
        registerForActivityResult(ActivityResultContracts.OpenDocument(), ::saveSelectedAudioFile)
//        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
//
//        }

    private val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupViewModel()

//        bindToMusicService()
    }

    /*
    private fun bindToMusicService() {
        // 1
        Intent(requireActivity(), MusicService::class.java).also {
            // 2
            requireActivity().bindService(it, boundServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

     */


    /**
     * setup toolbar, adapter, buttons, exoplayer listener
     */
    private fun setupUI() {
        //val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)

        with(binding) {
            recycleView.adapter = adapter

            val mDividerItemDecoration = DividerItemDecoration(
                recycleView.getContext(),
                RecyclerView.VERTICAL
            )
            recycleView.addItemDecoration(mDividerItemDecoration)

            itemTouchHelper.attachToRecyclerView(recycleView)

            playPauseButton.setOnClickListener {
                playOrPausePlayer()
            }

            stopButton.setOnClickListener {
                stopPlayer()
            }

            fragmentPlayToolbar.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == R.id.add_track) {
                    selectAudioFile()
                }
                true
            }
        }
    }

    /**
     * configuring view model
     */
    private fun setupViewModel() {
        // take all tracks from model
        viewModel.getAllTracks(requireContext())

        viewModel.playList.observe(viewLifecycleOwner) { playlist ->
            adapter.submitList(playlist)

            // prepare playlist for exoplayer
            createExoplayerPlaylist()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Log.d("myTag", it)
        }
    }

    // Model operations:
    private fun deleteTrack(position: Int) {
        viewModel.deleteTrack(requireContext(), position)
    }

    private fun moveTrack(fromPosition: Int, toPosition: Int) {
        viewModel.swapTracks(fromPosition, toPosition)
    }

    private fun saveTrackOrdering() {
        viewModel.saveTrackOrdering()
    }

    private fun selectAudioFile() {
        getContentFileViewer.launch(arrayOf("audio/*"))
    }

    private fun saveSelectedAudioFile(uri: Uri?) {
        uri?.let { notNull ->
            viewModel.saveTrack(requireContext(), notNull)
        }
    }

    // UI operations:
    /**
     * command to exoplayer play or pause music after button tapped
     */
    private fun playOrPausePlayer() {
        // toggle model player state
        viewModel.togglePlayPause()

        // change button icon
        changeButtonIcon(viewModel.currentPlayerState)

        when (viewModel.currentPlayerState) {
            // let's play music after full stop or restart this fragment
            is PlayerState.Play -> {
                player.prepare()
                player.seekToDefaultPosition(viewModel.currentPosition)
                player.play()
            }
            // continue play after pause
            is PlayerState.ContinuePlay -> player.play()
//            is PlayerState.ContinuePlay -> musicService?.continuePlayMusic()
//            else -> musicService?.pauseMusic()
            else -> player.pause()
        }
    }

    /**
     * change button icon corresponds to model player state
     */
    private fun changeButtonIcon(state: PlayerState) {
        val imageResource =
            when (state) {
                is PlayerState.Play -> R.drawable.ic_pause
                is PlayerState.ContinuePlay -> R.drawable.ic_pause
                else -> R.drawable.ic_play
            }

        binding.playPauseButton.setImageResource(imageResource)
    }

    /**
     * tapped track in adapter
     */
    private fun trackClick(position: Int) {
        viewModel.selectTrack(position)
        // let's stop play and start play from this track
        viewModel.togglePlayStop()

        // set this track as default in exoplayer
     //   player.seekToDefaultPosition(viewModel.currentPosition)

        // start playing music from this track
        playOrPausePlayer()
    }

    // player operations
    /**
     * create playlist in exoplayer
     */
    private fun createExoplayerPlaylist() {
        viewModel.playList.value?.map { track ->
            Log.d("myTag","track $track")
            MediaItem.fromUri(track.uri)

        }?.let { mediaItems ->
            player.setMediaItems(mediaItems)

//            musicService?.createExoplayerPlaylist(mediaItems)
        }
    }

    /**
     * stop music
     */
    private fun stopPlayer() {
        // change button icon
        binding.playPauseButton.setImageResource(R.drawable.ic_play)
//        musicService?.stopMusic()

        // save player state
        viewModel.togglePlayStop()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        if (playbackState == Player.STATE_ENDED) {
            // change button icon
            binding.playPauseButton.setImageResource(R.drawable.ic_play)
            viewModel.togglePlayStop()
        }
    }

    /**
     * event track changed by player self
     */
    /*
    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        // only if play, cause when we just create list not playing this event will be trigger too
        if (player.isPlaying) {
            viewModel.currentPosition = viewModel.currentPosition.plus(1)

            viewModel.selectTrack(viewModel.currentPosition)

            binding.recycleView.scrollToPosition(viewModel.currentPosition)
            player.seekToDefaultPosition(viewModel.currentPosition)
        }
    }

     */

    override fun onDestroy() {
        super.onDestroy()

    }



}