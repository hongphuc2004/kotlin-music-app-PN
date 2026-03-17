package com.example.musicapp.models.topic

import com.google.gson.annotations.SerializedName

data class Topic(
    @SerializedName("_id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String? = null,

    @SerializedName("deleted")
    val deleted: Boolean = false,

    @SerializedName("imgTopic")
    val imgTopic: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
