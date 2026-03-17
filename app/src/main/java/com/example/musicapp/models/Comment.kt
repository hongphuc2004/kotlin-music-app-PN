package com.example.musicapp.models

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("_id")
    val _id: String = "",

    @SerializedName("songId")
    val songId: String = "",

    @SerializedName("userId")
    val userId: CommentUser? = null,  // Cho phép null

    @SerializedName("content")
    val content: String = "",

    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("likes")
    val likes: Int = 0,

    @SerializedName("isLiked")
    val isLiked: Boolean = false
)

data class CommentUser(
    @SerializedName("_id")
    val _id: String = "",

    @SerializedName("fullName")
    val fullName: String = "Unknown User",  // Default value

    @SerializedName("avatar")
    val avatar: String? = null
)

data class CommentResponse(
    @SerializedName("success")
    val success: Boolean = true,

    @SerializedName("comments")
    val comments: List<Comment> = emptyList(),

    @SerializedName("total")
    val total: Int = 0
)

data class AddCommentRequest(
    @SerializedName("songId")
    val songId: String,

    @SerializedName("content")
    val content: String
)

data class AddCommentResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("comment")
    val comment: Comment? = null,

    @SerializedName("message")
    val message: String? = null
)
