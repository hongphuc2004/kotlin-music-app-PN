package com.example.musicapp.ui.artist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.artists.Artist

class ArtistAdapter(
    private var artists: List<Artist> = emptyList(),
    private val onItemClick: ((Artist) -> Unit)? = null
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    inner class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.ivArtistCover)
        val tvName: TextView = itemView.findViewById(R.id.tvArtistName)

        fun bind(artist: Artist) {
            tvName.text = artist.fullName

            // Log để debug ảnh
            Log.d("ArtistAdapter", "Avatar URL = ${artist.coverImage}")

            // load ảnh ca sĩ
            Glide.with(itemView.context)
                .load(artist.coverImage)
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.img_error)
                .centerCrop()
                .into(ivCover)

            // sự kiện click
            itemView.setOnClickListener {
                onItemClick?.invoke(artist)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        holder.bind(artists[position])
    }

    override fun getItemCount() = artists.size

    // ✅ Thêm hàm update để dễ dàng thay đổi danh sách
    fun updateArtists(newArtists: List<Artist>) {
        artists = newArtists
        notifyDataSetChanged()
    }
}
