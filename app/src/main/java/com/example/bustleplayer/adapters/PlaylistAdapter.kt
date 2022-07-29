package com.example.bustleplayer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bustleplayer.databinding.PlaylistItemBinding
import com.example.bustleplayer.models.Playlist

class PlaylistAdapter(
    private val callbackItemClick: (Int) -> Unit,
    private val callbackMoreItemClick: (Int, String) -> Unit)

    : ListAdapter<Playlist, PlaylistAdapter.Holder>(ItemDiffCallback) {
    class Holder(val binding: PlaylistItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = PlaylistItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)

        onBind(holder, item)
    }

    private fun onBind(holder: Holder, item: Playlist ) {
        with(holder.binding) {
            textViewPlaylistTitle.text = item.title

            buttonMore.setOnClickListener {
                with(item) {
                    callbackMoreItemClick(playlistId, title)
                }
            }
        }

        holder.itemView.setOnClickListener {
            with(item) {
                callbackItemClick(playlistId)
            }
        }
    }

    object ItemDiffCallback: DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
            oldItem == newItem
    }
}