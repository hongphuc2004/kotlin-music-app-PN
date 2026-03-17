package com.example.musicapp.ui.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.playlists.Playlist

class PlaylistGridAdapter(
    private var playlists: List<Playlist>,
    private val onItemClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistGridAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.ivCover)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvSongCount: TextView = view.findViewById(R.id.tvSongCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val playlist = playlists[position]

        holder.tvTitle.text = playlist.title
        holder.tvSongCount.text = "${playlist.songs?.size ?: 0} songs"

        Glide.with(holder.itemView.context)
            .load(playlist.coverImage)
            .placeholder(R.drawable.ic_default_album_art)
            .error(R.drawable.ic_default_album_art)
            .centerCrop()
            .into(holder.ivCover)

        holder.itemView.setOnClickListener {
            onItemClick(playlist)
        }
    }

    override fun getItemCount() = playlists.size

    fun updateData(newPlaylists: List<Playlist>) {
        playlists = newPlaylists
        notifyDataSetChanged()
    }
}
