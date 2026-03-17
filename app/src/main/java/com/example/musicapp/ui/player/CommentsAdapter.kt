package com.example.musicapp.ui.player

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.Comment
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.*

class CommentsAdapter(
    private val onLikeClick: (Comment) -> Unit,
    private val onMenuClick: (Comment, View) -> Unit
) : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view, onLikeClick, onMenuClick)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(
        itemView: View,
        private val onLikeClick: (Comment) -> Unit,
        private val onMenuClick: (Comment, View) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val imgAvatar: ShapeableImageView = itemView.findViewById(R.id.imgAvatar)
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val btnLike: ImageButton = itemView.findViewById(R.id.btnLike)
        private val tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
        private val btnCommentMenu: ImageButton = itemView.findViewById(R.id.btnCommentMenu)

        fun bind(comment: Comment) {
            try {
                // Log để debug
                Log.d("CommentsAdapter", "=== Binding comment ===")
                Log.d("CommentsAdapter", "Comment ID: ${comment._id}")
                Log.d("CommentsAdapter", "UserID: ${comment.userId?._id}")
                Log.d("CommentsAdapter", "FullName: ${comment.userId?.fullName}")
                Log.d("CommentsAdapter", "Avatar: ${comment.userId?.avatar}")
                
                // Xử lý trường hợp userId null
                val userName = comment.userId?.fullName?.takeIf { it.isNotEmpty() } ?: "Unknown User"
                tvUserName.text = userName
                tvContent.text = comment.content
                tvLikes.text = comment.likes.toString()
                tvTime.text = getTimeAgo(comment.createdAt)
                
                // Load avatar an toàn
                val avatarUrl = comment.userId?.avatar
                Log.d("CommentsAdapter", "Loading avatar: $avatarUrl")
                
                Glide.with(itemView.context)
                    .load(avatarUrl ?: R.drawable.ic_user)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .into(imgAvatar)
                
                // Update like button
                btnLike.setImageResource(
                    if (comment.isLiked) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                )
                
                try {
                    btnLike.setColorFilter(
                        itemView.context.getColor(
                            if (comment.isLiked) R.color.red else R.color.gray
                        )
                    )
                } catch (e: Exception) {
                    btnLike.clearColorFilter()
                }
                
                btnLike.setOnClickListener { onLikeClick(comment) }
                btnCommentMenu.setOnClickListener { onMenuClick(comment, it) }
                
            } catch (e: Exception) {
                Log.e("CommentsAdapter", "Error binding comment: ${e.message}", e)
                // Fallback display
                tvUserName.text = "Unknown User"
                tvContent.text = comment.content
                tvLikes.text = "0"
            }
        }
        
        private fun getTimeAgo(timestamp: String): String {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(timestamp) ?: return ""
                
                val now = System.currentTimeMillis()
                val diff = now - date.time
                
                return when {
                    diff < 60000 -> "Just now"
                    diff < 3600000 -> "${diff / 60000}m ago"
                    diff < 86400000 -> "${diff / 3600000}h ago"
                    diff < 604800000 -> "${diff / 86400000}d ago"
                    else -> "${diff / 604800000}w ago"
                }
            } catch (e: Exception) {
                return ""
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
}
