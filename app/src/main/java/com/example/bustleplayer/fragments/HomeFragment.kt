package com.example.bustleplayer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import com.example.bustleplayer.MainActivity
import com.example.bustleplayer.R
import com.example.bustleplayer.adapters.PlaylistAdapter
import com.example.bustleplayer.databinding.FragmentHomeBinding
import com.example.bustleplayer.models.Track
import com.example.bustleplayer.services.PlayerService
import com.example.bustleplayer.vm.HomeViewModel
import com.example.bustleplayer.vm.PlayerState
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
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
                    val imageId =
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) R.drawable.ic_expand_more
                        else R.drawable.ic_expand_less

                    binding.includeBottomSheet.imageViewShowBottomSheet.setImageDrawable(
                        context?.getDrawable(imageId)
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

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Log.d("myTag", it)
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
     * prepare media items for music service
     */
    private fun setupAndPlayMusic(items: List<Track>) {
        items.map { track ->
            val mediaMetadata = MediaMetadata.Builder()
                .setArtist(track.artist)
                .setTitle(track.title)
                .setDescription(track.duration)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(track.uri)
                .setMediaMetadata(mediaMetadata)
                .build()

            mediaItem
        }.let { mediaItems ->
            // установка Media Items в player сервисе
            if (mediaItems.isNotEmpty()) {
                musicService?.setMediaItems(mediaItems)
                musicService?.playMusic()
            } else {
                viewModel.toggleStop()
                changeButtonIcon(viewModel.currentPlayerState)
                binding.includeBottomSheet.trackTextView.text = ""
                binding.includeBottomSheet.durationTextView.text = ""
            }
        }
    }

    // bottom sheet aka music manager panel methods
    /**
     * play/pause button tapped
     */
    private fun playPauseTapped() {
        // save state in vm
        viewModel.togglePlayPause()

        // change button icon
        changeButtonIcon(viewModel.currentPlayerState)

        // TODO move all logic in VM
        if (viewModel.currentPlayerState is PlayerState.Play) {
            // stop music service
            musicService?.stopMusic()

            // add this fragment as listener for event actions
            musicService?.addListener(this@HomeFragment)

            // get all tracks for this playlist
            // TODO remove viewModel.playlistId parameter
            viewModel.getTracks(viewModel.playlistId)

            // event getting tracks
            viewModel.tracks.observe(viewLifecycleOwner) { items ->
                setupAndPlayMusic(items)
            }
        }

        if (viewModel.currentPlayerState is PlayerState.Pause) {
            musicService?.pauseMusic()
        }

        if (viewModel.currentPlayerState is PlayerState.ContinuePlay) {
            musicService?.continuePlayMusic()
        }
    }

    /**
     * button Stop tapped
     */
    private fun stopTapped() {
        // save state in vm
        viewModel.toggleStop()

        // change button icon
        changeButtonIcon(viewModel.currentPlayerState)

        // stop music service
        musicService?.stopMusic()

        // add this fragment as listener for event actions either we will have too many listeners
        musicService?.removeListener(this@HomeFragment)
    }

    /**
     * TODO move logic in vm
     * change button icon corresponds to model player state
     */
    private fun changeButtonIcon(state: PlayerState) {
        val imageResource =
            when (state) {
                is PlayerState.Play -> R.drawable.ic_pause
                is PlayerState.ContinuePlay -> R.drawable.ic_pause
                else -> R.drawable.ic_play
            }

        binding.includeBottomSheet.buttonPlayPause.setImageResource(imageResource)
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

        if (playbackState == ExoPlayer.STATE_ENDED) {
            viewModel.toggleStop()
            changeButtonIcon(viewModel.currentPlayerState)
        }
    }
}