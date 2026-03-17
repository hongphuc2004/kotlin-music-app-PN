package com.example.musicapp.models.songs

import com.example.musicapp.models.artists.Artist
import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("_id")
    val _id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("artist")
    val artist: List<Artist> = emptyList(),

    @SerializedName("album")
    val album: String = "",

    @SerializedName("topic")
    val topic: List<String> = emptyList(),

    @SerializedName("fileUrl")
    val fileUrl: String = "",

    @SerializedName("coverImage")
    val coverImage: String = "",

    @SerializedName("likes")
    val likes: List<String> = emptyList(),

    @SerializedName("lyrics")
    val lyrics: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("status")
    val status: String = "active",

    @SerializedName("deleted")
    val deleted: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: String = "",

    @SerializedName("updatedAt")
    val updatedAt: String = ""
)