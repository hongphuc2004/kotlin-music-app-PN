package com.example.musicapp.ui.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.topic.Topic

class TopicAdapter(
    private var items: List<Topic>,
    private val onClick: (Topic) -> Unit
) : RecyclerView.Adapter<TopicAdapter.VH>() {

    inner class VH(item: View) : RecyclerView.ViewHolder(item) {
        val img: ImageView = item.findViewById(R.id.imgTopic)
        val tvTitle: TextView = item.findViewById(R.id.tvTopicTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val topic = items[position]

        // Set title
        holder.tvTitle.text = topic.title

        // Load image
        Glide.with(holder.itemView.context)
            .load(topic.imgTopic)
            .placeholder(R.drawable.ic_default_album_art)
            .into(holder.img)

        // Click → BẮT BUỘC TRUYỀN topic.id
        holder.itemView.setOnClickListener {
            onClick(topic) // topic.id | topic.title sẽ được truyền đúng lên
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Topic>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
