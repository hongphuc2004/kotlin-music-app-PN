package com.example.musicapp.models.songs

import com.example.musicapp.models.artists.Artist
import com.google.gson.annotations.SerializedName

data class SongForArtist(
    @SerializedName("_id")
    val _id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("artist")
    val artist: List<Artist> = emptyList(),

    @SerializedName("album")
    val album: String?,

    @SerializedName("topic")
    val topic: List<String>?,

    @SerializedName("fileUrl")
    val fileUrl: String?,

    @SerializedName("coverImage")
    val coverImage: String?,

    @SerializedName("likes")
    val likes: List<String>?,

    @SerializedName("lyrics")
    val lyrics: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("deleted")
    val deleted: Boolean?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)