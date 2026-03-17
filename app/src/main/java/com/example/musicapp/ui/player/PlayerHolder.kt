package com.example.musicapp.ui.player


import androidx.media3.exoplayer.ExoPlayer
import com.example.musicapp.models.songs.Song

object PlayerHolder {
    lateinit var player: ExoPlayer
    var currentSong: Song? = null
}