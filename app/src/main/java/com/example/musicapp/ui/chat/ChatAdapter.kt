package com.example.musicapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R
import com.example.musicapp.models.chat.AIChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private var messages: List<com.example.musicapp.models.chat.AIChatMessage> = emptyList()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_USER = 1
        const val VIEW_TYPE_BOT = 2
        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender == "user") VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_user, parent, false)
            UserMessageVH(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_bot, parent, false)
            BotMessageVH(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserMessageVH -> holder.bind(message)
            is BotMessageVH -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    fun updateMessages(newMessages: List<com.example.musicapp.models.chat.AIChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    class UserMessageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: com.example.musicapp.models.chat.AIChatMessage) {
            tvMessage.text = message.content
            tvTime.text = formatTime(message.timestamp)
        }
    }

    class BotMessageVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: com.example.musicapp.models.chat.AIChatMessage) {
            tvMessage.text = message.content
            tvTime.text = formatTime(message.timestamp)
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
