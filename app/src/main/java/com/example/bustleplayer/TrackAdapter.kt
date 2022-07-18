package com.example.bustleplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bustleplayer.databinding.TrackItemBinding
import com.example.bustleplayer.models.Track

class TrackAdapter(
    private val context: Context,
    private val callbackClickItem: (Int) -> Unit
) :
    ListAdapter<Track, TrackAdapter.Holder>(ItemDiffCallback) {
    class Holder(val binding: TrackItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)

        val trackName = context.getString(R.string.track_name, item.artist, item.name)

        with(holder.binding) {
            trackNameTextView.text = trackName
            durationTextView.text = item.duration

            val textColorId =
                if (item.isSelected) context.getColor(R.color.purple_200) else context.getColor(R.color.black)
            trackNameTextView.setTextColor(textColorId)
            durationTextView.setTextColor(textColorId)
        }

        holder.itemView.setOnClickListener {
            callbackClickItem(position)
        }
    }

    object ItemDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean =
            oldItem == newItem


        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean =
            oldItem == newItem
    }
}