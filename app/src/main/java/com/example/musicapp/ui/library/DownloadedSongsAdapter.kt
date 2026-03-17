package com.example.musicapp.ui.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.local.DownloadedSong
import java.text.SimpleDateFormat
import java.util.*

class DownloadedSongsAdapter(
    private val onPlayClick: (DownloadedSong) -> Unit,
    private val onDeleteClick: (DownloadedSong) -> Unit
) : ListAdapter<DownloadedSong, DownloadedSongsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_downloaded_song, parent, false)
        return ViewHolder(view, onPlayClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        itemView: View,
        private val onPlayClick: (DownloadedSong) -> Unit,
        private val onDeleteClick: (DownloadedSong) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvArtist: TextView = itemView.findViewById(R.id.tvArtist)
        private val tvFileSize: TextView = itemView.findViewById(R.id.tvFileSize)
        private val btnPlay: ImageButton = itemView.findViewById(R.id.btnPlay)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        
        fun bind(song: DownloadedSong) {
            tvTitle.text = song.title
            tvArtist.text = song.artist
            tvFileSize.text = formatFileSize(song.fileSize)
            
            Glide.with(itemView.context)
                .load(song.coverImageUrl)
                .placeholder(R.drawable.ic_default_album_art)
                .into(imgCover)
            
            itemView.setOnClickListener { onPlayClick(song) }
            btnPlay.setOnClickListener { onPlayClick(song) }
            btnDelete.setOnClickListener { onDeleteClick(song) }
        }
        
        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> String.format("%.1f MB", bytes / 1024.0 / 1024.0)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DownloadedSong>() {
        override fun areItemsTheSame(oldItem: DownloadedSong, newItem: DownloadedSong): Boolean {
            return oldItem.songId == newItem.songId
        }

        override fun areContentsTheSame(oldItem: DownloadedSong, newItem: DownloadedSong): Boolean {
            return oldItem == newItem
        }
    }
}
