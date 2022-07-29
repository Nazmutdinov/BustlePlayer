package com.example.bustleplayer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import com.example.bustleplayer.activities.MainActivity
import com.example.bustleplayer.adapters.PlaylistAdapter
import com.example.bustleplayer.databinding.FragmentHomeBinding
import com.example.bustleplayer.services.PlayerService
import com.example.bustleplayer.vm.HomeViewModel
import com.example.bustleplayer.vm.PlayerState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), Player.Listener {
    private lateinit var binding: FragmentHomeBinding

    private val adapter = PlaylistAdapter(::itemClick, ::moreItemClick)

    private lateinit var bottomSheet: BottomSheetBehavior<LinearLayout>

    @Inject
    lateinit var dialogFactory: DialogFactory

    private val musicService: PlayerService? by lazy {
        val myActivity = requireActivity() as MainActivity
        myActivity.musicService
    }

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupResultListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

        setupViewModel()
    }

    /**
     * for transferring data to Tracks Fragment
     */
    private fun setupResultListener() {
        setFragmentResultListener("modify_playlist_key") { _, bundle ->
            val playlistId = bundle.getInt("playlistId")
            val title = bundle.getString("title")

            modifyPlaylist(playlistId, title)
        }

        setFragmentResultListener("delete_playlist_key") { _, bundle ->
            val playlistId = bundle.getInt("playlistId")
            deletePlaylist(playlistId)
        }
    }

    /**
     * setup toolbar, adapter, buttons
     */
    private fun setupUI() {
        with(binding) {
            recycleViewPlaylist.adapter = adapter

            bottomSheet = BottomSheetBehavior.from(binding.includeBottomSheet.bottomSheetBehavior)

            val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {

                    binding.includeBottomSheet.imageViewShowBottomSheet.setImageDrawable(
                        context?.getDrawable(viewModel.getImageIdButtonBottomSheet(newState))
                    )
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    //TODO("Not yet implemented")
                }
            }

            bottomSheet.addBottomSheetCallback(bottomSheetCallback)

            includeBottomSheet.buttonPlayPause.setOnClickListener {
                playPauseTapped()
            }

            includeBottomSheet.buttonPlayStop.setOnClickListener {
                stopTapped()
            }

            fragmentPlayToolbar.setOnMenuItemClickListener {
                dialogFactory.showAddPlaylistDialog(requireContext()) { title ->
                    viewModel.addPlaylist(title)
                }
                true
            }
        }
    }

    /**
     * configuring view model
     */
    private fun setupViewModel() {
        // take all playlists
        viewModel.getPlaylists()

        // слушаем получение плейлистов
        viewModel.playlists.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        // слушаем получение состояний плеера
        viewModel.currentPlayerState.observe(viewLifecycleOwner) { state ->
            actionToChangeState(state)
        }

        // event getting tracks
        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            Log.d("myTag", "items = $tracks")
            // stop music service
            //musicService?.stopMusic()

            // add this fragment as listener for event actions
            musicService?.addListener(this@HomeFragment)

            // send tracks to music service to start play them
            musicService?.setMediaItems(tracks, ::playlistError)
        }

        // event for play button icon
        viewModel.playButtonImageResourceId.observe(viewLifecycleOwner) { imageId ->
            binding.includeBottomSheet.buttonPlayPause.setImageResource(imageId)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Log.d("myTag", it)
        }
    }

    /**
     * changes player state from VM
     * set UI labels, buttons, commands to music service
     */
    private fun actionToChangeState(state: PlayerState) {
        when (state) {
            is PlayerState.Play -> viewModel.getTracks(viewModel.playlistId)
            is PlayerState.Pause -> musicService?.pauseMusic()
            is PlayerState.ContinuePlay -> musicService?.continuePlayMusic()
            is PlayerState.Stop -> {
                musicService?.stopMusic()
                musicService?.removeListener(this@HomeFragment)
            }
            else -> Unit
        }
    }

    // adapter methods
    /**
     * whole playlist item tapped
     */
    private fun itemClick(playlistId: Int) {
        bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        viewModel.setPlaylistId(playlistId)
    }

    /**
     * more icon tapped
     */
    private fun moreItemClick(playlistId: Int, title: String) {
        stopTapped()
        viewModel.setPlaylistId(playlistId)

        val action = HomeFragmentDirections.actionHomeFragmentToBottomDialog(playlistId, title)
        findNavController().navigate(action)
    }

    /**
     * add, re-order, delete tracks in playlist
     */
    private fun modifyPlaylist(playlistId: Int, title: String?) {
        title?.let {
            val action =
                HomeFragmentDirections.actionHomeFragmentToTracksFragment(playlistId, title)
            findNavController().navigate(action)
        }
    }

    /**
     * delete playlist
     */
    private fun deletePlaylist(playlistId: Int) {
        viewModel.deletePlaylist(playlistId)
    }

    /**
     * playlist is absent, it's error
     */
    private fun playlistError() {
        // if tracklist is empty
        viewModel.toggleStop()
        //changeButtonIcon(viewModel.currentPlayerState)

        // clear information of current track
        clearCurrentTrackLabels()
    }

    /**
     * clear UI labels of current track
     */
    private fun clearCurrentTrackLabels() {
        with(binding.includeBottomSheet) {
            trackTextView.text = ""
            durationTextView.text = ""
        }
    }

    // bottom sheet aka music manager panel methods
    /**
     * play/pause button tapped
     */
    private fun playPauseTapped() {
        viewModel.togglePlayPause()
    }

    /**
     * button Stop tapped
     */
    private fun stopTapped() {
        viewModel.toggleStop()
    }

    // music service lister methods
    /**
     * service play next track, refresh UI texts
     */
    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)

        binding.includeBottomSheet.trackTextView.text =
            "${mediaMetadata.artist} - ${mediaMetadata.title}"
        binding.includeBottomSheet.durationTextView.text = mediaMetadata.description
    }

    /**
     * service end play all tracks, refresh UI buttons
     */
    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)

        if (playbackState == ExoPlayer.STATE_ENDED) viewModel.toggleStop()
    }
}