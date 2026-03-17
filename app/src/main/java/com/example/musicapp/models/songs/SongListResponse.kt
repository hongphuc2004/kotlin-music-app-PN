package com.example.musicapp.models.songs

data class SongListResponse(
    val success: Boolean,
    val data: List<Song>
)