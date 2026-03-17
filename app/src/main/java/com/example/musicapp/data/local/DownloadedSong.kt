package com.example.musicapp.data.local

data class DownloadedSong(
    val songId: String,
    val title: String,
    val artist: String,
    val coverImageUrl: String,
    val localFilePath: String,
    val downloadedAt: Long = System.currentTimeMillis(),
    val fileSize: Long = 0L
)
