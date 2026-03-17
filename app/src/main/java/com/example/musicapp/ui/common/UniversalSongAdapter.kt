package com.example.musicapp.ui.common

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.DownloadRepository
import com.example.musicapp.models.songs.Song
import com.example.musicapp.ui.artist.ArtistDetailFragment

class UniversalSongAdapter(
    private var items: List<Song>,
    private val onClick: (Song) -> Unit,
    private val onAddToPlaylist: (Song) -> Unit,
    private val onToggleFavorite: (Song) -> Unit,
    private var favoriteSongIds: Set<String> = emptySet()
) : RecyclerView.Adapter<UniversalSongAdapter.SongVH>() {

    class SongVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtArtist: TextView = itemView.findViewById(R.id.txtArtist)
        val btnHeart: ImageView = itemView.findViewById(R.id.btnHeart)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongVH(view)
    }

    override fun onBindViewHolder(holder: SongVH, position: Int) {
        try {
            val song = items[position]

            holder.tvRank.text = (position + 1).toString()
            holder.txtTitle.text = song.title
            holder.txtArtist.text = song.artist.joinToString(", ") { it.fullName }

            Glide.with(holder.itemView)
                .load(song.coverImage)
                .placeholder(R.drawable.ic_default_album_art)
                .error(R.drawable.img_error)
                .centerCrop()
                .into(holder.imgCover)

            // Update heart icon
            updateHeartIcon(holder, song._id)

            // Click listeners
            holder.itemView.setOnClickListener { onClick(song) }
            holder.btnHeart.setOnClickListener { onToggleFavorite(song) }

            holder.btnMore.setOnClickListener { view ->
                showMenu(view, song)
            }

        } catch (e: Exception) {
            Log.e("UniversalSongAdapter", "Error binding view holder", e)
        }
    }

    private fun showMenu(view: View, song: Song) {
        val popup = android.widget.PopupMenu(view.context, view)
        popup.inflate(R.menu.song_item_menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_playlist -> {
                    onAddToPlaylist(song)
                    true
                }
                R.id.action_go_to_artist -> {
                    goToArtist(view.context, song)
                    true
                }
                R.id.action_download -> {
                    downloadSongWithRepository(view.context, song)
                    true
                }
                R.id.action_share -> {
                    shareSong(view.context, song)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun goToArtist(context: Context, song: Song) {
        val firstArtist = song.artist.firstOrNull()
        if (firstArtist != null) {
            val fragment = ArtistDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("artistId", firstArtist._id)
                }
            }
            
            (context as? FragmentActivity)?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragmentContainer, fragment)
                ?.addToBackStack("ARTIST_DETAIL")
                ?.commit()
        } else {
            Toast.makeText(context, "No artist information", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadSongWithRepository(context: Context, song: Song) {
        try {
            val repository = DownloadRepository(context)
            repository.startDownload(song)
            Toast.makeText(context, "Downloading ${song.title}...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("UniversalSongAdapter", "Download error: ${e.message}", e)
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareSong(context: Context, song: Song) {
        val artistNames = song.artist.joinToString(", ") { it.fullName }
        val shareText = "🎵 ${song.title} - $artistNames\n\nListen now: ${song.fileUrl}"
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share song via"))
    }

    private fun updateHeartIcon(holder: SongVH, songId: String) {
        val isFavorite = favoriteSongIds.contains(songId)
        holder.btnHeart.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
        holder.btnHeart.alpha = if (isFavorite) 1.0f else 0.6f
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<Song>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun updateFavoriteIds(ids: Set<String>) {
        favoriteSongIds = ids
        notifyDataSetChanged()
    }

    fun getAllSongs(): List<Song> {
        return items
    }
}
