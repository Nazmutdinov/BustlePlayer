package com.example.bustleplayer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.bustleplayer.databinding.MoreDialogBinding

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BottomSheetDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: MoreDialogBinding

    private var playlistId: Int? = null
    private var title: String? = null


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
        binding = MoreDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    /**
     * setup toolbar, adapter, buttons, exoplayer listener
     */
    private fun setupUI() {
        with(binding) {
            modifyItem.setOnClickListener {
                modifyPlaylist()
            }

            deleteItem.setOnClickListener {
                deletePlaylist()
            }

            cancelItem.setOnClickListener {
                closeSheet()
            }
        }
    }

    private fun modifyPlaylist() {
        closeSheet()
        setFragmentResult("modify_playlist_key",
            bundleOf("playlistId" to playlistId, "title" to title))
    }

    private fun deletePlaylist() {
        closeSheet()
        setFragmentResult("delete_playlist_key", bundleOf("playlistId" to playlistId))
    }

    private fun closeSheet() {
        findNavController().popBackStack()
    }
}