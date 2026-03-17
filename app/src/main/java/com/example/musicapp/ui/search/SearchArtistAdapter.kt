package com.example.musicapp.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.artists.Artist

class SearchArtistAdapter(
    private var items: List<Artist>,
    private val onItemClick: (Artist) -> Unit,
    private val onMoreClick: ((Artist) -> Unit)? = null
) : RecyclerView.Adapter<SearchArtistAdapter.VH>() {

    fun update(newItems: List<Artist>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_artist_search, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val artist = items[position]
        holder.bind(artist)
    }

    override fun getItemCount(): Int = items.size

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar: ImageView = itemView.findViewById(R.id.imgArtistAvatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvArtistName)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvArtistSubtitle)
        private val imgMore: ImageView = itemView.findViewById(R.id.imgMore)

        fun bind(artist: Artist) {
            tvName.text = artist.fullName ?: "Unknown"
            tvSubtitle.text = "Ca sĩ"

            // Load avatar nếu có, dùng Glide
            val avatar = artist.coverImage // hoặc trường tương ứng
            if (!avatar.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(avatar)
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(imgAvatar)
            } else {
                imgAvatar.setImageResource(R.drawable.ic_user)
            }

            itemView.setOnClickListener { onItemClick(artist) }
            imgMore.setOnClickListener { onMoreClick?.invoke(artist) }
        }
    }
}
