package com.example.bustleplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bustleplayer.R
import com.example.bustleplayer.databinding.TrackItemBinding
import com.example.bustleplayer.models.TrackExtended

class TrackAdapter(
    private val context: Context,
    private val callbackPlayItem: (Int) -> Unit,
) :
    ListAdapter<TrackExtended, TrackAdapter.Holder>(ItemDiffCallback) {
    class Holder(val binding: TrackItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)

        val trackName = context.getString(R.string.track_name, item.artist, item.title)

        with(holder.binding) {
            trackNameTextView.text = trackName
            durationTextView.text = item.duration

            /*
            val textColorId =
                if (item.isSelected) context.getColor(R.color.play) else context.getColor(R.color.black)


            val icon =
                if (item.isPlaying) context.getDrawable(R.drawable.ic_stop) else context.getDrawable(
                    R.drawable.ic_play
                )

             */

            trackNameTextView.setTextColor(context.getColor(item.textColor))
            durationTextView.setTextColor(context.getColor(item.textColor))

            playStopButton.setImageDrawable(context.getDrawable(item.imagePlayId))

            playStopButton.setOnClickListener {
                callbackPlayItem(position)
            }
        }
    }

    object ItemDiffCallback : DiffUtil.ItemCallback<TrackExtended>() {
        override fun areItemsTheSame(oldItem: TrackExtended, newItem: TrackExtended): Boolean =
            oldItem == newItem


        override fun areContentsTheSame(oldItem: TrackExtended, newItem: TrackExtended): Boolean =
            oldItem == newItem
    }
}