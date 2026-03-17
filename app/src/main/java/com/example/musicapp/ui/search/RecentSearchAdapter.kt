package com.example.musicapp.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.songs.Song

class RecentSearchAdapter(
    private var recentSongs: MutableList<Song> = mutableListOf(),
    private val onItemClick: (Song) -> Unit,
    private val onMoreClick: (Song) -> Unit
) : RecyclerView.Adapter<RecentSearchAdapter.RecentSearchViewHolder>() {

    inner class RecentSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtArtist: TextView = itemView.findViewById(R.id.txtArtist)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentSearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false) // 👈 Dùng item_song thay vì item_recent_search
        return RecentSearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentSearchViewHolder, position: Int) {
        val song = recentSongs[position]
        
        holder.tvRank.text = (position + 1).toString()
        holder.txtTitle.text = song.title
        holder.txtArtist.text = song.artist.joinToString(", ") { it.fullName }

        Glide.with(holder.itemView.context)
            .load(song.coverImage)
            .placeholder(R.drawable.ic_default_album_art)
            .error(R.drawable.ic_default_album_art)
            .centerCrop()
            .into(holder.imgCover)

        holder.itemView.setOnClickListener { onItemClick(song) }
        holder.btnMore.setOnClickListener { onMoreClick(song) }
    }

    override fun getItemCount(): Int = recentSongs.size

    fun updateData(newList: List<Song>) {
        recentSongs.clear()
        recentSongs.addAll(newList)
        notifyDataSetChanged()
    }
}
