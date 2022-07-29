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
import com.example.bustleplayer.activities.MainActivity
import com.example.bustleplayer.adapters.TrackAdapter
import com.example.bustleplayer.databinding.FragmentTracksBinding
import com.example.bustleplayer.services.MusicService
import com.example.bustleplayer.vm.TracksViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TracksFragment : Fragment() {
    private lateinit var binding: FragmentTracksBinding

    private val adapter: TrackAdapter by lazy {
        TrackAdapter(requireContext(), ::trackPlayClick)
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

    private val musicService: MusicService? by lazy {
        val myActivity = requireActivity() as MainActivity
        myActivity.musicService
    }

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
     * setup toolbar, adapter, buttons
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

        viewModel.currentTrack.observe(viewLifecycleOwner) { trackExtended ->
            trackExtended?.let {
                musicService?.setMediaItem(trackExtended)
            }

            if (trackExtended == null) musicService?.stopMusic()
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

    /**
     * play button tapped on track in adapter
     */
    private fun trackPlayClick(position: Int) {
        // save track position as start play position into view model
        viewModel.trackPlayTapped(position)
    }
}