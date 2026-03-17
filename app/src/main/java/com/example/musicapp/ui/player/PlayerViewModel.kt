package com.example.musicapp.ui.player

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicapp.models.songs.Song
import com.example.musicapp.receiver.MusicService
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerProgress(val positionMs: Long, val durationMs: Long)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val _currentSong = MutableLiveData<Song?>(null)
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _progress = MutableLiveData(PlayerProgress(0L, 0L))
    val progress: LiveData<PlayerProgress> = _progress

    private val _currentPosition = MutableLiveData(0)
    val currentPosition: LiveData<Int> = _currentPosition

    private val _duration = MutableLiveData(0)
    val duration: LiveData<Int> = _duration

    private val _currentPlaylist = MutableLiveData<List<Song>>()
    val currentPlaylist: LiveData<List<Song>> = _currentPlaylist

    private val _currentIndex = MutableLiveData<Int>(0)
    val currentIndex: LiveData<Int> = _currentIndex

    val player: ExoPlayer = ExoPlayer.Builder(app).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.postValue(isPlaying)
            }
        })
    }

    init {
        // Gán player này cho PlayerHolder
        PlayerHolder.player = player

        // Tick cập nhật tiến độ mỗi 500ms
        viewModelScope.launch {
            while (isActive) {
                val durationMs = if (player.duration > 0) player.duration else 0L
                val position = player.currentPosition
                _progress.postValue(PlayerProgress(position, durationMs))
                _currentPosition.postValue(position.toInt())
                _duration.postValue(durationMs.toInt())
                delay(500)
            }
        }
    }

    private var songQueue: List<Song> = emptyList()
    private var currentSongIndex: Int = -1

    fun play(song: Song, queue: List<Song> = listOf(song)) {
        songQueue = queue
        currentSongIndex = queue.indexOfFirst { it._id == song._id }
        if (currentSongIndex == -1) {
            currentSongIndex = 0
            songQueue = listOf(song) + queue
        }

        val artistName = song.artist.firstOrNull()?.fullName ?: "Unknown Artist"

        val mediaItem = MediaItem.Builder()
            .setUri(song.fileUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(artistName)
                    .setArtworkUri(Uri.parse(song.coverImage ?: ""))
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _currentSong.postValue(song)

        // Start MusicService with song info
        val intent = Intent(getApplication(), MusicService::class.java).apply {
            putExtra("SONG_TITLE", song.title)
            putExtra("SONG_ARTIST", song.artist.firstOrNull()?.fullName ?: "Unknown")
            putExtra("SONG_URL", song.fileUrl)
            putExtra("SONG_COVER", song.coverImage)
        }
        ContextCompat.startForegroundService(getApplication(), intent)

        PlayerHolder.currentSong = song
    }

    fun setPlaylistAndPlay(playlist: List<Song>, song: Song) {
        play(song, playlist)
    }

    fun toggle() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun seekTo(positionMs: Int) {
        player.seekTo(positionMs.toLong())
    }

    fun playNext() {
        if (songQueue.isEmpty()) return
        currentSongIndex = (currentSongIndex + 1) % songQueue.size
        play(songQueue[currentSongIndex], songQueue)
    }

    fun playPrevious() {
        if (songQueue.isEmpty()) return
        currentSongIndex = if (currentSongIndex <= 0) songQueue.size - 1 else currentSongIndex - 1
        play(songQueue[currentSongIndex], songQueue)
    }

    fun updateProgress() {
        val position = player.currentPosition
        val durationMs = if (player.duration > 0) player.duration else 0L
        _currentPosition.postValue(position.toInt())
        _duration.postValue(durationMs.toInt())
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
