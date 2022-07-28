package com.example.bustleplayer.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.bustleplayer.ItemTouchHelperCallback
import com.example.bustleplayer.R
import com.example.bustleplayer.adapters.TrackAdapter
import com.example.bustleplayer.databinding.FragmentTracksBinding
import com.example.bustleplayer.vm.TracksViewModel
import com.google.android.exoplayer2.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TracksFragment : Fragment() {
    private lateinit var binding: FragmentTracksBinding

    private val adapter: TrackAdapter by lazy {
        TrackAdapter(requireContext(), ::trackClick)
    }

    private val itemTouchHelperCallback = ItemTouchHelperCallback(
        ::deleteTrack,
        ::moveTrack,
        ::saveTrackOrdering
    )

    private val getContentFileViewer =
        registerForActivityResult(ActivityResultContracts.OpenDocument(), ::saveSelectedAudioFile)

    private val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)

    private var playlistId: Int? = null
    private var title: String? = null

    @Inject
    lateinit var player: ExoPlayer

    private val viewModel: TracksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getInt("playlistId")
            title = it.getString("title")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTracksBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupViewModel()
    }

    /**
     * setup toolbar, adapter, buttons, exoplayer listener
     */
    private fun setupUI() {
        with(binding) {
            recycleView.adapter = adapter

            val mDividerItemDecoration = DividerItemDecoration(
                recycleView.context,
                RecyclerView.VERTICAL
            )
            recycleView.addItemDecoration(mDividerItemDecoration)

            itemTouchHelper.attachToRecyclerView(recycleView)

            fragmentPlayToolbar.title = title

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
        viewModel.getTracks(playlistId)

        viewModel.tracks.observe(viewLifecycleOwner) { playlist ->
            adapter.submitList(playlist)
        }

        viewModel.mediaItem.observe(viewLifecycleOwner) { mediaItem ->
            if (mediaItem == null) player.stop()
            else {
                player.prepare()
                player.setMediaItem(mediaItem)
                player.play()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Log.d("myTag", it)
        }
    }

    // Model operations:
    private fun deleteTrack(position: Int) {
        viewModel.deleteTrack(position, playlistId)
    }

    private fun moveTrack(fromPosition: Int, toPosition: Int) {
        viewModel.swapTracks(fromPosition, toPosition)
    }

    private fun saveTrackOrdering() {
        viewModel.saveTrackOrdering(playlistId)
    }

    private fun selectAudioFile() {
        getContentFileViewer.launch(arrayOf("audio/*"))
    }

    private fun saveSelectedAudioFile(uri: Uri?) {
        uri?.let { notNullUri ->
            playlistId?.let { playlistId ->
                val action = TracksFragmentDirections.actionTracksFragmentToMetadataFragment(
                    notNullUri.toString(),
                    playlistId
                )
                findNavController().navigate(action)
            }
        }
    }

    // UI operations:
    /**
     * command to exoplayer play or pause music after button tapped
     */
    /*
    private fun playOrStopMusic() {
        // toggle model player state
        viewModel.togglePlayPause()

        // change button icon
        changeButtonIcon(viewModel.currentPlayerState)

        when (viewModel.currentPlayerState) {
            // let's play music after full stop or restart this fragment
            is PlayerState.Play -> {
                // prepare playlist for exoplayer
                player.stop()
                player.prepare()
                player.play()
            }
            else -> player.stop()
        }
    }

    /**
     * change button icon corresponds to model player state
     */
    private fun changeButtonIcon(state: PlayerState) {
//        val imageResource =
//            when (state) {
//                is PlayerState.Play -> R.drawable.ic_pause
//                is PlayerState.ContinuePlay -> R.drawable.ic_pause
//                else -> R.drawable.ic_play
//            }


        //binding.pla.setImageResource(imageResource)
    }

     */

    /**
     * tapped track in adapter
     */
    private fun trackClick(position: Int) {
        // save track position as start play position into view model
        viewModel.selectTrack(position)
    }

    // player operations
    /**
     * create playlist in exoplayer
     */
    /*
    private fun createExoplayerPlaylist() {
        viewModel.tracks.value?.map { track ->
            MediaItem.fromUri(track.uri)

        }?.let { mediaItems ->
            //player.setMediaItems(mediaItems)
        }
    }

     */

    /**
     * stop music
     */
    /*
    private fun stopPlayer() {
        // change button icon
        //binding.playPauseButton.setImageResource(R.drawable.ic_play)

        player.stop()

        // save player state
        viewModel.togglePlayStop()
    }

     */

    /*
    // Bound Service Methods
    private fun bindToMusicService() {
        with(requireActivity()) {
            Intent(requireContext(), PlayerService::class.java).also { intent ->
                startService(intent)
            }

            bindService(
                Intent(requireContext(), PlayerService::class.java),
                boundServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }


    }

    private fun unbindMusicService() {
        if (viewModel.isMusicServiceBound) {
            // stop the audio
            playerService?.stopMusic()

            // stop the service
            Intent(requireContext(), PlayerService::class.java).also {
                requireActivity().stopService(it)
            }

            // disconnect the service and save state
            requireActivity().unbindService(boundServiceConnection)

            viewModel.unbindPlayerService()
        }
    }

     */

}