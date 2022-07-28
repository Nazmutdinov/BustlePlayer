package com.example.bustleplayer.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bustleplayer.databinding.FragmentMetadataBinding
import com.example.bustleplayer.vm.MetadataViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MetadataFragment : Fragment() {
    private lateinit var binding: FragmentMetadataBinding

    private var playlistId: Int? = null
    private var uri: Uri? = null

    private val viewModel: MetadataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val uriParam = it.getString("uri")
            playlistId = it.getInt("playlistId")

            uriParam?.let { uriString ->
                uri = Uri.parse(uriString)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMetadataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()

        setupUI()
    }

    /**
     * setup toolbar, adapter, buttons, exoplayer listener
     */
    private fun setupUI() {
        with(binding) {
            with(viewModel) {
                textFieldArtist.editText?.setText(artist)
                textFieldTitle.editText?.setText(title)

                fragmentMetadataToolbar.setOnMenuItemClickListener {
                    saveNewTrack(
                        textFieldArtist.editText?.text.toString(),
                        textFieldTitle.editText?.text.toString()
                    )

                    startObserveViewModel()

                    true
                }

                fragmentMetadataToolbar.setNavigationOnClickListener {
                    findNavController().popBackStack()
                }
            }
        }

    }

    /**
     * configuring view model
     */
    private fun setupViewModel() {
        viewModel.getMetadataFromUri(requireContext(), uri)
    }

    private fun saveNewTrack(artist: String, title: String) {
        viewModel.saveTrack(requireContext(), playlistId, uri, artist, title)
    }

    private fun startObserveViewModel() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT)
        }

        viewModel.eventSave.observe(viewLifecycleOwner) { isSaveSuccessfully ->
            if (isSaveSuccessfully) findNavController().popBackStack()
        }
    }
}