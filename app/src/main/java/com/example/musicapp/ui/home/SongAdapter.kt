package com.example.musicapp.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.MainActivity
import com.example.musicapp.R
import com.example.musicapp.models.songs.Song

class SongAdapter(
    private var items: List<Song>,
    private val onClick: (Song) -> Unit = {}
) : RecyclerView.Adapter<SongAdapter.SongVH>() {

    private var onAddToPlaylistClick: ((Song) -> Unit)? = null
    private var onHeartClick: ((Song) -> Unit)? = null
    private var favoriteSongIds: Set<String> = emptySet()

    // 👇 Thêm biến để điều khiển hiển thị
    private var showLimit = true
    private val LIMIT = 5

    fun setOnAddToPlaylistClickListener(listener: (Song) -> Unit) {
        onAddToPlaylistClick = listener
    }

    fun setOnHeartClickListener(listener: (Song) -> Unit) {
        onHeartClick = listener
    }

    fun updateFavoriteIds(ids: Set<String>) {
        favoriteSongIds = ids
        notifyDataSetChanged()
    }

    class SongVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtArtist: TextView = itemView.findViewById(R.id.txtArtist)
        val btnHeart: ImageView = itemView.findViewById(R.id.btnHeart)
        val btnAdd: ImageView = itemView.findViewById(R.id.btnAdd)
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

            // Rank
            holder.tvRank.text = (position + 1).toString()

            // Title + Artist (lấy artist đầu tiên hoặc "Unknown")
            holder.txtTitle.text = song.title
            holder.txtArtist.text = song.artist.firstOrNull()?.fullName ?: "Unknown Artist"
           Log.d("SongAdapter", "Artist name: ${song}")

            // Load cover
            Glide.with(holder.itemView)
                .load(song.coverImage)
                .placeholder(R.drawable.ic_default_album_art)
                .error(R.drawable.img_error)
                .centerCrop()
                .into(holder.imgCover)

            // Heart
            updateHeartIcon(holder, song._id)

            // Item click
            holder.itemView.setOnClickListener {
                (holder.itemView.context as? MainActivity)?.showMiniPlayer(song)
            }

            holder.btnHeart.setOnClickListener { onHeartClick?.invoke(song) }
            holder.btnAdd.setOnClickListener { onAddToPlaylistClick?.invoke(song) }

            holder.btnMore.setOnClickListener { view ->
                val popup = android.widget.PopupMenu(view.context, view)
                popup.inflate(R.menu.song_item_menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_add_playlist -> {
                            onAddToPlaylistClick?.invoke(song)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }

        } catch (e: Exception) {
            Log.e("SongAdapter", "Error binding view holder", e)
        }
    }

    // 👇 Chỉ trả về tối đa 5 bài khi showLimit = true
    override fun getItemCount(): Int {
        return if (showLimit && items.size > LIMIT) LIMIT else items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submit(newItems: List<Song>) {
        items = newItems
        showLimit = true // reset lại khi load danh sách mới
        notifyDataSetChanged()
    }

    fun updateData(newItems: List<Song>) {
        items = newItems
        notifyDataSetChanged()
    }

    // 👇 Khi bấm "Xem thêm"
    @SuppressLint("NotifyDataSetChanged")
    fun showAll() {
        showLimit = false
        notifyDataSetChanged()
    }

    private fun updateHeartIcon(holder: SongVH, songId: String) {
        val isFavorite = favoriteSongIds.contains(songId)
        holder.btnHeart.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
        holder.btnHeart.alpha = if (isFavorite) 1.0f else 0.6f
    }
}
