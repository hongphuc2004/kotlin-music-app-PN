package com.example.musicapp.ui.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.CommentRepository
import com.example.musicapp.models.Comment
import com.example.musicapp.network.ApiClient
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CommentsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var rvComments: RecyclerView
    private lateinit var etComment: TextInputEditText
    private lateinit var btnSendComment: FloatingActionButton
    private lateinit var tvCommentsTitle: TextView
    private lateinit var layoutEmptyComments: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var imgUserAvatar: ShapeableImageView
    
    private lateinit var commentsAdapter: CommentsAdapter
    private val commentRepository = CommentRepository()
    
    private var songId: String = ""
    private val comments = mutableListOf<Comment>()

    companion object {
        private const val ARG_SONG_ID = "song_id"
        private const val TAG = "CommentsBottomSheet"
        
        fun newInstance(songId: String): CommentsBottomSheet {
            return CommentsBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_SONG_ID, songId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        songId = arguments?.getString(ARG_SONG_ID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        loadCurrentUserAvatar()
        setupRecyclerView()
        setupListeners()
        loadComments()
        startRealtimeUpdates()
    }

    private fun initViews(view: View) {
        rvComments = view.findViewById(R.id.rvComments)
        etComment = view.findViewById(R.id.etComment)
        btnSendComment = view.findViewById(R.id.btnSendComment)
        tvCommentsTitle = view.findViewById(R.id.tvCommentsTitle)
        layoutEmptyComments = view.findViewById(R.id.layoutEmptyComments)
        progressBar = view.findViewById(R.id.progressBar)
        imgUserAvatar = view.findViewById(R.id.imgUserAvatar)
        
        view.findViewById<ImageButton>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }

    private fun loadCurrentUserAvatar() {
        lifecycleScope.launch {
            try {
                // Fetch user info if not already loaded
                if (ApiClient.currentUser == null) {
                    Log.d(TAG, "Fetching current user...")
                    ApiClient.fetchCurrentUser()
                }
                
                // Load avatar của người dùng hiện tại vào ô input
                ApiClient.currentUser?.data?.let { userData ->
                    Log.d(TAG, "Current user: username=${userData.username}, avatar=${userData.avatar}")
                    
                    context?.let { ctx ->
                        Glide.with(ctx)
                            .load(userData.avatar)
                            .placeholder(R.drawable.ic_user)
                            .error(R.drawable.ic_user)
                            .circleCrop()
                            .into(imgUserAvatar)
                    }
                } ?: run {
                    Log.e(TAG, "Current user data is null, using default avatar")
                    loadDefaultAvatar()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading current user: ${e.message}", e)
                loadDefaultAvatar()
            }
        }
    }

    private fun loadDefaultAvatar() {
        context?.let { ctx ->
            Glide.with(ctx)
                .load(R.drawable.ic_user)
                .circleCrop()
                .into(imgUserAvatar)
        }
    }

    private fun setupRecyclerView() {
        commentsAdapter = CommentsAdapter(
            onLikeClick = { comment -> likeComment(comment) },
            onMenuClick = { comment, view -> showCommentMenu(comment, view) }
        )
        
        rvComments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentsAdapter
        }
    }

    private fun setupListeners() {
        btnSendComment.setOnClickListener {
            val content = etComment.text?.toString()?.trim()
            if (!content.isNullOrEmpty()) {
                addComment(content)
            } else {
                Toast.makeText(context, "Please enter a comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadComments() {
        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                commentRepository.getComments(songId).fold(
                    onSuccess = { loadedComments ->
                        Log.d(TAG, "=== Loaded ${loadedComments.size} comments ===")
                        
                        // Filter out invalid comments (userId null)
                        val validComments = loadedComments.filter { comment ->
                            val isValid = comment.userId != null && comment._id.isNotEmpty()
                            if (!isValid) {
                                Log.w(TAG, "Invalid comment filtered: ${comment._id}, userId=${comment.userId}")
                            }
                            isValid
                        }
                        
                        Log.d(TAG, "Valid comments: ${validComments.size}")
                        
                        // Log chi tiết từng comment
                        validComments.forEachIndexed { index, comment ->
                            Log.d(TAG, "Comment #$index:")
                            Log.d(TAG, "  - ID: ${comment._id}")
                            Log.d(TAG, "  - UserID: ${comment.userId?._id}")
                            Log.d(TAG, "  - FullName: ${comment.userId?.fullName}")
                            Log.d(TAG, "  - Avatar: ${comment.userId?.avatar}")
                            Log.d(TAG, "  - Content: ${comment.content}")
                            Log.d(TAG, "  - Likes: ${comment.likes}")
                        }
                        
                        comments.clear()
                        comments.addAll(validComments)
                        commentsAdapter.submitList(comments.toList())
                        updateUI()
                        progressBar.visibility = View.GONE
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to load comments: ${error.message}", error)
                        error.printStackTrace()
                        Toast.makeText(context, "Failed to load comments: ${error.message}", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                        updateUI()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in loadComments: ${e.message}", e)
                e.printStackTrace()
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addComment(content: String) {
        btnSendComment.isEnabled = false
        
        lifecycleScope.launch {
            try {
                commentRepository.addComment(songId, content).fold(
                    onSuccess = { newComment ->
                        Log.d(TAG, "=== New comment added ===")
                        Log.d(TAG, "  - UserID: ${newComment.userId?._id}")
                        Log.d(TAG, "  - FullName: ${newComment.userId?.fullName}")
                        Log.d(TAG, "  - Avatar: ${newComment.userId?.avatar}")
                        Log.d(TAG, "  - Content: ${newComment.content}")
                        
                        // Kiểm tra comment hợp lệ trước khi add
                        if (newComment.userId != null && newComment._id.isNotEmpty()) {
                            comments.add(0, newComment)
                            commentsAdapter.submitList(comments.toList())
                            etComment.text?.clear()
                            updateUI()
                            rvComments.smoothScrollToPosition(0)
                            Toast.makeText(context, "Comment added!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e(TAG, "Invalid comment returned from server")
                            Toast.makeText(context, "Error: Invalid comment data", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to add comment: ${error.message}", error)
                        error.printStackTrace()
                        Toast.makeText(context, "Failed to add comment: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in addComment: ${e.message}", e)
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                btnSendComment.isEnabled = true
            }
        }
    }

    private fun likeComment(comment: Comment) {
        lifecycleScope.launch {
            commentRepository.likeComment(comment._id).fold(
                onSuccess = {
                    val index = comments.indexOfFirst { it._id == comment._id }
                    if (index != -1) {
                        val updatedComment = comment.copy(
                            isLiked = !comment.isLiked,
                            likes = if (comment.isLiked) comment.likes - 1 else comment.likes + 1
                        )
                        comments[index] = updatedComment
                        commentsAdapter.submitList(comments.toList())
                    }
                },
                onFailure = { error ->
                    Toast.makeText(context, "Failed to like comment", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showCommentMenu(comment: Comment, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.comment_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    deleteComment(comment)
                    true
                }
                R.id.action_report -> {
                    Toast.makeText(context, "Report comment", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun deleteComment(comment: Comment) {
        lifecycleScope.launch {
            commentRepository.deleteComment(comment._id).fold(
                onSuccess = {
                    comments.removeIf { it._id == comment._id }
                    commentsAdapter.submitList(comments.toList())
                    updateUI()
                    Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show()
                },
                onFailure = { error ->
                    Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun updateUI() {
        tvCommentsTitle.text = "Comments (${comments.size})"
        layoutEmptyComments.visibility = if (comments.isEmpty()) View.VISIBLE else View.GONE
        rvComments.visibility = if (comments.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun startRealtimeUpdates() {
        lifecycleScope.launch {
            while (isActive) {
                delay(10000) // Refresh every 10 seconds
                if (isVisible) {
                    loadComments()
                }
            }
        }
    }
}
